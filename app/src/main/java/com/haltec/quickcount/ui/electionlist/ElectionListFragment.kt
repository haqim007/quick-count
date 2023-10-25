package com.haltec.quickcount.ui.electionlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.haltec.quickcount.R
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.mechanism.ResourceHandler
import com.haltec.quickcount.data.mechanism.handle
import com.haltec.quickcount.databinding.FragmentElectionListBinding
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.ui.BaseFragment
import com.haltec.quickcount.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class ElectionListFragment : BaseFragment() {
    
    private lateinit var binding: FragmentElectionListBinding
    private val viewModel: ElectionListViewModel by hiltNavGraphViewModels(R.id.authorized_nav_graph)
    private val mainViewModel: MainViewModel by activityViewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentElectionListBinding.inflate(layoutInflater, container, false)
        
        val args : ElectionListFragmentArgs by navArgs()
        val tps = args.tps
        viewModel.setTps(tps)
        
        binding.apply {
            btnBack.setOnClickListener { 
                findNavController().navigateUp()
            }
            btnLogout.setOnClickListener {
                mainViewModel.requestToLogout()
            }
            tvTpsName.text = tps.name
            tvTpsLocation.text = getString(R.string.tps_location_, tps.village, tps.subdistrict)
            val adapter = setupAdapter()
            observeElectionList(adapter)
        }
        
        return binding.root
    }

    private fun FragmentElectionListBinding.setupAdapter(): ElectionListAdapter {
        val adapter = ElectionListAdapter(
            object : ElectionListAdapter.ElectionListCallback {
                override fun onClick(election: Election) {

                }
            })
        rvElections.adapter = adapter
        return adapter
    }

    private fun FragmentElectionListBinding.observeElectionList(adapter: ElectionListAdapter) {
        viewModel.state.map { it.data }.launchCollect {
            
            it.handle(
                object : ResourceHandler<List<Election>> {
                    override fun onSuccess(data: List<Election>?) {
                        adapter.submitList(data)
                        if (data.isNullOrEmpty()) {
                            layoutLoader.lavAnimation.setAnimation(R.raw.empty_box)
                            layoutLoader.lavAnimation.playAnimation()
                            layoutLoader.tvErrorMessage.text = getString(R.string.data_is_empty)
                        }
                        layoutLoader.lavAnimation.isVisible = data.isNullOrEmpty()
                        rvElections.isVisible = data?.isNotEmpty() == true
                    }

                    override fun onError(message: String?, data: List<Election>?) {
                        layoutLoader.lavAnimation.setAnimation(R.raw.error_box)
                        layoutLoader.lavAnimation.playAnimation()
                        layoutLoader.lavAnimation.isVisible = true
                        rvElections.isVisible = false
                        layoutLoader.tvErrorMessage.text =
                            message ?: getString(R.string.error_occured)
                    }

                    override fun onLoading() {
                        layoutLoader.lavAnimation.setAnimation(R.raw.loading)
                        layoutLoader.lavAnimation.playAnimation()
                        layoutLoader.lavAnimation.isVisible = true
                        rvElections.isVisible = false
                    }

                    override fun onAll(resource: Resource<List<Election>>) {
                        tvElectionListTitle.isVisible = resource !is Resource.Error
                        layoutLoader.btnTryAgain.isVisible = resource is Resource.Error
                        layoutLoader.tvErrorMessage.isVisible = resource is Resource.Error
                    }
                }
            )
        }
    }

}
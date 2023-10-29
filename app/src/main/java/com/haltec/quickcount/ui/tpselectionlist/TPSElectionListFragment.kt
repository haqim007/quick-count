package com.haltec.quickcount.ui.tpselectionlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.haltec.quickcount.R
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.mechanism.ResourceHandler
import com.haltec.quickcount.data.mechanism.handle
import com.haltec.quickcount.databinding.FragmentTpsElectionListBinding
import com.haltec.quickcount.domain.model.TPSElection
import com.haltec.quickcount.ui.BaseFragment
import com.haltec.quickcount.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class TPSElectionListFragment : BaseFragment() {
    
    private lateinit var binding: FragmentTpsElectionListBinding
    private val viewModel: TPSElectionListViewModel by hiltNavGraphViewModels(R.id.authorized_nav_graph)
    private val mainViewModel: MainViewModel by activityViewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentTpsElectionListBinding.inflate(layoutInflater, container, false)


        binding.apply {
            tpsElectionListSetup()

            btnLogout.setOnClickListener {
                mainViewModel.requestToLogout()
            }
            
            btnBack.setOnClickListener { findNavController().navigateUp() }

            val items = arrayOf("Semua", "Belum Dikirim", "Belum Terverifikasi", "Sudah Terverifikasi")
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
            etFilter.setAdapter(adapter)
            etFilter.setOnItemClickListener { adapterView, view, position, l -> 
                viewModel.setFilter(items[position])
            }
        }
        
        return binding.root
    }

    private fun FragmentTpsElectionListBinding.tpsElectionListSetup() {
        val adapter = TPSElectionListAdapter(
            object : TPSElectionListAdapter.TPSElectionListCallback {
                override fun onClick(tpsElection: TPSElection) {
                    findNavController().navigate(
                        TPSElectionListFragmentDirections.actionTPSElectionListFragmentToElectionActionFragment(
                            tpsElection.toTPS() , tpsElection.toElection()
                        )
                    )
                }
            }
        )
        rvTps.adapter = adapter
        viewModel.state.map { it.data }.launchCollectLatest {
            it.handle(
                object : ResourceHandler<List<TPSElection>> {
                    override fun onSuccess(data: List<TPSElection>?) {
                        adapter.submitList(data)
                        if (data.isNullOrEmpty()) {
                            layoutLoader.lavAnimation.setAnimation(R.raw.empty_box)
                            layoutLoader.lavAnimation.playAnimation()
                            layoutLoader.tvErrorMessage.text = getString(R.string.data_is_empty)
                            
                        }
                        layoutLoader.tvErrorMessage.isVisible = data.isNullOrEmpty()
                        layoutLoader.btnTryAgain.isVisible = data.isNullOrEmpty()
                        tilFilterDropdown.isVisible = true
                        layoutLoader.lavAnimation.isVisible = data.isNullOrEmpty()
                        rvTps.isVisible = data?.isNotEmpty() == true
                    }

                    override fun onError(message: String?, data: List<TPSElection>?) {
                        layoutLoader.lavAnimation.setAnimation(R.raw.error_box)
                        layoutLoader.lavAnimation.playAnimation()
                        layoutLoader.lavAnimation.isVisible = true
                        rvTps.isVisible = false
                        layoutLoader.tvErrorMessage.text = message ?: getString(R.string.error_occured)
                    }

                    override fun onLoading() {
                        layoutLoader.lavAnimation.setAnimation(R.raw.loading)
                        layoutLoader.lavAnimation.playAnimation()
                        layoutLoader.lavAnimation.isVisible = true
                        rvTps.isVisible = false
                        layoutLoader.tvErrorMessage.isVisible = false
                        layoutLoader.btnTryAgain.isVisible = false
                    }

                    override fun onAll(resource: Resource<List<TPSElection>>) {
                        tilFilterDropdown.isInvisible = resource is Resource.Error
                    }
                }
            )
        }

        layoutLoader.btnTryAgain.setOnClickListener {
            viewModel.getTPSElectionlist()
        }
    }

}
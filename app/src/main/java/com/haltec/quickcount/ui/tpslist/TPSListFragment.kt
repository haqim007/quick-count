package com.haltec.quickcount.ui.tpslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.haltec.quickcount.R
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.mechanism.ResourceHandler
import com.haltec.quickcount.data.mechanism.handle
import com.haltec.quickcount.databinding.FragmentTpsListBinding
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.ui.BaseFragment
import com.haltec.quickcount.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class TPSListFragment : BaseFragment() {
    
    private lateinit var binding: FragmentTpsListBinding
    private val viewModel by hiltNavGraphViewModels<TPSListViewModel>(R.id.authorized_nav_graph)
    private val activityViewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentTpsListBinding.inflate(layoutInflater, container, false)
        
        binding.apply {
            observeUserName()
            tpsListSetup()
            
            btnLogout.setOnClickListener {
                activityViewModel.requestToLogout()
            }
            chipFormList.setOnClickListener {
                findNavController().navigate(
                    TPSListFragmentDirections.actionTPSListFragmentToTPSElectionListFragment()
                )
            }
        }
        
        return binding.root
    }

    private fun FragmentTpsListBinding.observeUserName() {
        viewModel.state.map { it.userName }.launchCollectLatest {
            tvUserName.text = it
        }
    }

    private fun FragmentTpsListBinding.tpsListSetup() {
        val adapter = TPSListAdapter(
            object : TPSListAdapter.TPSListCallback {
                override fun onClick(tps: TPS) {
                    findNavController().navigate(
                        TPSListFragmentDirections.actionTPSListFragmentToElectionListFragment(tps)
                    )
                }
            }
        )
        rvTps.adapter = adapter
        viewModel.state.map { it.data }.launchCollectLatest {
            it.handle(
                object : ResourceHandler<List<TPS>> {
                    override fun onSuccess(data: List<TPS>?) {
                        adapter.submitList(data)
                        if (data.isNullOrEmpty()) {
                            layoutLoader.lavAnimation.setAnimation(R.raw.empty_box)
                            layoutLoader.lavAnimation.playAnimation()
                            layoutLoader.tvErrorMessage.text = getString(R.string.data_is_empty)
                        }
                        layoutLoader.lavAnimation.isVisible = data.isNullOrEmpty()
                        rvTps.isVisible = data?.isNotEmpty() == true
                    }

                    override fun onError(message: String?, data: List<TPS>?) {
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
                    }

                    override fun onAll(resource: Resource<List<TPS>>) {
                        cgMenu.isVisible = resource !is Resource.Error
                        tvTpsListTitle.isInvisible = resource is Resource.Error
                        layoutLoader.tvErrorMessage.isVisible = resource is Resource.Error
                        layoutLoader.btnTryAgain.isVisible = resource is Resource.Error
                    }
                }
            )
        }

        layoutLoader.btnTryAgain.setOnClickListener { 
            viewModel.getTPSList()
        }
    }
}
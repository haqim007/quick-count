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
import androidx.lifecycle.lifecycleScope
import com.haltec.quickcount.R
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.mechanism.ResourceHandler
import com.haltec.quickcount.data.mechanism.handle
import com.haltec.quickcount.databinding.FragmentTpsListBinding
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.ui.BaseFragment
import com.haltec.quickcount.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
            
            // remove
            ivTask.setOnClickListener {
                activityViewModel.logout()
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
                    Toast.makeText(requireContext(), tps.name, Toast.LENGTH_LONG).show()
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
                            lavAnimation.setAnimation(R.raw.empty_box)
                            lavAnimation.playAnimation()
                            tvErrorMessage.text = getString(R.string.data_is_empty)
                        }
                        lavAnimation.isVisible = data.isNullOrEmpty()
                        rvTps.isVisible = data?.isNotEmpty() == true
                    }

                    override fun onError(message: String?, data: List<TPS>?) {
                        lavAnimation.setAnimation(R.raw.error_box)
                        lavAnimation.playAnimation()
                        lavAnimation.isVisible = true
                        rvTps.isVisible = false
                        tvErrorMessage.text = message ?: getString(R.string.error_occured)
                        btnTryAgain.isVisible = true
                    }

                    override fun onLoading() {
                        lavAnimation.setAnimation(R.raw.loading)
                        lavAnimation.playAnimation()
                        lavAnimation.isVisible = true
                        rvTps.isVisible = false
                        btnTryAgain.isVisible = false
                    }

                    override fun onAll(resource: Resource<List<TPS>>) {
                        cgMenu.isVisible = resource !is Resource.Error
                        tvTpsListTitle.isInvisible = resource is Resource.Error
                    }
                }
            )
        }

        btnTryAgain.setOnClickListener { 
            viewModel.getTPSList()
        }
    }
}
package com.haltec.quickcount.ui.tpslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.haltec.quickcount.R
import com.haltec.quickcount.databinding.FragmentTpsListBinding
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.ui.BaseFragment
import com.haltec.quickcount.ui.MainViewModel
import com.haltec.quickcount.ui.util.handleLoadStates
import com.haltec.quickcount.util.NotificationChannelEnum
import com.haltec.quickcount.util.NotificationUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
        viewModel.pagingFlow.launchCollect { 
            adapter.submitData(it)
        }

        adapter.loadStateFlow.launchCollect {
            handleLoadStates(
                it,
                layoutLoader,
                rvTps,
                adapter,
                getString(R.string.data_is_empty),
                getString(R.string.error_occured)
            )
        }
        

        layoutLoader.btnTryAgain.setOnClickListener { 
            adapter.retry()
        }

        srlTpsList.setOnRefreshListener {
            adapter.refresh()
            srlTpsList.isRefreshing = false
        }
    }
}
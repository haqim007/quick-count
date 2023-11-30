package com.haltec.quickcount.ui.tpselectionlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.flatMap
import androidx.paging.map
import com.google.android.material.chip.Chip
import com.haltec.quickcount.R
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.mechanism.ResourceHandler
import com.haltec.quickcount.data.mechanism.handle
import com.haltec.quickcount.databinding.FragmentTpsElectionListBinding
import com.haltec.quickcount.domain.model.ElectionFilter
import com.haltec.quickcount.domain.model.TPSElection
import com.haltec.quickcount.domain.model.text
import com.haltec.quickcount.ui.BaseFragment
import com.haltec.quickcount.ui.MainViewModel
import com.haltec.quickcount.ui.util.handleLoadStates
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

            val items = ElectionFilter.entries.map { 
                it.text
            }
            cgFilter.setOnCheckedStateChangeListener { _, checkedIds ->
                val typeChip = cgFilter.findViewById<Chip>(checkedIds[0])
                if (items.contains(typeChip.text.toString())){
                    viewModel.setFilter(typeChip.text.toString())
                }
                
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
        
        viewModel.pagingFlow.launchCollectLatest{
            adapter.submitData(it)
        }
        
        adapter.loadStateFlow.launchCollectLatest {
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

        srlTpsElection.setOnRefreshListener {
            adapter.refresh()
            srlTpsElection.isRefreshing = false
        }
    }

}
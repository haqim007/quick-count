package com.haltec.quickcount.ui.electionlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.haltec.quickcount.R
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.mechanism.ResourceHandler
import com.haltec.quickcount.data.mechanism.handle
import com.haltec.quickcount.databinding.FragmentElectionListBinding
import com.haltec.quickcount.databinding.LayoutLoaderAndErrorBinding
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.ui.BaseFragment
import com.haltec.quickcount.ui.MainViewModel
import com.haltec.quickcount.ui.util.handleLoadStates
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ElectionListFragment : BaseFragment() {
    
    private lateinit var binding: FragmentElectionListBinding
    private val viewModel: ElectionListViewModel by hiltNavGraphViewModels(R.id.authorized_nav_graph)
    private val mainViewModel: MainViewModel by activityViewModels()
    
    private lateinit var adapter: ElectionListAdapter
    
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
            adapter = setupAdapter()
            observeElectionList(adapter)

            srlElectionList.setOnRefreshListener {
                adapter.refresh()
                srlElectionList.isRefreshing = false
            }
        }
        
        return binding.root
    }

    private fun FragmentElectionListBinding.setupAdapter(): ElectionListAdapter {
        val adapter = ElectionListAdapter(
            object : ElectionListAdapter.ElectionListCallback {
                override fun onClick(election: Election) {
                    viewModel.state.value.tps?.let {
                        findNavController().navigate(
                            ElectionListFragmentDirections
                                .actionElectionListFragmentToElectionActionFragment(it, election)
                        )
                    }
                }
            })
        rvElections.adapter = adapter
        return adapter
    }

    private fun FragmentElectionListBinding.observeElectionList(adapter: ElectionListAdapter) {

        viewLifecycleOwner.lifecycleScope.launch { 
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.pagingFlow.launchCollect {
                    adapter.submitData(it)
                }
            }
        }

        adapter.loadStateFlow.launchCollect {
            handleLoadStates(
                it, 
                layoutLoader, 
                rvElections, 
                adapter, 
                getString(R.string.data_is_empty), 
                getString(R.string.error_occured)
            )
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.refresh()
    }

}


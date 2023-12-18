package com.haltec.quickcount.ui.tpselectionlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.haltec.quickcount.R
import com.haltec.quickcount.databinding.FragmentTpsElectionListBinding
import com.haltec.quickcount.domain.model.ElectionFilter
import com.haltec.quickcount.domain.model.TPSElection
import com.haltec.quickcount.domain.model.text
import com.haltec.quickcount.ui.BaseFragment
import com.haltec.quickcount.ui.MainViewModel
import com.haltec.quickcount.ui.util.handleLoadStates
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TPSElectionListFragment : BaseFragment() {
    
    private lateinit var binding: FragmentTpsElectionListBinding
    private val viewModel: TPSElectionListViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    
    private val filterItems = ElectionFilter.entries.map {
        it.text
    }
    
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
            
            cgFilter.setOnCheckedStateChangeListener { _, checkedIds ->
                val typeChip = cgFilter.findViewById<Chip>(checkedIds[0])
                if (filterItems.contains(typeChip.text.toString())){
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

    override fun onStart() {
        super.onStart()

        val typeChip = binding.cgFilter.findViewById<Chip>(R.id.chip_filter_all)
        typeChip.isChecked = true
        if (filterItems.contains(typeChip.text.toString())){
            viewModel.setFilter(typeChip.text.toString())
        }
    }

}
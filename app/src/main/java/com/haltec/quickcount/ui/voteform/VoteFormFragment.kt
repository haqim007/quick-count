package com.haltec.quickcount.ui.voteform

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.haltec.quickcount.R
import com.haltec.quickcount.databinding.FragmentVoteFormBinding
import com.haltec.quickcount.ui.BaseFragment
import com.haltec.quickcount.ui.vote.CandidateAdapter
import com.haltec.quickcount.ui.vote.VoteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class VoteFormFragment : BaseFragment() {
   
    private lateinit var binding: FragmentVoteFormBinding
    private val viewModel: VoteViewModel by hiltNavGraphViewModels(R.id.vote_graph)
    private var partyId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentVoteFormBinding.inflate(layoutInflater, container, false)

        val args: VoteFormFragmentArgs by navArgs()
        partyId = args.partyId
        viewModel.loadCandidates(partyId)
        
        // Inflate the layout for this fragment
        return binding.root
    }
    
    
    private val candidateAdapter = CandidateAdapter(object : CandidateAdapter.Callback{
        override fun onCandidateVoteChange(position: Int, partyId: Int, candidateId: Int, vote: Int) {
            viewModel.setCandidateVote(partyId, candidateId, vote)
        }
    })
    

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            
            val layoutManager = rvCandidate.layoutManager as LinearLayoutManager
            var adapterState: Parcelable? = layoutManager.onSaveInstanceState()
            
            btnClose.setOnClickListener { 
                findNavController().navigateUp()
            }
           
            
            viewModel.state.map { it.onEditParty }.launchCollectLatest { data ->

                data?.let {
                
                    tvPartyTitle.text = getString(R.string.data_perolehan_partai_s, data.partyName)
                    etTotalPartyVote.setText(data.totalPartyVote.toString())
                   
                    rvCandidate.adapter = candidateAdapter
                    rvCandidate.itemAnimator = null

                    candidateAdapter.submitList(data.candidateList)
                    layoutManager.onRestoreInstanceState(adapterState)
                    rvCandidate.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                        adapterState = layoutManager.onSaveInstanceState()
                    }

                    etTotalPartyVote.addTextChangedListener {
                        if (it.toString() != data.totalPartyVote.toString()){
                            viewModel.setPartyVote(data.id, it.toString().toIntOrNull() ?: 0)
                        }
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.getTotalVote(partyId).collectLatest {
                    tvTotalVote.text = it.toString()
                }
            }
            
        }

        

    }
}
package com.haltec.quickcount.ui.vote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.haltec.quickcount.R
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.mechanism.ResourceHandler
import com.haltec.quickcount.data.mechanism.handle
import com.haltec.quickcount.data.util.formatNumberWithSeparator
import com.haltec.quickcount.databinding.FragmentVoteBinding
import com.haltec.quickcount.domain.model.BasicMessage
import com.haltec.quickcount.domain.model.ElectionStatus
import com.haltec.quickcount.domain.model.VoteData
import com.haltec.quickcount.ui.BaseFragment
import com.haltec.quickcount.ui.voteform.VoteFormDialogCallback
import com.haltec.quickcount.ui.voteform.VoteFormDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class VoteFragment : BaseFragment() {
    
    private lateinit var binding: FragmentVoteBinding
    private val viewModel: VoteViewModel by hiltNavGraphViewModels(R.id.authorized_nav_graph)

    private val voteDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
    }
    
    private lateinit var formDialog: VoteFormDialogFragment
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentVoteBinding.inflate(inflater, container, false)
        
        val args: VoteFragmentArgs by navArgs()
        viewModel.setTpsElection(args.tps, args.election)
        val isEditable = args.election.statusVote != ElectionStatus.VERIFIED
        
        binding.apply {
            
            btnBack.setOnClickListener { 
                if (viewModel.state.value.hasInputData){
                    voteDialog
                        .setTitle(R.string.are_you_done)
                        .setMessage(R.string.you_need_to_reinput)
                        .setPositiveButton(getString(R.string.yes)) { _, _ ->
                            viewModel.clear()
                            findNavController().navigateUp()
                        }
                        .setNegativeButton(R.string.no, null)
                        .setCancelable(false)
                        .show()
                }else{
                    viewModel.clear()
                    findNavController().navigateUp()
                }
            }
            tvTpsName.text = args.tps.name
            tvElectionName.text = args.election.title

            mcvTotalInvalidVoteView.isVisible = !isEditable
            mcvTotalInvalidVote.isVisible = isEditable
            
            viewModel.state.map { it.invalidVote }.launchOnResumeCollectLatest {
                if (it.toString() != etTotalInvalidVote.text.toString()) {
                    etTotalInvalidVote.setText(it.toString())
                    tvTotalInvalidVote.text = formatNumberWithSeparator(it)
                }
            }
            
            etTotalInvalidVote.addTextChangedListener {
                if (it.toString() != viewModel.state.value.invalidVote.toString()){
                    viewModel.setInvalidVote(it.toString().toIntOrNull() ?: 0)
                }
            }

            setupVoteAdapter(isEditable, args.election.statusVote)
            
            cbApproveTerms.setOnCheckedChangeListener { _, isChecked -> 
                viewModel.setTermIsApproved(isChecked)
            }
            viewModel.state.map { it.termsIsApproved }.launchCollectLatest { 
                btnSubmit.isClickable = it
                btnSubmit.isEnabled = it
            }
            btnSubmit.setOnClickListener { 
                if(viewModel.state.value.termsIsApproved){
                    viewModel.submit()
                }
            }
            
            var invalidVoteExpanded = true
            clInvalidVoteHeader.setOnClickListener {
                if (invalidVoteExpanded){
                    btnToggleInvalidVote.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_bottom)
                    )
                    mcvTotalInvalidVote.visibility = View.VISIBLE
                }else{
                    btnToggleInvalidVote.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_top)
                    )
                    mcvTotalInvalidVote.visibility = View.GONE
                }

                invalidVoteExpanded = !invalidVoteExpanded
            }

            observeSubmitResult()
            
            mcvVerifiedMessage.isVisible = !isEditable
        }
        
        return binding.root
    }

    private fun FragmentVoteBinding.observeSubmitResult() {
        viewModel.state.map { it.submitResult }.launchCollectLatest {
            it.handle(object : ResourceHandler<BasicMessage> {
                override fun onSuccess(data: BasicMessage?) {
                    voteDialog
                        .setTitle(R.string.data_sent)
                        .setMessage(
                            getString(R.string.data_sent_successfully)
                        )
                        .setIcon(R.drawable.ic_success)
                        .setPositiveButton(getString(R.string.ok)){ _, _ ->
                            viewModel.clear()
                            findNavController().navigateUp()
                        }
                        .setCancelable(false)
                        .show()
                }

                override fun onError(message: String?, data: BasicMessage?) {
                    voteDialog
                        .setTitle(R.string.error_occured)
                        .setMessage(message)
                        .setIcon(R.drawable.ic_warning)
                        .setPositiveButton(getString(R.string.ok), null)
                        .show()
                }

                override fun onAll(resource: Resource<BasicMessage>) {
                    svContent.isVisible = resource !is Resource.Loading
                    cbApproveTerms.isVisible = resource !is Resource.Loading
                    btnSubmit.isVisible = resource !is Resource.Loading

                    layoutLoader.lavAnimation.setAnimation(R.raw.send_loading)
                    layoutLoader.lavAnimation.playAnimation()
                    layoutLoader.lavAnimation.isVisible = resource is Resource.Loading
                }
            })
        }
    }

    private fun FragmentVoteBinding.setupVoteAdapter(
        isEditable: Boolean,
        electionStatus: ElectionStatus
    ) {
        val adapter = VoteAdapter(isEditable, object: VoteAdapter.Callback{
            
            override fun toggleView(partyId: Int) {
                viewModel.toggleView(partyId)
            }

            override fun onEdit(data: VoteData.PartyListsItem) {
                formDialog = VoteFormDialogFragment.newInstance(object : VoteFormDialogCallback(){
                    override fun getData(): VoteData.PartyListsItem {
                        return data
                    }

                    override fun onCandidateVoteChange(partyId: Int, candidateId: Int, vote: Int) {
                        viewModel.setCandidateVote(partyId, candidateId, vote)
                    }

                    override fun onPartyVoteChange(partyId: Int, vote: Int) {
                        viewModel.setPartyVote(partyId, vote)
                    }

                    override fun getTotalVote(partyId: Int): Flow<Int> {
                        return viewModel.getTotalVote(partyId)
                    }

                    override fun getCandidateList(partyInt: Int): Flow<List<VoteData.Candidate>>{
                        return viewModel.getCandidateListData(partyInt)
                    }
                })

                formDialog.show(requireActivity().supportFragmentManager, VoteFormDialogFragment.TAG)
                
            }
        })
        
        rvVote.adapter = adapter
        rvVote.itemAnimator = null
        bindVoteData(adapter, electionStatus)
    }

    private fun FragmentVoteBinding.bindVoteData(
        adapter: VoteAdapter,
        electionStatus: ElectionStatus,
    ) {
        val isEditable = electionStatus != ElectionStatus.VERIFIED
        viewModel.state.apply {
            map { it.voteData }.launchCollectLatest { voteData ->
                voteData.handle(
                    object : ResourceHandler<VoteData> {
                        
                        override fun onSuccess(data: VoteData?) {
                            val dataEmpty = data == null || data.partyLists.isEmpty()
                            val dataNotEmpty = data != null && data.partyLists.isNotEmpty()
                            svContent.isVisible = dataNotEmpty
                            
                            layoutLoader.lavAnimation.isVisible = dataEmpty
                            cbApproveTerms.isVisible = dataNotEmpty && isEditable
                            btnSubmit.isVisible = dataNotEmpty && isEditable
                            mcvInvalidVote.isVisible = dataNotEmpty
                            tvTpsName.isVisible = dataNotEmpty
                            llAreaTitle.isVisible = dataNotEmpty
                            llAreaValue.isVisible = dataNotEmpty

                            if (dataEmpty) {
                                layoutLoader.lavAnimation.setAnimation(R.raw.empty_box)
                                layoutLoader.lavAnimation.playAnimation()
                                layoutLoader.tvErrorMessage.text = getString(R.string.list_party_empty)
                                layoutLoader.tvErrorMessage.isVisible = true
                                layoutLoader.btnTryAgain.isVisible = true
                            } else {

                                tvVillageName.text = data!!.village
                                tvSubdistrictName.text = data.subdistrict
                                // show data in list
                                adapter.submitList(data.partyLists)
                                
                                // show note
                                if (electionStatus == ElectionStatus.REJECTED){
                                    if (!data.note.isNullOrBlank()){
                                        tvRejectedMessage.text = data.note
                                        mcvRejectedMessage.isVisible = true
                                        btnShowNote.isVisible = false
                                        btnCloseNote.setOnClickListener { 
                                            mcvRejectedMessage.isVisible = false
                                            btnShowNote.isVisible = true
                                        }
                                        btnShowNote.setOnClickListener {
                                            mcvRejectedMessage.isVisible = true
                                            btnShowNote.isVisible = false
                                        }
                                    }
                                }
                            }
                        }

                        override fun onError(message: String?, data: VoteData?) {
                            // loader
                            layoutLoader.lavAnimation.setAnimation(R.raw.error_box)
                            layoutLoader.lavAnimation.playAnimation()
                            layoutLoader.lavAnimation.isVisible = true
                            layoutLoader.tvErrorMessage.text =
                                message ?: getString(R.string.error_occured)
                            layoutLoader.btnTryAgain.isVisible = true
                            layoutLoader.tvErrorMessage.isVisible = true
                        }

                        override fun onLoading() {
                            // loader
                            layoutLoader.lavAnimation.isVisible = true
                            layoutLoader.lavAnimation.setAnimation(R.raw.loading)
                            layoutLoader.lavAnimation.playAnimation()
                            layoutLoader.btnTryAgain.isVisible = false
                            layoutLoader.tvErrorMessage.isVisible = false

                            //content
                            svContent.isVisible = false
                            mcvInvalidVote.isVisible = false
                            tvTpsName.isVisible = false
                            llAreaTitle.isVisible = false
                            llAreaValue.isVisible = false
                            cbApproveTerms.isVisible = false
                            btnSubmit.isVisible = false
                            mcvVerifiedMessage.isVisible = false
                        }
                    }
                )
                
            }
        }

        layoutLoader.btnTryAgain.setOnClickListener {
            viewModel.fetchCandidates()
        }
    }


}
package com.haltec.quickcount.ui.voteform

import android.app.Dialog
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.haltec.quickcount.R
import com.haltec.quickcount.databinding.FragmentVoteFormDialogBinding
import com.haltec.quickcount.domain.model.VoteData
import com.haltec.quickcount.ui.vote.CandidateAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize


class VoteFormDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentVoteFormDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var callback: VoteFormDialogCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentVoteFormDialogBinding.inflate(inflater, container, false)

        binding.btnClose.setOnClickListener { 
            dismiss()
        }
        binding.btnClose1.setOnClickListener {
            dismissNow()
        }

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            setupFullHeight()
        }
        return dialog
    }

    private fun setupFullHeight() {
        val behavior = BottomSheetBehavior.from(binding.bottomSheetVoteForm)
        val layoutParams = binding.bottomSheetVoteForm.layoutParams
        val windowHeight = getWindowHeight()
        if (layoutParams != null) {
            layoutParams.height = windowHeight
        }
        binding.bottomSheetVoteForm.layoutParams = layoutParams
        behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun getWindowHeight(): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(VOTE_FORM_CALLBACK, VoteFormDialogCallback::class.java)!!
        }else{
            arguments?.getParcelable(VOTE_FORM_CALLBACK)!!
        }
        
        val data = callback.getData()
        
        if (data == null){
            dismiss()
        }else{
            
            binding.apply {
                tvPartyTitle.text = getString(com.haltec.quickcount.R.string.data_perolehan_partai_s, data.partyName)
                etTotalPartyVote.setText(data.totalPartyVote.toString())
                val childAdapter = CandidateAdapter(object : CandidateAdapter.Callback{
                    override fun onCandidateVoteChange(candidateId: Int, vote: Int) {
                        callback.onCandidateVoteChange(data.id, candidateId, vote)
                    }
                })
                rvCandidate.adapter = childAdapter
                rvCandidate.itemAnimator = null
                
                viewLifecycleOwner.lifecycleScope.launch {
                    callback.getCandidateList(data.id).collectLatest {
                        childAdapter.submitList(it)
                    }
                }
                
                etTotalPartyVote.addTextChangedListener { 
                    if (it.toString() != data.totalPartyVote.toString()){
                        callback.onPartyVoteChange(data.id, it.toString().toIntOrNull() ?: 0)
                    }
                }
                
                etTotalPartyVote.setOnFocusChangeListener { view, isFocused ->
                    binding.viewSpacer.isVisible = isFocused
                }
                
                viewLifecycleOwner.lifecycleScope.launch { 
                    callback.getTotalVote(data.id).collectLatest { 
                        tvTotalVote.text = it.toString()
                    }
                }
            }
        }

    }


    companion object {

        fun newInstance(callback: VoteFormDialogCallback): VoteFormDialogFragment =
            VoteFormDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(VOTE_FORM_CALLBACK, callback)
                }
            }
        
        const val VOTE_FORM_CALLBACK = "VOTE_FORM_CALLBACK"
        const val TAG = "VOTE_FORM_DIALOG"

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

@Parcelize
open class VoteFormDialogCallback: Parcelable{
    
    open fun getData(): VoteData.PartyListsItem? = null
    open fun onCandidateVoteChange(partyId: Int, candidateId: Int, vote: Int){}
    open fun onPartyVoteChange(partyId: Int, vote: Int){}
    open fun getTotalVote(partyId: Int): Flow<Int> = flowOf(0)

    open fun getCandidateList(partyInt: Int): Flow<List<VoteData.Candidate>> = flowOf(listOf())
}
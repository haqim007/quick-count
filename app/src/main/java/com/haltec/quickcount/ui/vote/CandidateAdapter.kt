package com.haltec.quickcount.ui.vote

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.haltec.quickcount.R
import com.haltec.quickcount.databinding.ItemCandidateBinding
import com.haltec.quickcount.domain.model.VoteData

class CandidateAdapter(
    private val callback: Callback
): ListAdapter<VoteData.Candidate, CandidateAdapter.ViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.onCreate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.setIsRecyclable(false)
        holder.onBind(
            position, getItem(position), callback
        )
    }

    class ViewHolder(
        private val binding: ItemCandidateBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun onBind(
            position: Int,
            data: VoteData.Candidate,
            callback: Callback
        ){
            binding.apply {
                tvCandidateName.text = itemView.context.getString(R.string.candidate_number_name_, data.orderNumber, data.candidateName)
                etCandidateVote.setText(data.totalCandidateVote.toString())
                var vote = data.totalCandidateVote
                var updateValue = false // flag to allow when to submit change to prevent submit value on scroll because of recycling view
           
                btnIncrease.setOnClickListener {
                    updateValue = true
                    etCandidateVote.setText((++vote).toString())
                    etCandidateVote.clearFocus()
                    btnDecrese.clearFocus()
                    updateValue = false
                }
                btnDecrese.setOnClickListener {
                    if (vote > 0){
                        updateValue = true
                        etCandidateVote.setText((--vote).toString())
                    }
                    etCandidateVote.clearFocus()
                    btnIncrease.clearFocus()
                    updateValue = false
                }
                
                etCandidateVote.setOnFocusChangeListener { v, hasFocus ->
                    val value = etCandidateVote.text.toString().toIntOrNull()
                    if (hasFocus){
                        if (value == null || value == 0){
                            etCandidateVote.setText("")
                        }
                        updateValue = true
                        
                    }else{
                        v.clearFocus()
                        updateValue = false
                        if (value == null){
                            etCandidateVote.setText("0")
                        }
                    }
                }
                etCandidateVote.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        if (data.totalCandidateVote.toString() != s.toString() && updateValue) {
                            
                            vote = s.toString().toIntOrNull() ?: 0
                            
                            callback.onCandidateVoteChange(
                                position,
                                data.partyId, 
                                data.id,
                                vote
                            )
                            
                        }
                    }
                })

            }
        }

        companion object{
            fun onCreate(parent: ViewGroup): ViewHolder{
                val itemView = ItemCandidateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(itemView)
            }
        }
    }

    interface Callback{
        fun onCandidateVoteChange(position: Int, partyId:Int, candidateId: Int, vote: Int)
    }


    private class ItemDiffCallback: DiffUtil.ItemCallback<VoteData.Candidate>(){
        override fun areItemsTheSame(
            oldItem: VoteData.Candidate,
            newItem: VoteData.Candidate,
        ): Boolean {
            return oldItem.id == newItem.id //&& oldItem.totalCandidateVote == newItem.totalCandidateVote
        }

        override fun areContentsTheSame(
            oldItem: VoteData.Candidate,
            newItem: VoteData.Candidate,
        ): Boolean {
            return oldItem == newItem
        }
    }

}
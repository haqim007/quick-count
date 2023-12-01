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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class CandidateAdapter(
    private val callback: Callback
): ListAdapter<VoteData.Candidate, CandidateAdapter.ViewHolder>(ItemDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.onCreate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.setIsRecyclable(false)
        holder.onBind(getItem(position), callback)
    }

    class ViewHolder(
        private val binding: ItemCandidateBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun onBind(
            data: VoteData.Candidate,
            callback: Callback
        ){
            binding.apply {

                tvOrderNumber.text = itemView.context.getString(R.string.candidate_order_number_, data.orderNumber)
                tvCandidateName.text = data.candidateName
                etCandidateVote.setText(data.totalCandidateVote.toString())
                var vote = data.totalCandidateVote
                btnIncrease.setOnClickListener {
                    etCandidateVote.setText((++vote).toString())
                    etCandidateVote.clearFocus()
                    btnDecrese.clearFocus()
                }
                btnDecrese.setOnClickListener {
                    if (vote > 0){
                        etCandidateVote.setText((--vote).toString())
                    }
                    etCandidateVote.clearFocus()
                    btnIncrease.clearFocus()
                }
                etCandidateVote.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        if (data.totalCandidateVote.toString() != s.toString()) {
                            
                            vote = s.toString().toIntOrNull() ?: 0
                            
                            callback.onCandidateVoteChange(
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
        fun onCandidateVoteChange(partyId:Int, candidateId: Int, vote: Int)
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
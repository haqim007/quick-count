package com.haltec.quickcount.ui.vote

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.haltec.quickcount.R
import com.haltec.quickcount.util.formatNumberWithSeparator
import com.haltec.quickcount.databinding.ItemCandidateViewBinding
import com.haltec.quickcount.domain.model.VoteData


class CandidateViewAdapter: ListAdapter<VoteData.Candidate, CandidateViewAdapter.ViewHolder>(ItemDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.onCreate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        holder.onBind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemCandidateViewBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun onBind(
            data: VoteData.Candidate
        ){
            binding.apply {
                tvCandidateName.text = itemView.context.getString(R.string.candidate_number_name_, data.orderNumber, data.candidateName)
                tvCandidateVote.text = itemView.context.getString(
                    R.string.total_vote_,
                    formatNumberWithSeparator(data.totalCandidateVote)
                )
            }
        }

        companion object{
            fun onCreate(parent: ViewGroup): ViewHolder{
                val itemView = ItemCandidateViewBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(itemView)
            }
        }
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
package com.haltec.quickcount.ui.electionlist

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.haltec.quickcount.R
import com.haltec.quickcount.databinding.ItemElectionBinding
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.domain.model.SubmitVoteStatus


class ElectionListAdapter(
    private val callback: ElectionListCallback
): PagingDataAdapter<Election, ElectionListAdapter.ElectionListViewHolder>(ItemDIffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElectionListViewHolder {
        return ElectionListViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ElectionListAdapter.ElectionListViewHolder, position: Int) {
        holder.bind(getItem(position), callback)
    }

    class ElectionListViewHolder(private val binding: ItemElectionBinding): RecyclerView.ViewHolder(binding.root){


        fun bind(election: Election?, callback: ElectionListCallback){
            election?.let {
                binding.apply {
                    tvElectionName.text = election.title
                    tvElectionInfo.text = if (arrayOf(SubmitVoteStatus.PENDING, SubmitVoteStatus.IN_QUEUE).contains(election.statusVote)){
                        null
                    }else{
                        itemView.context.getString(R.string.sent_at_date_time, election.updatedAt)
                    }
                    tvElectionInfo.isVisible = tvElectionInfo.text != null
                    val statusSpannable = SpannableString(itemView.context.getString(R.string.status_s, election.statusVote.label))
                    val statusColor: Int
                    val borderColor: Int
                    when(election.statusVote) {
                        SubmitVoteStatus.SUBMITTED -> {
                            statusColor = ContextCompat.getColor(
                                itemView.context,
                                R.color.color_status_election_submitted
                            )
                            borderColor = ContextCompat.getColor(
                                itemView.context,
                                R.color.color_border_election_submitted
                            )
                        }

                        SubmitVoteStatus.VERIFIED -> {
                            statusColor = ContextCompat.getColor(
                                itemView.context,
                                R.color.color_status_election_verified
                            )
                            borderColor = ContextCompat.getColor(
                                itemView.context,
                                R.color.color_border_election_verified
                            )
                        }

                        else -> {
                            statusColor = ContextCompat.getColor(
                                itemView.context,
                                R.color.color_status_election_not_sent
                            )
                            borderColor = ContextCompat.getColor(
                                itemView.context,
                                R.color.color_border_election_not_sent
                            )
                        }
                    }
                    statusSpannable.setSpan(ForegroundColorSpan(
                        statusColor
                    ), 8, statusSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    tvElectionStatus.text = statusSpannable

                    mcvElectionItem.strokeColor = borderColor

                    root.setOnClickListener {
                        callback.onClick(election)
                    }

                }
            }

        }

        companion object{
            fun create(parent: ViewGroup): ElectionListViewHolder  {
                val itemView = ItemElectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ElectionListViewHolder(itemView)
            }
        }
    }

    interface ElectionListCallback{
        fun onClick(election: Election)
    }

    private class ItemDIffCallback: DiffUtil.ItemCallback<Election>(){
        override fun areItemsTheSame(oldItem: Election, newItem: Election): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Election, newItem: Election): Boolean {
            return oldItem == newItem
        }
    }
}
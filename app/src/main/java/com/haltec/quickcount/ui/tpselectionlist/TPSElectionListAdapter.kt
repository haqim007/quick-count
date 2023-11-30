package com.haltec.quickcount.ui.tpselectionlist

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.haltec.quickcount.R
import com.haltec.quickcount.databinding.ItemTpsElectionBinding
import com.haltec.quickcount.domain.model.SubmitVoteStatus
import com.haltec.quickcount.domain.model.TPSElection


class TPSElectionListAdapter(
    private val callback: TPSElectionListCallback
): PagingDataAdapter<TPSElection, TPSElectionListAdapter.TPSElectionListViewHolder>(ItemDIffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TPSElectionListViewHolder {
        return TPSElectionListViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TPSElectionListViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it, callback) }
    }

    class TPSElectionListViewHolder(private val binding: ItemTpsElectionBinding): RecyclerView.ViewHolder(binding.root){


        fun bind(data: TPSElection, callback: TPSElectionListCallback){

            binding.apply {
                tvTpsName.text = data.tpsName
                tvElectionName.text = data.electionName
                tvElectionInfo.text = if (data.statusVote == SubmitVoteStatus.PENDING){
                    itemView.context.getString(R.string.input_data_before_date_time, data.createdAt)
                }else{
                    itemView.context.getString(R.string.sent_at_date_time, data.createdAt)
                }
                val statusSpannable = SpannableString(itemView.context.getString(R.string.status_s, data.statusVote.text))
                val statusColor: Int
                val borderColor: Int
                when(data.statusVote) {
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
                
                btnOpenElection.setOnClickListener { 
                    callback.onClick(data)
                }
                
            }

        }

        companion object{
            fun create(parent: ViewGroup): TPSElectionListViewHolder  {
                val itemView = ItemTpsElectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return TPSElectionListViewHolder(itemView)
            }
        }
    }

    interface TPSElectionListCallback{
        fun onClick(tpsElection: TPSElection)
    }

    private class ItemDIffCallback: DiffUtil.ItemCallback<TPSElection>(){
        override fun areItemsTheSame(oldItem: TPSElection, newItem: TPSElection): Boolean {
            return oldItem.tpsId == newItem.tpsId && oldItem.electionId == newItem.electionId
        }

        override fun areContentsTheSame(oldItem: TPSElection, newItem: TPSElection): Boolean {
            return oldItem == newItem
        }
    }
}
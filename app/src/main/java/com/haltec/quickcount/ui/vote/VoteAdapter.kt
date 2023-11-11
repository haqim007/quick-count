package com.haltec.quickcount.ui.vote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import com.haltec.quickcount.R
import com.haltec.quickcount.data.util.formatNumberWithSeparator
import com.haltec.quickcount.databinding.ItemVoteBinding
import com.haltec.quickcount.domain.model.VoteData

class VoteAdapter(
   private val isEditable: Boolean = true,
   private val callback: Callback
): ListAdapter<VoteData.PartyListsItem, VoteAdapter.ViewHolder>(ItemDiffCallback()) {
    
    private val viewPool = RecycledViewPool()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.onCreate(parent, viewPool)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.onBind(item, isEditable, callback)
    }

    override fun getItemId(position: Int): Long {
        val item = getItem(position) as  VoteData.PartyListsItem
        return item.id.toLong()
    }

    class ViewHolder(
        private val binding: ItemVoteBinding,
        private val viewPool: RecycledViewPool
    ): RecyclerView.ViewHolder(binding.root){
        
        fun onBind(
            data: VoteData.PartyListsItem,
            isEditable: Boolean,
            callback: Callback
        ){
            binding.apply {
                tvTotalPartyVote.text = formatNumberWithSeparator(data.totalPartyVote)
                
                tvPartyTitle.text = itemView.context.getString(R.string.data_perolehan_partai_s, data.partyName)
                tvTotalVote.text = data.totalVote.toString()
                
                val childAdapter = CandidateViewAdapter()
                rvCandidate.adapter = childAdapter
                rvCandidate.itemAnimator = null
                rvCandidate.setRecycledViewPool(viewPool)
                childAdapter.submitList(data.candidateList)
                btnToggle.setOnClickListener { 
                    callback.toggleView(data.id)
                }
                tvPartyTitle.setOnClickListener {
                    callback.toggleView(data.id)
                }
                if (data.isExpanded){
                    btnToggle.setImageDrawable(
                        ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_bottom)
                    )
                    clContent.visibility = View.VISIBLE
                }else{
                    btnToggle.setImageDrawable(
                        ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_top)
                    )
                    clContent.visibility = View.GONE
                }
                
                btnEdit.isVisible = isEditable
                if (isEditable){
                    btnEdit.setOnClickListener {
                        callback.onEdit(data)
                    }
                }
                
                mcvTotalPartyVote.isVisible = data.includePartyVote
                
            }
        }
        
        companion object{
            fun onCreate(parent: ViewGroup, viewPool: RecycledViewPool): ViewHolder{
                val itemView = ItemVoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(itemView, viewPool)
            }
        }
    }
    
    interface Callback{
        fun toggleView(partyId: Int)
        fun onEdit(data: VoteData.PartyListsItem)
    }


    private class ItemDiffCallback: DiffUtil.ItemCallback<VoteData.PartyListsItem>(){
        override fun areItemsTheSame(
            oldItem: VoteData.PartyListsItem,
            newItem: VoteData.PartyListsItem,
        ): Boolean {
            return oldItem.id == newItem.id && 
            oldItem.totalPartyVote == newItem.totalPartyVote && 
            oldItem.totalVote == newItem.totalVote
        }

        override fun areContentsTheSame(
            oldItem: VoteData.PartyListsItem,
            newItem: VoteData.PartyListsItem,
        ): Boolean {
            return oldItem == newItem
        }
    }

}
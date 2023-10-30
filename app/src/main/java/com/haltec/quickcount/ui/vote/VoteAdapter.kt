package com.haltec.quickcount.ui.vote

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import com.haltec.quickcount.R
import com.haltec.quickcount.databinding.ItemVoteBinding
import com.haltec.quickcount.domain.model.VoteData

class VoteAdapter(
   private val callback: Callback
): ListAdapter<VoteData.PartyListsItem, VoteAdapter.ViewHolder>(ItemDiffCallback()) {
    
    private val viewPool = RecycledViewPool()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.onCreate(parent, viewPool)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val item = getItem(position)
        holder.onBind(item, position, callback)
    }

    class ViewHolder(
        private val binding: ItemVoteBinding,
        private val viewPool: RecycledViewPool
    ): RecyclerView.ViewHolder(binding.root){
        
        fun onBind(
            data: VoteData.PartyListsItem,
            position: Int,
            callback: Callback
        ){
            binding.apply {
                val totalPartyVote = data.totalPartyVote.toString()
                etTotalPartyVote.setText(totalPartyVote)
                if (data.requestFocus){
                    etTotalPartyVote.requestFocus()
                }
                
                etTotalPartyVote.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        if (data.totalPartyVote.toString() != s.toString()) {
                            callback.onTotalPartyVoteChange(data.id, s.toString().toIntOrNull() ?: 0)
                        }
                    }
                })
                tvPartyTitle.text = itemView.context.getString(R.string.data_perolehan_partai_s, data.partyName)
                
                tvTotalVote.text = data.totalVote.toString()
                
                val childAdapter = CandidateAdapter(object : CandidateAdapter.Callback{
                    override fun onCandidateVoteChange(candidateId: Int, vote: Int) {
                        callback.onCandidateVoteChange(data.id, candidateId, vote)
                    }

                    override fun onLostFocus(candidateId: Int) {
                        callback.onCandidateLostFocus(data.id, candidateId)
                    }
                })
                rvCandidate.adapter = childAdapter
                rvCandidate.setRecycledViewPool(viewPool)
                childAdapter.submitList(data.candidateList)
                btnToggle.setOnClickListener { 
                    callback.toggleView(position, data.id)
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
        fun onCandidateVoteChange(partyId: Int, candidateId: Int, vote: Int)
        fun onTotalPartyVoteChange(partyId: Int, vote: Int)
        fun toggleView(position: Int, partyId: Int)
        
        fun onCandidateLostFocus(partyId: Int, candidateId: Int)
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
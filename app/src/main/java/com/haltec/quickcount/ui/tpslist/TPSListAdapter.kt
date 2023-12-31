package com.haltec.quickcount.ui.tpslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.haltec.quickcount.R
import com.haltec.quickcount.util.capitalizeWords
import com.haltec.quickcount.databinding.ItemTpsBinding
import com.haltec.quickcount.domain.model.TPS

class TPSListAdapter(
    private val callback: TPSListCallback
): PagingDataAdapter<TPS, TPSListAdapter.TPSListViewHolder>(ItemDIffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TPSListViewHolder {
        return TPSListViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TPSListViewHolder, position: Int) {
        holder.bind(getItem(position), callback)
    }
    
    class TPSListViewHolder(private val binding: ItemTpsBinding): RecyclerView.ViewHolder(binding.root){
        
        fun bind(tps: TPS?, callback: TPSListCallback){
            
            tps?.let {
                binding.apply {
                    tvTpsName.text = itemView.context.getString(R.string.tps_name_, capitalizeWords(tps.name))
                    tvTpsLocation.text = itemView.context.getString(
                        R.string.tps_location_,
                        capitalizeWords(tps.village),
                        capitalizeWords(tps.subdistrict)
                    )
                    tvDataSent.text = itemView.context.getString(
                        R.string.data_sent_, tps.submitted.toString()
                    )
                    tvDataUnverified.text = itemView.context.getString(
                        R.string.data_pending_, tps.pending.toString()
                    )
                    tvDataVerified.text = itemView.context.getString(
                        R.string.data_verified_, tps.approved.toString()
                    )
                    tvDataRejected.text = itemView.context.getString(
                        R.string.data_rejected_, tps.rejected.toString()
                    )
                    tvDataWaitToBeSent.text = itemView.context.getString(
                        R.string.data_wait_to_be_sent_, tps.waitToBeSent.toString()
                    )
                    root.setOnClickListener {
                        callback.onClick(tps)
                    }
                }
            }
            
        }
        
        companion object{
            fun create(parent: ViewGroup): TPSListViewHolder  {
                val itemView = ItemTpsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return TPSListViewHolder(itemView)
            }
        }
    }
    
    interface TPSListCallback{
        fun onClick(tps: TPS)
    }

    private class ItemDIffCallback: DiffUtil.ItemCallback<TPS>(){
        override fun areItemsTheSame(oldItem: TPS, newItem: TPS): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TPS, newItem: TPS): Boolean {
            return oldItem == newItem
        }
    }
}
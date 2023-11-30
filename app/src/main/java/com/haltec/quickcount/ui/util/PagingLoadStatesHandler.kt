package com.haltec.quickcount.ui.util

import androidx.core.view.isVisible
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.haltec.quickcount.R
import com.haltec.quickcount.databinding.LayoutLoaderAndErrorBinding


fun handleLoadStates(
    loadStates: CombinedLoadStates,
    layoutLoader: LayoutLoaderAndErrorBinding,
    recyclerView: RecyclerView,
    adapter: PagingDataAdapter<*, *>,
    emptyMessage: String,
    errorMessage: String
) {
    when  {
        loadStates.refresh is LoadState.Loading -> {
            layoutLoader.root.isVisible = true
            layoutLoader.lavAnimation.setAnimation(R.raw.loading)
            layoutLoader.lavAnimation.playAnimation()
            layoutLoader.lavAnimation.isVisible = true
            recyclerView.isVisible = false
            layoutLoader.tvErrorMessage.text = null
        }

        loadStates.refresh is LoadState.Error -> {
            if (adapter.itemCount == 0) {
                layoutLoader.root.isVisible = true
                layoutLoader.lavAnimation.setAnimation(R.raw.error_box)
                layoutLoader.lavAnimation.playAnimation()
                layoutLoader.lavAnimation.isVisible = true
                recyclerView.isVisible = false
                layoutLoader.tvErrorMessage.text = errorMessage
                layoutLoader.tvErrorMessage.isVisible = true
            } else {
                layoutLoader.lavAnimation.isVisible = false
                recyclerView.isVisible = true
                layoutLoader.tvErrorMessage.isVisible = false
            }
        }
        loadStates.append.endOfPaginationReached && adapter.itemCount == 0 -> {
            layoutLoader.root.isVisible = true
            recyclerView.isVisible = false
            layoutLoader.lavAnimation.setAnimation(R.raw.empty_box)
            layoutLoader.lavAnimation.playAnimation()
            layoutLoader.tvErrorMessage.text = emptyMessage
            layoutLoader.tvErrorMessage.isVisible = true
        }
        else -> {
            recyclerView.isVisible = true
            layoutLoader.tvErrorMessage.text = null
            layoutLoader.tvErrorMessage.isVisible = false
            layoutLoader.lavAnimation.cancelAnimation()
            layoutLoader.root.isVisible = false
        }
    }
}
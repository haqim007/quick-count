package com.haltec.quickcount.ui

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import com.haltec.quickcount.hideKeyboard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
abstract class BaseFragment() : Fragment(){
    fun <T> Flow<T>.launchCollectLatest(callback: suspend (value: T) -> Unit){
        viewLifecycleOwner.lifecycleScope.launch {
            this@launchCollectLatest.collectLatest {
                callback(it)
            }
        }
    }

    fun <T> Flow<T>.launchCollect(callback: suspend (value: T) -> Unit){
        viewLifecycleOwner.lifecycleScope.launch {
            this@launchCollect.collect {
                callback(it)
            }
        }
    }
    
    fun hideKeyboard(){
        view?.let { 
            requireActivity().hideKeyboard()
        }
    }
}
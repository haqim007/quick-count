package com.haltec.quickcount.ui

import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import com.haltec.quickcount.hideKeyboard
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

abstract class BaseFragment() : Fragment(){
    fun <T> Flow<T>.launchCollectLatest(callback: suspend (value: T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            this@launchCollectLatest.distinctUntilChanged().collectLatest {
                callback(it)
            }
        }
    }

    fun <T> Flow<T>.launchCollect(callback: suspend (value: T) -> Unit){
        viewLifecycleOwner.lifecycleScope.launch {
            this@launchCollect.distinctUntilChanged().collect {
                callback(it)
            }
        }
    }
    
    fun hideKeyboard(){
        view?.let { 
            requireActivity().hideKeyboard()
        }
    }

    protected fun isPermissionGranted(permission: String) =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
}
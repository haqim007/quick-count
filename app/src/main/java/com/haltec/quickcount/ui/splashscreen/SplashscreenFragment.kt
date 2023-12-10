package com.haltec.quickcount.ui.splashscreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.haltec.quickcount.R
import com.haltec.quickcount.databinding.FragmentSplashscreenBinding
import com.haltec.quickcount.ui.BaseFragment
import com.haltec.quickcount.worker.WorkerRunner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashscreenFragment : BaseFragment() {
    private val viewModel: SplashscreenViewModel by viewModels()
    private lateinit var binding: FragmentSplashscreenBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSplashscreenBinding.inflate(layoutInflater, container, false)
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeSession()
    }

    private fun observeSession() {
        viewLifecycleOwner.lifecycleScope.launch {
            val isSessionValid = viewModel.state.map { it.sessionValid }.distinctUntilChanged()
            isSessionValid.collectLatest { it ->
                if (isAdded && findNavController().currentDestination?.id == R.id.splashscreenFragment){
                    if(it?.isValid == false){
                        findNavController().navigate(
                            SplashscreenFragmentDirections.actionSplashscreenFragmentToLogin()
                        )
                    }else{
                        repeatOnLifecycle(Lifecycle.State.RESUMED){
                            viewModel.state.map { it.syncStatus }.distinctUntilChanged().collectLatest {

                                if (it?.hasSync == false && !it.syncInProgress){
                                    WorkerRunner.stopSyncWorker(requireContext())
                                    WorkerRunner.runSyncWorker(requireContext())
                                }else if(it?.hasSync == false && it.syncInProgress){
                                    binding.tvTitleMessage.isVisible = true
                                }else if (it?.hasSync == true && !it.syncInProgress){
                                    WorkerRunner.stopSyncWorker(requireContext())
                                    findNavController().navigate(
                                        SplashscreenFragmentDirections.actionSplashscreenFragmentToTPSListFragment()
                                    )
                                }else{
                                    binding.tvTitleMessage.isVisible = false
                                }
                            }
                        }
                    }
                }

            }
        }
    }


}
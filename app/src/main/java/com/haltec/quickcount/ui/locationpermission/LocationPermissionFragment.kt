package com.haltec.quickcount.ui.locationpermission

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import com.haltec.quickcount.databinding.FragmentLocationPermissionBinding
import com.haltec.quickcount.ui.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationPermissionFragment : Fragment() {
    
    private lateinit var binding: FragmentLocationPermissionBinding
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLocationPermissionBinding.inflate(layoutInflater, container, false)
        
        // Observe location access permission. If granted, will automatically navigate to Login
        viewLifecycleOwner.lifecycleScope.launch { 
            mainViewModel.state.map { it.isPermissionLocationGranted }.collectLatest {
                if(it == true){
                    findNavController().navigate(
                        LocationPermissionFragmentDirections.actionLocationPermissionFragmentToLoginFragment()
                    )
                }
            }
        }

        // Show location permission dialog
        binding.btnRequestLocPermission.setOnClickListener {
            mainViewModel.showLocationPermissionDialog()
        }
        
        return binding.root
    }

}
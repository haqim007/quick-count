package com.haltec.quickcount.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import com.haltec.quickcount.R
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.mechanism.ResourceHandler
import com.haltec.quickcount.data.mechanism.handle
import com.haltec.quickcount.data.util.generateDeviceToken
import com.haltec.quickcount.databinding.FragmentLoginBinding
import com.haltec.quickcount.domain.model.Login
import com.haltec.quickcount.ui.BaseFragment
import com.haltec.quickcount.ui.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment() {
    
    private lateinit var binding: FragmentLoginBinding
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: LoginViewModel by hiltNavGraphViewModels(R.id.unauthorized_nav_graph)

    private val loginDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)

        observeLocationPermissionAndSession()
        observeDeviceToken()
        
        binding.apply {
            inputHandlerSetup()
            observeLoginResult()
        }
        
        return binding.root
    }

    private fun FragmentLoginBinding.observeLoginResult() {
        viewModel.state.map { it.result }.launchCollectLatest {
            it.handle(object : ResourceHandler<Login> {
                
                override fun onSuccess(data: Login?) {
                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToTPSListFragment()
                    )
                }

                override fun onError(message: String?, data: Login?) {
                    var title = ""
                    var content = ""
                    message?.let {
                        title = getString(R.string.warning)
                        content = it
                    } ?: run {
                        title = getString(R.string.error_occured)
                        content = getString(R.string.please_contact_admin)
                    }
                    loginDialog
                        .setTitle(title)
                        .setMessage(content)
                        .setIcon(R.drawable.ic_warning)
                        .setPositiveButton(getString(R.string.ok), null)
                        .show()

                }

                override fun onAll(resource: Resource<Login>) {
                    btnLogin.isClickable = resource !is Resource.Loading
                    pbLogin.isVisible = resource is Resource.Loading
                    btnLogin.alpha = if (resource is Resource.Loading) 0.5F else 1F
                    tilUsername.isEnabled = resource !is Resource.Loading
                    tilPassword.isEnabled = resource !is Resource.Loading
                }
            })
        }
    }

    private fun FragmentLoginBinding.inputHandlerSetup() {
        btnLogin.setOnClickListener {
            viewModel.login(
                username = etUsername.text.toString(),
                password = etPassword.text.toString()
            )
            hideKeyboard()
        }
        viewModel.state.map {
            mapOf(
                "usernameIsValid" to it.usernameIsValid,
                "passwordIsValid" to it.passwordIsValid
            )
        }.launchCollectLatest {
            if (it["usernameIsValid"] == false) {
                tilUsername.error = "Username is required"
            } else {
                tilUsername.error = null
            }

            if (it["passwordIsValid"] == false) {
                tilPassword.error = "Password is required"
            } else {
                tilPassword.error = null
            }
        }
    }

    private fun observeDeviceToken() {
        viewModel.state.map { it.deviceToken }.launchCollectLatest {
            it?.let {
                binding.tvDeviceToken.text = getString(R.string.device_token_s, it)
            } ?: run {
                viewModel.saveDeviceToken(generateDeviceToken())
            }
        }
    }

    private fun observeLocationPermissionAndSession() {
        viewLifecycleOwner.lifecycleScope.launch {
            val isPermissionLocationGranted = mainViewModel.state.map { it.isPermissionLocationGranted }.distinctUntilChanged()
            val isSessionValid = mainViewModel.state.map { it.sessionValid }
            val loginResult = viewModel.state.map { it.result }
            val combineFlow = combine(isPermissionLocationGranted, isSessionValid, loginResult, ::Triple)
            combineFlow.collectLatest {(isPermissionLocationGranted, sessionValid, loginResult) ->
                if (isPermissionLocationGranted == false && findNavController().currentDestination?.id == R.id.loginFragment) {
                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToLocationPermissionFragment()
                    )
                }
                else if(
                    isPermissionLocationGranted == true &&
                    sessionValid?.isValid == true && 
                    loginResult is Resource.Idle && 
                    findNavController().currentDestination?.id == R.id.loginFragment
                ){
                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToTPSListFragment()
                    )
                }
            }
        }
    }

}
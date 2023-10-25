package com.haltec.quickcount

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.haltec.quickcount.databinding.ActivityMainBinding
import com.haltec.quickcount.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels()
    
    private val locationTrackerDialog by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.fitur_lokasi_nonaktif))
            .setMessage(getString(R.string.activate_location_tracker_via_settings))
            .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setCancelable(false)
            .create()
    }

    private val sessionDialog by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.session_end))
            .setMessage(getString(R.string.please_relogin_to_continue))
            .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                navController.navigate(R.id.action_logout)
            }
            .setCancelable(false)
            .create()
    }

    private val logoutDialog by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.are_you_done)
            .setMessage(R.string.you_need_to_relogin_to_continue)
            .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                viewModel.logout()
            }
            .setNegativeButton(R.string.no){dialog, which ->
                viewModel.cancelLogout()
            }
            .setCancelable(false)
            .create()
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){permissions ->
        if(permissions[ACCESS_COARSE_LOCATION] == true){
            checkLocationTrackerOn()
            if(permissions[ACCESS_FINE_LOCATION] == false){
                dialogLocationAccessDenied()
                viewModel.setPermissionLocationGrant(false)
            }else{
                viewModel.setPermissionLocationGrant(true)
            }
        }else if(permissions[ACCESS_COARSE_LOCATION] == false || permissions[ACCESS_FINE_LOCATION] == false){
            dialogLocationAccessDenied()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostSetup()

        lifecycleScope.launch {
            viewModel.state.map { it.showLocationPermissionDialog }.collectLatest {
                if(it){
                    launchLocationAccessPermission()
                }
            }
        }
        
        lifecycleScope.launch { 
            viewModel.state.map { it.requestToLogout }
                .distinctUntilChanged()
                .collectLatest { 
                if (it){
                    if (!logoutDialog.isShowing) logoutDialog.show()
                }
            }
        }
    }

    private fun observeSessionValidity() {
        lifecycleScope.launch {
            viewModel.state.map { it.sessionValid }.collect {
                if(navHostFragment.navController.currentDestination?.id !in listOf(
                        R.id.loginFragment,
                        R.id.locationPermissionFragment
                    )
                ){
                    if (it?.isValid == false && !it.hasLogout) {
                        if (locationTrackerDialog.isShowing) locationTrackerDialog.dismiss()
                        if (!sessionDialog.isShowing) sessionDialog.show()
                    }else if (it?.isValid == false && it.hasLogout) {
                        navController.navigate(R.id.action_logout)
                    }
                }
                
            }
        }
    }

    private fun navHostSetup() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment

        navHostFragment.navController.addOnDestinationChangedListener { controller, destination, arguments ->
            navController = controller
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    val checkLocationAccessPermission = checkLocationAccessPermission()
                    viewModel.setPermissionLocationGrant(checkLocationAccessPermission)
                    if (!checkLocationAccessPermission) {
                        if (destination.id !in listOf(
                                R.id.loginFragment,
                                R.id.locationPermissionFragment
                            )
                        ) {
                            launchLocationAccessPermission()
                        }
                    }
                    checkLocationTrackerOn()
                }
            }
            observeSessionValidity()
        }
    }

    private fun checkLocationTrackerOn(){
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Alert user to switch Location on in Settings
            if(!locationTrackerDialog.isShowing) locationTrackerDialog.show()
        }
    }
    
    private fun launchLocationAccessPermission(){
        if(shouldShowRequestPermissionRationale(
                ACCESS_COARSE_LOCATION
            ) && shouldShowRequestPermissionRationale(
                ACCESS_FINE_LOCATION
            )
        ){
            dialogLocationAccessDenied()
        }else{
            requestPermissionLauncher.launch(
                arrayOf(
                    ACCESS_COARSE_LOCATION,
                    ACCESS_FINE_LOCATION
                )
            )
        }
    }
    
    private fun checkLocationAccessPermission(): Boolean{
        return isPermissionGranted(ACCESS_COARSE_LOCATION) && isPermissionGranted(
            ACCESS_FINE_LOCATION)
    }

    private fun isPermissionGranted(permission: String) =
        ActivityCompat.checkSelfPermission(
            this@MainActivity,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    private fun dialogLocationAccessDenied(){
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.location_access_permission_required)
            .setMessage(R.string.please_give_location_access_permission)
            .setPositiveButton(R.string.open_settings) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                })
            }
            .setNegativeButton(R.string.close, null)
            .show()
    }
}
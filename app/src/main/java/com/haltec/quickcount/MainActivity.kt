package com.haltec.quickcount

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.CAMERA
import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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
import androidx.work.WorkManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.haltec.quickcount.databinding.ActivityMainBinding
import com.haltec.quickcount.ui.MainViewModel
import com.haltec.quickcount.ui.util.LocationTrackerManager
import com.haltec.quickcount.util.ConnectivityObserver
import com.haltec.quickcount.util.IConnectivityObserver
import com.haltec.quickcount.util.NotificationChannelEnum
import com.haltec.quickcount.util.NotificationUtil
import com.haltec.quickcount.worker.WorkerRunner
import com.haltec.quickcount.worker.WorkerRunner.SUBMIT_WORKER_TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectIndexed
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
    
    private val connectivityObserver: IConnectivityObserver by lazy { 
        ConnectivityObserver()
    }
    private var latestConnectivityState : IConnectivityObserver.Status? = null
    private lateinit var snackbarConnectivityStatus: Snackbar
    private var neverShowConnectivityDialogAgain: Boolean = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private val locationTrackerDialog by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.fitur_lokasi_nonaktif))
            .setMessage(getString(R.string.activate_location_tracker_via_settings))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
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
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                navController.navigate(R.id.action_logout)
                
                // clean all workers
                WorkManager.getInstance(applicationContext).cancelAllWork()
            }
            .setCancelable(false)
            .create()
    }

    private val connectivityObserverDialog by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.no_internet_connection))
            .setMessage(getString(R.string.data_is_cache))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                snackbarConnectivityStatus.show() // to maintain snackbar existence
            }
            .setNeutralButton(getString(R.string.close)) { _, _ ->
                neverShowConnectivityDialogAgain = true
            } // will close the snackbar
            .setCancelable(false)
            .create()
    }

    private val logoutDialog by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.are_you_done)
            .setMessage(R.string.you_need_to_relogin_to_continue)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton(R.string.no){ _, _ ->
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
                dialogPermissionWhenNotAllowed(
                    R.string.location_access_permission_required,
                    R.string.please_give_location_access_permission
                )
                viewModel.setPermissionLocationGrant(false)
            }else{
                viewModel.setPermissionLocationGrant(true)
            }
        }else if(permissions[ACCESS_COARSE_LOCATION] == false || permissions[ACCESS_FINE_LOCATION] == false){
            dialogPermissionWhenNotAllowed(
                R.string.location_access_permission_required,
                R.string.please_give_location_access_permission
            )
        }else if(permissions[CAMERA] == false){
            dialogPermissionWhenNotAllowed(
                R.string.camera_access_permission_required,
                R.string.please_give_camera_access_permission
            ) { _, _ -> navController.navigateUp() }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostSetup()

        // setup to show permission location access dialog
        lifecycleScope.launch {
            viewModel.state.map { it.showLocationPermissionDialog }.collectLatest {
                if(it){
                    launchLocationAccessPermission()
                }
            }
        }
        
        // setup show camera permission dialog
        lifecycleScope.launch { 
            viewModel.state.map { it.showCameraPermissionDialog }.collectLatest { 
                if(it){
                    launchCameraAccessPermission()
                    viewModel.showCameraPermissionDialog(false)
                }
            }
        }
        
        // observe request to logout
        lifecycleScope.launch { 
            viewModel.state.map { it.requestToLogout }
                .distinctUntilChanged()
                .collectLatest { 
                if (it){
                    if (!logoutDialog.isShowing) logoutDialog.show()
                }
            }
        }

        // setup permission dialog for notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            lifecycleScope.launch { 
                repeatOnLifecycle(Lifecycle.State.STARTED){
                    if (!isPermissionGranted(POST_NOTIFICATIONS)) {
                        requestPermissionLauncher.launch(arrayOf(POST_NOTIFICATIONS))
                    }
                }
            }
        }
        
        // observe connectivity of network
        lifecycleScope.launch { 
            repeatOnLifecycle(Lifecycle.State.STARTED){
                observeConnectivityChange()
            }
        }

        setupNotificationChannel()

        WorkManager.getInstance(this).getWorkInfosByTagLiveData(SUBMIT_WORKER_TAG)
            .observe(this) { workInfo ->
                workInfo.forEach {
                    Log.d("worker", it.toString())
                }
            }
    }
    
    private fun observeConnectivityChange(){
        val connectivityFlow = connectivityObserver.observer(this@MainActivity).distinctUntilChanged()
        val locationTracker = LocationTrackerManager(this, intervalMillis = 10000L, minimalDistance = 5.0F)
        val locationCallback = object: LocationCallback(){
            override fun onLocationResult(result: LocationResult) {
                if (result.lastLocation != null){
                    WorkerRunner.runSubmitVoteWorker(this@MainActivity, result.lastLocation!!)
                    locationTracker.stopLocationTracking(this)
                }
            }
        }
        
        lifecycleScope.launch {
            connectivityFlow.collectIndexed { index, status -> 
                latestConnectivityState = status
                // run worker on initial network state
                when(status){
                    IConnectivityObserver.Status.Available -> {
                        locationTracker.startLocationTracking(locationCallback)
                        viewModel.setOnline(true)
                    }
                    IConnectivityObserver.Status.Lost, IConnectivityObserver.Status.Unavailable -> {
                        WorkerRunner.stopSubmitVoteWorker(this@MainActivity)
                        viewModel.setOnline(false)
                        locationTracker.stopLocationTracking(locationCallback)
                    }
                    else -> {}
                }
                
                if (!(index == 0 && status == IConnectivityObserver.Status.Available)){
                    if(this@MainActivity::snackbarConnectivityStatus.isInitialized && 
                        snackbarConnectivityStatus.isShown
                    ){
                        snackbarConnectivityStatus.dismiss()
                    }
                    
                    if (connectivityObserverDialog.isShowing) connectivityObserverDialog.dismiss()
                    
                    when(status){
                        IConnectivityObserver.Status.Available -> {
                            snackbarConnectivityStatus = Snackbar.make(
                                binding.root,
                                getString(R.string.internet_connection_is_back),
                                Snackbar.LENGTH_SHORT)
                                .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                                .setTextColor(getColor(android.R.color.white))
                                .setBackgroundTint(getColor(R.color.color_success))

                            snackbarConnectivityStatus.show()
                            neverShowConnectivityDialogAgain = false
                        }
                        else -> {
                            snackbarConnectivityStatus = Snackbar.make(
                                binding.root,
                                getString(R.string.no_internet_connection),
                                Snackbar.LENGTH_INDEFINITE
                            )
                                .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                                .setTextColor(getColor(R.color.white))
                                .setBackgroundTint(getColor(R.color.color_danger))
                                .setAction(getString(R.string.dismiss)){}
                                .setActionTextColor(getColor(R.color.white))
                                .setAction(R.string.info){
                                    connectivityObserverDialog.show()
                                }

                            if (!neverShowConnectivityDialogAgain){
                                snackbarConnectivityStatus.show()
                            }
                            
                        }
                    }
                }
            }
        }
    }
    

    private fun observeSessionValidity() {
        lifecycleScope.launch {
            viewModel.state.map { it.sessionValid }.collect {
                if(navHostFragment.navController.currentDestination?.id !in listOf(
                        R.id.loginFragment,
                        R.id.locationPermissionFragment,
                        R.id.splashscreenFragment
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

        navHostFragment.navController.addOnDestinationChangedListener { controller, destination, _ ->
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
            dialogPermissionWhenNotAllowed(
                R.string.location_access_permission_required,
                R.string.please_give_location_access_permission
            )
        }else{
            requestPermissionLauncher.launch(
                arrayOf(
                    ACCESS_COARSE_LOCATION,
                    ACCESS_FINE_LOCATION
                )
            )
        }
    }
    
    private fun launchCameraAccessPermission(){
        if(shouldShowRequestPermissionRationale(CAMERA)){
            dialogPermissionWhenNotAllowed(
                R.string.camera_access_permission_required,
                R.string.please_give_camera_access_permission
            ){ _, _ -> navController.navigateUp()}
        }else{
            requestPermissionLauncher.launch(
                arrayOf(
                    CAMERA
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
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    private fun dialogPermissionWhenNotAllowed(
        title: Int,
        message: Int,
        onClose: OnClickListener? = null
    ){
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.open_settings) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                })
            }
            .setNegativeButton(R.string.close, onClose)
            .show()
    }
    
    private fun setupNotificationChannel(){
        // Initialize notification
        enumValues<NotificationChannelEnum>().forEach {
            NotificationUtil.createChannel(
                this,
                it.channelID,
                it.channelName,
                it.importance
            )
        }
    }

    override fun onStart() {
        super.onStart()
        WorkerRunner.stopSyncWorker(this)
    }

    override fun onStop() {
        super.onStop()
        if (viewModel.state.value.sessionValid?.isValid == true){
            WorkerRunner.runSyncWorker(this)   
        }
    }
}
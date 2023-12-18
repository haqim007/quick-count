package com.haltec.quickcount.ui.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task

@SuppressLint("MissingPermission")
class LocationTrackerManager(
    context: Context,
    private var intervalMillis: Long,
    private var minimalDistance: Float
) {

    private var request: LocationRequest
    private var locationClient: FusedLocationProviderClient

    init {
        // getting the location client
        locationClient = LocationServices.getFusedLocationProviderClient(context)
        request = createRequest()
    }

    private fun createRequest(): LocationRequest =
        // New builder
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis).apply {
            setMinUpdateDistanceMeters(minimalDistance)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()

    fun changeRequest(intervalMillis: Long, minimalDistance: Float, locationCallback: LocationCallback) {
        this.intervalMillis = intervalMillis
        this.minimalDistance = minimalDistance
        createRequest()
        stopLocationTracking(locationCallback)
        startLocationTracking(locationCallback)
    }

    fun startLocationTracking(locationCallback: LocationCallback): Task<Void> {
        Log.d(this::class.java.name, "start location tracking with callback $locationCallback")
        return locationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper())
    }


    fun stopLocationTracking(locationCallback: LocationCallback) {
        Log.d(this::class.java.name, "stop location tracking with callback $locationCallback")
        locationClient.flushLocations()
        locationClient.removeLocationUpdates(locationCallback)
    }


}
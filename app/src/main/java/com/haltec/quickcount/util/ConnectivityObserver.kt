package com.haltec.quickcount.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ConnectivityObserver: IConnectivityObserver {
    override fun observer(context: Context): Flow<IConnectivityObserver.Status> {
        return callbackFlow { 
            val connectivityManager = context.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
            val initialNetworkState = connectivityManager.activeNetwork?.let { network ->
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                if (networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true){
                    IConnectivityObserver.Status.Available
                }else{
                    IConnectivityObserver.Status.Unavailable
                }
                
            } ?: IConnectivityObserver.Status.Unavailable
            
            trySend(initialNetworkState)
            
            val callback = object: ConnectivityManager.NetworkCallback(){
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    trySend(IConnectivityObserver.Status.Available)
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    trySend(IConnectivityObserver.Status.Losing)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    trySend(IConnectivityObserver.Status.Lost)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    trySend(IConnectivityObserver.Status.Unavailable)
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    val isInternetAvailable = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    if (isInternetAvailable){
                        trySend(IConnectivityObserver.Status.Available)
                    }else{
                        trySend(IConnectivityObserver.Status.Unavailable)
                    }
                }
            }
            
            connectivityManager.registerDefaultNetworkCallback(callback)
            awaitClose { 
                connectivityManager.unregisterNetworkCallback(callback)
                close()
            }
        }
    }
}
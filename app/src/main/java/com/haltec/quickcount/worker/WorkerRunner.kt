package com.haltec.quickcount.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.UUID
import java.util.concurrent.TimeUnit

object WorkerRunner{

    const val SYNC_WORKER_TAG = "SyncWorker"
    const val SUBMIT_WORKER_TAG = "SubmitVoteWorker"
    
    fun runSubmitVoteWorker(context: Context){

        val submitVoteWorkRequest = OneTimeWorkRequestBuilder<SubmitVoteWorker>()
            .addTag(SUBMIT_WORKER_TAG)
            .setInitialDelay(3L, TimeUnit.SECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 3L, TimeUnit.SECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("SUBMIT_VOTE_WORKER", ExistingWorkPolicy.REPLACE, submitVoteWorkRequest)
    }
    
    fun stopSubmitVoteWorker(context: Context){
        WorkManager.getInstance(context).cancelAllWorkByTag(SUBMIT_WORKER_TAG)
    }
    
    
    fun runSyncWorker(context: Context){

//        val syncWorkerRequest = PeriodicWorkRequestBuilder<SyncWorker>(
//            1, TimeUnit.HOURS
//        )
//            .addTag(SYNC_WORKER_TAG)
//            .setInitialDelay(3L, TimeUnit.SECONDS)
//            .setBackoffCriteria(BackoffPolicy.LINEAR, 30L, TimeUnit.SECONDS)
//            .setConstraints(
//                Constraints.Builder()
//                    .setRequiredNetworkType(NetworkType.CONNECTED)
//                    .setRequiresBatteryNotLow(true)
//                    .build()
//            )
//            .build()
//
//        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//            "SYNC_WORKER",
//            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
//            syncWorkerRequest
//        )
        
        
    }

    fun stopSyncWorker(context: Context){
//        WorkManager.getInstance(context).cancelAllWorkByTag(SYNC_WORKER_TAG)
    }
}
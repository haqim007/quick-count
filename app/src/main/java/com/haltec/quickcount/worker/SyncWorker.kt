package com.haltec.quickcount.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.preference.DevicePreference
import com.haltec.quickcount.domain.repository.IOfflineRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

@HiltWorker
class SyncWorker @AssistedInject constructor (
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    @Assisted
    private val offlineRepository: IOfflineRepository,
    @Assisted
    private val devicePreference: DevicePreference
): CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            
            devicePreference.setSyncInProgress(true)
            
            Log.d("SyncWorker", "Running!")
            
            val submitWorker = withContext(Dispatchers.IO) {
                WorkManager.getInstance(context).getWorkInfosByTag(WorkerRunner.SUBMIT_WORKER_TAG)
                    .get()
            }
            
            // if submit worker is running, SyncWorker have to wait
            if (submitWorker.any { it.state == WorkInfo.State.RUNNING }){
                return Result.retry()
            }
            
            var isError = false
            offlineRepository.getAllData().collectLatest {
                isError = it is Resource.Error
                if (isError){
                    Log.d("SyncWorker", it.message!!)
                }
            }

            devicePreference.setSyncInProgress(false)
            return if (!isError){
                Log.d("SyncWorker", "Succeed!")
                devicePreference.setHasSync(true)
                Result.success()
            }else{
                retryOrFailure()
            }

        }catch (e: Exception){
            Log.d("SyncWorker", "Failed! ${e.message ?: (e.localizedMessage ?: "")}")
            devicePreference.setSyncInProgress(false)
            devicePreference.setHasSync(true)
            return Result.failure()
        }
        
    }

    private suspend fun retryOrFailure(): Result {
        return if (runAttemptCount > MAX_RETRIES) {
            devicePreference.setHasSync(true)
            Log.d("SyncWorker", "Failed! Maximum retries reached")
            Result.failure()
        } else {
            Log.d("SyncWorker", "Retrying! (${runAttemptCount}/${MAX_RETRIES})")
            Result.retry()
        }
    }

    companion object {
        const val MAX_RETRIES = 3
    }
}


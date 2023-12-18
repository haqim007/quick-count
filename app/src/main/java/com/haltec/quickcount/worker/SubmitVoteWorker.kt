package com.haltec.quickcount.worker

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.repository.IUploadEvidenceRepository
import com.haltec.quickcount.domain.repository.IVoteRepository
import com.haltec.quickcount.util.NotificationChannelEnum
import com.haltec.quickcount.util.NotificationUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@HiltWorker
class SubmitVoteWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    @Assisted
    private val voteRepository: IVoteRepository,
    @Assisted
    private val notificationUtil: NotificationUtil,
    @Assisted
    private val uploadEvidenceRepository: IUploadEvidenceRepository
): CoroutineWorker(context, params) {
    
    private var title: String = "Submit vote"
    private var failedReason: String? = null

    override suspend fun doWork(): Result {
        return try {
            val longitude = inputData.getDouble("longitude", 0.0)
            val latitude = inputData.getDouble("latitude", 0.0)
            val submitVoteResult = submitVote(longitude, latitude)
            if (submitVoteResult == Result.success()){
                uploadEvidence(longitude, latitude)
            }else{
                submitVoteResult
            }
            
        }catch (e: Exception){
            Log.e("SubmitVoteWorker", "$title Failed! ${e.localizedMessage}")
            Result.failure()
        }
        
    }

    private suspend fun submitVote(longitude: Double, latitude: Double): Result {
        Log.d("SubmitVoteWorker", "$title Running!")

        updateForegroundInfo {
            notificationUtil.initProgress(
                SUBMIT_WORKER_NOTIFICATION_ID,
                context,
                NotificationChannelEnum.VOTE_CHANNEL.channelID,
                "Processing vote to submit"
            )
        }

        
        val tempVoteData = voteRepository.getTempVoteData(longitude, latitude)
        val total = tempVoteData.count()
        if (total == 0) {
            Log.d("SubmitVoteWorker", "$title Succeed!")
            clearNotification()
            return Result.success()
        } else {
            updateForegroundInfo {
                notificationUtil.update(context) {
                    setContentText("Counting vote to submit")
                }
            }
        }
        
        tempVoteData.forEachIndexed { index, it ->
            updateForegroundInfo {
                notificationUtil.update(context) {
                    setContentText("Submitting vote (${(index + 1)}/${total})")
                }
            }
            var isError = false
            voteRepository.vote(it).collectLatest {
                isError = it is Resource.Error
                failedReason = it.message
            }

            if (isError) {
                delay(1000)
                failedNotification()
                clearNotification()
                return retryOrFailure(reason = failedReason)
            }
        }

        
        succeedNotification(total)
        clearNotification()
        failedReason = null
        Log.d("SubmitVoteWorker", "$title Succeed!")
        return Result.success()
    }

    private suspend fun SubmitVoteWorker.clearNotification() {
        delay(5000)
        updateForegroundInfo { notificationUtil.clear(context) }
    }

    private suspend fun uploadEvidence(longitude: Double, latitude: Double): Result {
        title = "Upload evidence"
        Log.d("SubmitVoteWorker file", "$title Running!")

        updateForegroundInfo {
            notificationUtil.initProgress(
                SUBMIT_WORKER_NOTIFICATION_ID,
                context,
                NotificationChannelEnum.VOTE_CHANNEL.channelID,
                "Processing file to upload"
            )
        }

        val tempUploadEvidence = uploadEvidenceRepository.getTempUploadEvidence(longitude, latitude)
        val total = tempUploadEvidence.count()
        if (total == 0) {
            Log.d("SubmitVoteWorker file", "$title Succeed!")
            return Result.success()
        } else {
            updateForegroundInfo {
                notificationUtil.update(context) {
                    setContentText("Counting file to upload")
                }
            }
        }

        tempUploadEvidence.forEachIndexed { index, request ->
            updateForegroundInfo {
                notificationUtil.update(context) {
                    setContentText("Uploading files (${(index + 1)}/${total})")
                }
            }
            var isError = false
            uploadEvidenceRepository.upload(request).collectLatest {
                isError = it is Resource.Error
                failedReason = it.message
            }

            if (isError) {
                delay(1000)
                failedNotification()
                clearNotification()
                return retryOrFailure(reason = failedReason)
            }
        }

        succeedNotification(total)
        clearNotification()
        failedReason = null
        Log.d("SubmitVoteWorker file", "$title Succeed! ${runAttemptCount + 1}/${MAX_RETRIES}")
        return Result.success()
    }

    private suspend fun SubmitVoteWorker.failedNotification() {
        updateForegroundInfo {
            notificationUtil.stopProgress(
                context,
                "$title failed! Retrying.. ${runAttemptCount + 1}/${MAX_RETRIES} of attempts"
            )
        }
    }

    private suspend fun SubmitVoteWorker.failedNotification(reason: String) {
        updateForegroundInfo {
            notificationUtil.stopProgress(
                context,
                reason
            )
        }
    }

    private suspend fun SubmitVoteWorker.succeedNotification(total: Int) {
        updateForegroundInfo {
            notificationUtil.stopProgress(context, "$title completed! (${total}/${total})")
        }
    }

    private fun retryOrFailure(reason: String?): Result {
        failedReason = null
        return if (runAttemptCount > MAX_RETRIES) {
            Log.d("SubmitVoteWorker", "Failed! $reason")
            Result.failure()
        } else {
            Log.d("SubmitVoteWorker", "Retrying! (${runAttemptCount+1}/${MAX_RETRIES})\n$reason")
            failedReason = null
            Result.retry()
        }
    }
    
    private suspend fun updateForegroundInfo(notification: () -> Notification){
        setForeground(
            ForegroundInfo(
                SUBMIT_WORKER_NOTIFICATION_ID,
                notification()
            )
        )
    }

    companion object {
        const val MAX_RETRIES = 10
        const val SUBMIT_WORKER_NOTIFICATION_ID = 1
    }
}


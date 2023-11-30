package com.haltec.quickcount.worker

import android.app.Notification
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
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

    override suspend fun doWork(): Result {
        return try {
            submitVote()
            uploadEvidence()
        }catch (e: Exception){
            Log.d("SubmitVoteWorker", "$title Failed!")
            Result.failure()
        }
        
    }

    private suspend fun submitVote(): Result {
        Log.d("SubmitVoteWorker", "$title Running!")

        val tempVoteData = voteRepository.getTempVoteData()
        val total = tempVoteData.count()
        if (total == 0) {
            Log.d("SubmitVoteWorker", "$title Succeed!")
            return Result.success()
        } else {
            updateForegroundInfo {
                notificationUtil.initProgress(
                    SUBMIT_WORKER_NOTIFICATION_ID,
                    context,
                    NotificationChannelEnum.VOTE_CHANNEL.channelID,
                    "Counting vote to submit"
                )
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
            }

            if (isError) {
                delay(3000)
                failedNotification()
                clearNotification()
                return retryOrFailure()
            }
        }

        succeedNotification(total)
        clearNotification()

        Log.d("SubmitVoteWorker", "$title Succeed!")
        return Result.success()
    }

    private suspend fun SubmitVoteWorker.clearNotification() {
        delay(5000)
        updateForegroundInfo { notificationUtil.clear(context) }
    }

    private suspend fun uploadEvidence(): Result {
        title = "Upload evidence"
        Log.d("SubmitVoteWorker", "$title Running!")

        val tempUploadEvidence = uploadEvidenceRepository.getTempUploadEvidence()
        val total = tempUploadEvidence.count()
        if (total == 0) {
            Log.d("SubmitVoteWorker", "$title Succeed!")
            return Result.success()
        } else {
            updateForegroundInfo {
                notificationUtil.initProgress(
                    SUBMIT_WORKER_NOTIFICATION_ID,
                    context,
                    NotificationChannelEnum.VOTE_CHANNEL.channelID,
                    "Counting file to upload"
                )
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
            }

            if (isError) {
                delay(3000)
                failedNotification()
                clearNotification()
                return retryOrFailure()
            }
        }

        succeedNotification(total)
        clearNotification()

        Log.d("SubmitVoteWorker", "$title Succeed!")
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

    private suspend fun SubmitVoteWorker.succeedNotification(total: Int) {
        updateForegroundInfo {
            notificationUtil.stopProgress(context, "$title completed! (${total}/${total})")
        }
    }

    private fun retryOrFailure(): Result {
        return if (runAttemptCount > MAX_RETRIES) {
            Log.d("SubmitVoteWorker", "Failed!")
            Result.failure()
        } else {
            Log.d("SubmitVoteWorker", "Retrying! (${runAttemptCount}/${MAX_RETRIES})")
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


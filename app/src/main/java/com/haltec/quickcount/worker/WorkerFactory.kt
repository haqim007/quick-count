package com.haltec.quickcount.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.haltec.quickcount.data.preference.DevicePreference
import com.haltec.quickcount.data.repository.UploadEvidenceRepository
import com.haltec.quickcount.di.Unscoped
import com.haltec.quickcount.domain.repository.IOfflineRepository
import com.haltec.quickcount.domain.repository.IUploadEvidenceRepository
import com.haltec.quickcount.domain.repository.IVoteRepository
import com.haltec.quickcount.util.NotificationUtil
import javax.inject.Inject

class AppWorkerFactory @Inject constructor(
    @Unscoped
    private val voteRepository: IVoteRepository,
    private val notificationUtil: NotificationUtil,
    private val offlineRepository: IOfflineRepository,
    @Unscoped
    private val uploadEvidenceRepository: IUploadEvidenceRepository,
    private val devicePreference: DevicePreference
): WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        return when(workerClassName){
            SubmitVoteWorker::class.java.name ->
                SubmitVoteWorker(
                    appContext, 
                    workerParameters, 
                    voteRepository, 
                    notificationUtil,
                    uploadEvidenceRepository
                    )
            SyncWorker::class.java.name ->
                SyncWorker(appContext, workerParameters, offlineRepository, devicePreference)
            else -> null
        }
        
    }
}
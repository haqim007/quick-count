package com.haltec.quickcount.util

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.haltec.quickcount.R
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.Delegates

data class NotificationData(
    val channelID: String,
    val channelName: String,
    val title: String,
    val content: String,
    val icon: Bitmap? = null,
    val pendingIntent: PendingIntent
)

enum class NotificationChannelEnum(val channelID: String, val channelName: String, val importance: Int){
    VOTE_CHANNEL("submit_vote_channel", "Submit Vote Progress", NotificationManager.IMPORTANCE_LOW),
}

/**
 * Notification util
 *
 * @property uId Unique ID
 * @constructor Create empty Notification util
 */
@Singleton
class NotificationUtil @Inject constructor() {

    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private var uId by Delegates.notNull<Int>()

    private fun initNotification(
        uId: Int,
        context: Context,
        data: NotificationData
    ){
        notificationBuilder =
            NotificationCompat
                .Builder(context, data.channelID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000, 1000))
                .setOnlyAlertOnce(false)
                .setContentIntent(data.pendingIntent)
                .setContentTitle(data.title)
                .setContentText(data.content)
                .setPriority(2)

        notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT
            >= Build.VERSION_CODES.O
        ) {
            val notificationChannel = NotificationChannel(
                data.channelID, data.channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Initialize uId
        this@NotificationUtil.uId = uId
        notify(context, uId)
        

    }

    private fun notify(context: Context, uId: Int): android.app.Notification {
        val build = notificationBuilder.build()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(uId, build)
        }
        
        return build
    }

    /**
     * Initialize progress on notification panel
     *
     * @param uId Unique ID
     * @param context Activity Context
     * @param channelID Notification channel ID
     * @param fileName Filename
     * @return Pair<NotificationManagerCompat, NotificationCompat.Builder>
     */
    fun initProgress(
        uId: Int,
        context: Context,
        channelID: String,
        content: String
    ): Notification {

        notificationBuilder =
            NotificationCompat.Builder(context, channelID)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(PROGRESS_MAX, 0, true)
                .setAutoCancel(true)

        // Initialize uId
        this@NotificationUtil.uId = uId
        notificationManager = NotificationManagerCompat.from(context)
        return notify(context, uId)
        

    }

    /**
     * Update notification
     *
     * @param progress Progress update
     */
    fun update(
        context: Context,
        builder: NotificationCompat.Builder.() -> Unit
    ): Notification {
        builder(notificationBuilder)
        return notify(context, uId)
    }

    /**
     * Stop progress
     *
     * @param context Activity or Application Context
     */
    fun stopProgress(
        context: Context,
        message: String,
    ): Notification {
        notificationBuilder
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)
        notificationBuilder.setContentText(message)
        return notify(context, uId)
        
    }
    
    fun clear(context: Context): Notification {
        notificationManager.cancel(uId)
        return notify(context, uId)
    }

    
    companion object{
        const val PROGRESS_MAX = 100

        /**
         * Create channel for notification
         *
         * @param context Activity or Application Context
         * @param channelID Notification channel ID
         * @param ChannelName Notification channel name
         * @param priority Notification priority
         */
        fun createChannel(context: Context, channelID: String, ChannelName: String,
                          priority: Int = NotificationManager.IMPORTANCE_DEFAULT): NotificationChannel? {
            if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.O
            ) {
                val notificationChannel = NotificationChannel(
                    channelID, ChannelName,
                    priority
                )
                val notificationManager: NotificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(notificationChannel)
                return notificationChannel
            }
            return null
        }

    }


}


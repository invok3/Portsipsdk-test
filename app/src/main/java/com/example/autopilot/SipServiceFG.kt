package com.example.autopilot

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import androidx.core.content.ContextCompat.*


class SipServiceFG : Service() {

    companion object {

        fun start(baseContext: Context) {
            baseContext.startForegroundService(Intent(baseContext, SipServiceFG::class.java))
        }

        fun stop(baseContext: Context) {
            baseContext.stopService(Intent(baseContext, SipServiceFG::class.java))
            Kore.clear()
        }

        fun updateNotification(
            baseContext: Context,
            progress: Int,
            count: Int,
            operation: String = "Scanning",
            number: String = "-",
            status: String = "Scanning.."
        ) {
            val mNotificationManager: NotificationManager = getSystemService(
                baseContext,
                NotificationManager::class.java
            ) as NotificationManager
            val notificationIntent = Intent(baseContext, MyActionReceiver::class.java)
            val pendingIntent = PendingIntent.getActivity(baseContext, 0, notificationIntent, 0)

            val mRemoteView: RemoteViews =
                RemoteViews(baseContext.packageName, R.layout.my_notification)
            val mLargeRemoteView: RemoteViews =
                RemoteViews(baseContext.packageName, R.layout.my_notification_extended)
            mLargeRemoteView.setTextViewText(R.id.description_field, "$operation $number")
            mLargeRemoteView.setTextViewText(R.id.status, status)
            mLargeRemoteView.setTextViewText(R.id.cCount, "$progress")
            mLargeRemoteView.setTextViewText(R.id.aCount, "$count")
            mRemoteView.setTextViewText(R.id.status, status)
            mRemoteView.setTextViewText(R.id.cCount, "$progress")
            mRemoteView.setTextViewText(R.id.aCount, "$count")
            //mRemoteView.setProgressBar(R.id.progress_bar, count, progress, true)
            mRemoteView.setProgressBar(R.id.progress_bar, 0, 0, true)
            val mNotificationIntentPause = Intent(baseContext, MyActionReceiver::class.java).apply {
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                action = "ACTION_PAUSE"
            }
            val mNotificationIntentPlay = Intent(baseContext, MyActionReceiver::class.java).apply {
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                action = "ACTION_PLAY"
            }
            val mNotificationIntentStop = Intent(baseContext, MyActionReceiver::class.java).apply {
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                action = "ACTION_STOP"
            }
            val pauseIntent = PendingIntent.getBroadcast(
                baseContext,
                1,
                mNotificationIntentPause,
                FLAG_UPDATE_CURRENT
            )
            val playIntent = PendingIntent.getBroadcast(
                baseContext,
                1,
                mNotificationIntentPlay,
                FLAG_UPDATE_CURRENT
            )
            val stopIntent = PendingIntent.getBroadcast(
                baseContext,
                1,
                mNotificationIntentStop,
                FLAG_UPDATE_CURRENT
            )
            mLargeRemoteView.setOnClickPendingIntent(R.id.pause_button_extended, pauseIntent)
            mLargeRemoteView.setOnClickPendingIntent(R.id.play_button_extended, playIntent)
            mLargeRemoteView.setOnClickPendingIntent(R.id.stop_button_extended, stopIntent)


            var mNotificationBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(baseContext, "MobileSip")
                    .setChannelId("MobileSip")
                    .setSmallIcon(R.drawable.ic_mo_dialer_foreground)
                    .setContentIntent(pendingIntent)
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomBigContentView(mLargeRemoteView)
                    .setCustomContentView(mRemoteView)
//            .setContentText("Asd")
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setContentTitle("Asd")
//            .addAction(R.drawable.ic_baseline_pause_24, "Pause", pauseIntent)
//            .addAction(R.drawable.ic_baseline_play_arrow_24, "Pause", playIntent)
//            .addAction(R.drawable.ic_baseline_stop_24, "Stop", stopIntent)
                    .setProgress(count, progress, false)
                    .setOnlyAlertOnce(true)

            val mNotification: Notification = mNotificationBuilder.build()
            mNotificationManager.notify(1945, mNotification)
        }

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, MyActionReceiver::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val mRemoteView = RemoteViews(baseContext.packageName, R.layout.my_notification)
        val mLargeRemoteView =
            RemoteViews(baseContext.packageName, R.layout.my_notification_extended)
        val mNotificationIntentPause = Intent(this, MyActionReceiver::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            action = "ACTION_PAUSE"
        }
        val mNotificationIntentPlay = Intent(this, MyActionReceiver::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            action = "ACTION_PLAY"
        }
        val mNotificationIntentStop = Intent(this, MyActionReceiver::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            action = "ACTION_STOP"
        }
        val pauseIntent =
            PendingIntent.getBroadcast(this, 1, mNotificationIntentPause, FLAG_UPDATE_CURRENT)
        val playIntent =
            PendingIntent.getBroadcast(this, 1, mNotificationIntentPlay, FLAG_UPDATE_CURRENT)
        val stopIntent =
            PendingIntent.getBroadcast(this, 1, mNotificationIntentStop, FLAG_UPDATE_CURRENT)
        mLargeRemoteView.setOnClickPendingIntent(R.id.pause_button_extended, pauseIntent)
        mLargeRemoteView.setOnClickPendingIntent(R.id.play_button_extended, playIntent)
        mLargeRemoteView.setOnClickPendingIntent(R.id.stop_button_extended, stopIntent)


        var mNotificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(baseContext, "MobileSip")
                .setChannelId("MobileSip")
                .setSmallIcon(R.drawable.ic_mo_dialer_foreground)
                .setContentIntent(pendingIntent)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomBigContentView(mLargeRemoteView)
                .setCustomContentView(mRemoteView)
//            .setContentText("Asd")
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setContentTitle("Asd")
//            .addAction(R.drawable.ic_baseline_pause_24, "Pause", pauseIntent)
//            .addAction(R.drawable.ic_baseline_play_arrow_24, "Pause", playIntent)
//            .addAction(R.drawable.ic_baseline_stop_24, "Stop", stopIntent)
                .setProgress(0, 0, true)
                .setOnlyAlertOnce(true)
        val mNotification: Notification = mNotificationBuilder.build()

        startForeground(1945, mNotification)
        Thread {
            Kore.mMain(baseContext)
        }.start()
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        val mNotificationManager = getSystemService(NotificationManager::class.java)
        val mNotificationChannel: NotificationChannel = NotificationChannel(
            "MobileSip",
            "MobileSipChannel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        mNotificationManager.createNotificationChannel(mNotificationChannel)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}
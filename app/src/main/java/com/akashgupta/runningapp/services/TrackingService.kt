package com.akashgupta.runningapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.akashgupta.runningapp.R
import com.akashgupta.runningapp.others.Constants.ACTION_PAUSE_SERVICE
import com.akashgupta.runningapp.others.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.akashgupta.runningapp.others.Constants.ACTION_START_OR_RESUME_SERVICE
import com.akashgupta.runningapp.others.Constants.ACTION_STOP_SERVICE
import com.akashgupta.runningapp.others.Constants.NOTIFICATION_CHANNEL_ID
import com.akashgupta.runningapp.others.Constants.NOTIFICATION_CHANNEL_NAME
import com.akashgupta.runningapp.others.Constants.NOTIFICATION_ID
import com.akashgupta.runningapp.ui.MainActivity
import timber.log.Timber

class TrackingService : LifecycleService() {

    /*LifecycleService() - reason for that we will need to observe, in which state our tracking service
    * currently in which lifecycle state.*/

    /* Foreground service and background service -> Foreground service must come with a notification,
    * user is actively aware of our service running. Big benefit of Foreground service is that it can
    * not be killed by Android System.
    * We can also use Background service here it will work, but if Android system need MEMORY then it might
    * happen that it kills your service.
    * So Foreground service is best option here.*/

    var isFirstRun = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun){
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service...")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false) //onclick
            .setOngoing(true) //swipe
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        //pending intent -> is used here to open our activity(TrackingFragment) when we click our notification
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
        /*FLAG_UPDATE_CURRENT -> Whenever we launch that pending intent and it already exists it will update it instead
        * of recreating it or restarting it.
        * IMPORTANT - we can go into MainActivity and whenever we get a new intent in MainActivity we can check if that
        * action is attached to that intent and if it's we simply want to navigate to our TrackingFragment from (WHEREVER WE
        * ARE). To accomplish that what we need to do is to define a GLOBAL ACTION in our NAV GRAPH. */
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

}
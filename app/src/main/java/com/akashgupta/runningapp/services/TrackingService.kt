package com.akashgupta.runningapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.akashgupta.runningapp.R
import com.akashgupta.runningapp.others.Constants.ACTION_PAUSE_SERVICE
import com.akashgupta.runningapp.others.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.akashgupta.runningapp.others.Constants.ACTION_START_OR_RESUME_SERVICE
import com.akashgupta.runningapp.others.Constants.ACTION_STOP_SERVICE
import com.akashgupta.runningapp.others.Constants.FASTEST_LOCATION_INTERVAL
import com.akashgupta.runningapp.others.Constants.LOCATION_UPDATE_INTERVAL
import com.akashgupta.runningapp.others.Constants.NOTIFICATION_CHANNEL_ID
import com.akashgupta.runningapp.others.Constants.NOTIFICATION_CHANNEL_NAME
import com.akashgupta.runningapp.others.Constants.NOTIFICATION_ID
import com.akashgupta.runningapp.others.TrackingUtility
import com.akashgupta.runningapp.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

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

    //we will use here so called fused location provider client, use to request location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        //Video -> Tracking User Location in the Background
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf()) //mutableListOf() -> empty list
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // only possible Because we declared that service as LifecycleService()
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            //Observer block here will actually be called with the value TRUE
        })
    }

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

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean){
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                /*val request = LocationRequest().apply { //deprecated
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }*/
                val request = LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
                    .setMinUpdateIntervalMillis(FASTEST_LOCATION_INTERVAL)
                    .build()
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    //Anonymous class of type object
    //Whenever we retrieve a new location which is saved in result variable we just add that location
    //to the end of our last polyline.
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!){
                result?.locations?.let { locations ->
                    for (location in locations){
                        addPathPoint(location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.latitude)
            //Now we want to add this position to the last polyline of our polylines list.
            pathPoints.value?.apply {
                //last() -> last index of that list
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    //fun that add an empty polyline so an empty list of lat/long coordinates at the emd of our polylines list
    //because when we pause our tracking and resume it again then we simply need to add that empty list first
    //before we can add coordinates in it.
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
        addEmptyPolyline()
        isTracking.postValue(true)

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
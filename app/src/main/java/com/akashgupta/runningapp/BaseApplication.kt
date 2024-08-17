package com.akashgupta.runningapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class BaseApplication: Application(){

    //Tell our app that it should use dagger hilt as dependency injection.
    //For that we need to create an application class

    //Dagger is compile time injected so that means when we launch our app it is
    //already clear which dependency will be injected into which class.

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

}
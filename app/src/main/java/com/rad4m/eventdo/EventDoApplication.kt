package com.rad4m.eventdo

import android.app.Application
import com.rad4m.eventdo.di.AppComponent
import com.rad4m.eventdo.di.AppComponentProvider
import com.rad4m.eventdo.di.DaggerAppComponent
import timber.log.Timber

class EventDoApplication : Application(), AppComponentProvider {

    override val appComponent: AppComponent by lazy {
        DaggerAppComponent
            .builder()
            .application(this)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
package com.rad4m.eventdo.di

import android.app.Application
import com.rad4m.eventdo.database.EventsDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DbModule {

    @Singleton
    @Provides
    fun provideDb(app: Application): EventsDatabase = EventsDatabase.invoke(app)
}
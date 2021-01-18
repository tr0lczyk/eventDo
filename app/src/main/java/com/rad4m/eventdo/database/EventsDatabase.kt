package com.rad4m.eventdo.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rad4m.eventdo.models.EventModel

@Database(
    entities = [EventModel::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EventsDatabase : RoomDatabase() {

    abstract fun eventsDao(): EventDao

    companion object {
        @Volatile
        private var instance: EventsDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context,
            EventsDatabase::class.java, "eventsDatabase.db"
        ).fallbackToDestructiveMigration()
            .build()
    }
}
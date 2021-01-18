package com.rad4m.eventdo.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rad4m.eventdo.models.EventModel

@Dao
interface EventDao : BaseDao<EventModel> {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvents(events: List<EventModel>)

    @Query("delete from eventTable ")
    suspend fun deleteEvents()

    @Query("select * from eventTable")
    fun getEvents(): LiveData<List<EventModel>>

    @Query("select * from eventTable where dtStart <:currentDate")
    fun getPastEvents(currentDate: String): LiveData<List<EventModel>>

    @Query("select * from eventTable where dtEnd >:currentDate")
    fun getUpcomingEvents(currentDate: String): LiveData<List<EventModel>>

    @Query("select * from eventTable where id =:id")
    fun getEvent(id: Int): EventModel

    @Query("select * from eventTable where id =:id")
    fun getEventById(id: Int): LiveData<EventModel>

    @Query("select * from eventTable where vendorId =:vendorId")
    fun getEventByIVendord(vendorId: String): EventModel

    @Query("select * from eventTable where localEventId != 0")
    fun getEventWithLocalEventId(): List<EventModel>?

    @Query("select * from eventTable order by :id desc limit 1")
    fun getLastEvent(id: Int): EventModel
}
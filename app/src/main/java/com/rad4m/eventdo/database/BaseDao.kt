package com.rad4m.eventdo.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

@Dao
interface BaseDao<T> {

    @Insert
    fun insert(`object`: T)

    @Update
    fun update(`object`: T)

    @Delete
    fun delete(`object`: T)

}
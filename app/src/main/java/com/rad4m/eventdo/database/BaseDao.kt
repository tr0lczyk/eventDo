package com.rad4m.eventdo.database

import androidx.room.*

@Dao
interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(`object`: T)

    @Update
    fun update(`object`: T)

    @Delete
    fun delete(`object`: T)

}
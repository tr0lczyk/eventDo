package com.rad4m.eventdo.database

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @TypeConverter
    fun intListToString(list: List<String>?): String? {
        val type = Types.newParameterizedType(
            List::class.java,
            String::class.javaObjectType
        )
        val adapter: JsonAdapter<List<String>> = moshi.adapter(type)
        return adapter.toJson(list)
    }

    @TypeConverter
    fun stringToIntList(value: String): List<String>? {
        val type = Types.newParameterizedType(
            List::class.java,
            String::class.javaObjectType
        )
        val adapter: JsonAdapter<List<String>> = moshi.adapter(type)
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun anyToString(any: Any?): String? =
        moshi.adapter(Any::class.java).toJson(any)

    @TypeConverter
    fun stringToAny(value: String): Any? =
        moshi.adapter(Any::class.java).fromJson(value)
}
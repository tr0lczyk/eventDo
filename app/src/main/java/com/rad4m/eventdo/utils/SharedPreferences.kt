package com.rad4m.eventdo.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.rad4m.eventdo.models.MyCalendar
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import javax.inject.Inject

class SharedPreferences @Inject constructor(application: Application, val moshi: Moshi) {

    private val SHARED_PREFS = "prefs"
    private val sharedPref: SharedPreferences = application.getSharedPreferences(
        SHARED_PREFS,
        Context.MODE_PRIVATE
    )

    fun save(KEY_NAME: String, text: String) {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putString(KEY_NAME, text)

        editor.apply()
    }

    fun save(KEY_NAME: String, value: Long) {
        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putLong(KEY_NAME, value)

        editor.apply()
    }

    fun save(KEY_NAME: String, status: Boolean) {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putBoolean(KEY_NAME, status)

        editor.apply()
    }

    fun save(KEY_NAME: String, list: List<MyCalendar>?) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        val type = Types.newParameterizedType(
            List::class.java,
            MyCalendar::class.javaObjectType
        )
        val adapter: JsonAdapter<List<MyCalendar>> = moshi.adapter(type)
        editor.putString(KEY_NAME, adapter.toJson(list))
        editor.apply()
    }

    fun saveCalendar(KEY_NAME: String, myCalendar: MyCalendar) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(KEY_NAME, moshi.adapter(MyCalendar::class.java).toJson(myCalendar))
        editor.apply()
    }

    fun saveCalendarId(KEY_NAME: String, calendarId: String){
        val editor = sharedPref.edit()
        editor.putString(KEY_NAME, calendarId)
        editor.apply()
    }

    fun saveCalendarName(KEY_NAME: String, calendarName: String){
        val editor = sharedPref.edit()
        editor.putString(KEY_NAME, calendarName)
        editor.apply()
    }

    fun getCalendarList(KEY_NAME: String): List<MyCalendar>? {
        val returnItem = sharedPref.getString(KEY_NAME, null)
        val type = Types.newParameterizedType(
            List::class.java,
            MyCalendar::class.javaObjectType
        )
        val adapter: JsonAdapter<List<MyCalendar>> = moshi.adapter(type)
        return adapter.fromJson(returnItem)
    }

    fun getCalendar(KEY_NAME: String): MyCalendar? {
        val returnItem = sharedPref.getString(KEY_NAME, null)
        return moshi.adapter(MyCalendar::class.java).fromJson(returnItem)
    }

    fun getValueString(KEY_NAME: String): String? {
        return sharedPref.getString(KEY_NAME, null)
    }

    fun getValueLong(KEY_NAME: String): Long? {
        return sharedPref.getLong(KEY_NAME, 0)
    }

    fun getValueBoolean(KEY_NAME: String): Boolean? {
        return sharedPref.getBoolean(KEY_NAME, false)
    }

    fun clearSharedPreference() {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }

    fun removeValue(KEY_NAME: String) {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.remove(KEY_NAME)
        editor.apply()
    }
}
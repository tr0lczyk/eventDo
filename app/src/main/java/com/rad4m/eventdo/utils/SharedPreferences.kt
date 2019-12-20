package com.rad4m.eventdo.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class SharedPreferences @Inject constructor(application: Application) {

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
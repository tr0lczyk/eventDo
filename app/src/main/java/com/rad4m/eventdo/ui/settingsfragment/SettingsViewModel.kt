package com.rad4m.eventdo.ui.settingsfragment

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rad4m.eventdo.R
import com.rad4m.eventdo.models.MyCalendar
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities.Companion.AUTO_ADD_EVENT
import com.rad4m.eventdo.utils.Utilities.Companion.NEW_EVENT_PAGE
import com.rad4m.eventdo.utils.Utilities.Companion.USER_CALENDAR_LIST
import com.rad4m.eventdo.utils.Utilities.Companion.USER_MAIN_CALENDAR_ID
import com.rad4m.eventdo.utils.Utilities.Companion.USER_MAIN_CALENDAR_NAME
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    val sharedPrefs: SharedPreferences, application: Application
) : AndroidViewModel(application) {

    val backIconTintColor = MutableLiveData<Int>()
    val backNavigation = MutableLiveData<Boolean>()
    val isNewEventPageOn = MutableLiveData<Boolean>()
    val isAutoAddEventOn = MutableLiveData<Boolean>()

    init {
        changeBackButtonColor(R.color.darkGray)
        isNewEventPageOn.value = sharedPrefs.getValueBoolean(NEW_EVENT_PAGE) ?: false
        isAutoAddEventOn.value = sharedPrefs.getValueBoolean(AUTO_ADD_EVENT) ?: false
    }

    fun startBackNavigation() {
        backNavigation.value = true
        changeBackButtonColor(R.color.blue)
    }

    fun stopBackNavigation() {
        backNavigation.value = false
        changeBackButtonColor(R.color.darkGray)
    }

    private fun changeBackButtonColor(colorInt: Int) {
        backIconTintColor.value = ContextCompat.getColor(getApplication(), colorInt)
    }

    private fun saveCalendarList(list: List<MyCalendar>) {
        sharedPrefs.save(USER_CALENDAR_LIST, list)
    }

    fun saveMainCalendar(calendar: MyCalendar) {
        sharedPrefs.saveCalendarId(USER_MAIN_CALENDAR_ID, calendar.calID)
        sharedPrefs.saveCalendarName(USER_MAIN_CALENDAR_NAME, calendar.calName)
    }

    fun searchForCal(currentCalName: String): MyCalendar {
        return sharedPrefs.getCalendarList(USER_CALENDAR_LIST)!!.filter {
            it.calName == currentCalName
        }[0]
    }

    private fun returnListOfCalNames(myCalendarsList: MutableList<MyCalendar>): MutableList<String> {
        val listOfCalendars = mutableListOf<String>()
        for (i in myCalendarsList) {
            listOfCalendars.add(i.calName)
        }
        return listOfCalendars
    }

    fun getCalendarsIds(activity: FragmentActivity): MutableList<String> {
        val projection = arrayOf("_id", "calendar_displayName")
        val calendars: Uri = Uri.parse("content://com.android.calendar/calendars")

        val contentResolver: ContentResolver = activity.contentResolver
        val managedCursor =
            contentResolver.query(calendars, projection, null, null, null)
        val mCalendars = mutableListOf<MyCalendar>()

        if (managedCursor!!.moveToFirst()) {
            var calName: String?
            var calID: String?
            var cont = 0
            val nameCol = managedCursor.getColumnIndex(projection[1])
            val idCol = managedCursor.getColumnIndex(projection[0])
            do {
                calName = managedCursor.getString(nameCol)
                Timber.i(calName)
                calID = managedCursor.getString(idCol)
                Timber.i(calID)
                mCalendars.add(MyCalendar(calName, calID))
                cont++
            } while (managedCursor.moveToNext())
            saveCalendarList(mCalendars)
            managedCursor.close()
        }
        return returnListOfCalNames(mCalendars)
    }
}
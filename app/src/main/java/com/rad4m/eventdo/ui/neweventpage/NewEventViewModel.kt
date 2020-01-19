package com.rad4m.eventdo.ui.neweventpage

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rad4m.eventdo.models.EventModel
import com.rad4m.eventdo.models.MyCalendar
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities
import timber.log.Timber
import javax.inject.Inject

class NewEventViewModel @Inject constructor(
    val sharedPrefs: SharedPreferences,
    application: Application
) :
    AndroidViewModel(application) {

    var startAddingNewEvent = MutableLiveData<Boolean>(false)
    var cancelAddingNewEvent = MutableLiveData<Boolean>(false)
    val selectedEvent = MutableLiveData<EventModel>()
    val eventTitle = MutableLiveData<String>()
    val eventLocation = MutableLiveData<String>()
    val eventShowAs = MutableLiveData<String>()
    val eventAllDay = MutableLiveData<Boolean>()

    fun setSelectedEvent(eventModel: EventModel) {
        selectedEvent.value = eventModel
        eventTitle.value = eventModel.title
        eventLocation.value = eventModel.location
    }

    fun addNewEventToCalendar() {
        startAddingNewEvent.value = true
    }

    fun stopAddingNewEvent() {
        startAddingNewEvent.value = false
    }

    fun startCancelling() {
        cancelAddingNewEvent.value = true
    }

    fun stopCancelling() {
        cancelAddingNewEvent.value = false
    }

    private fun saveCalendarList(list: List<MyCalendar>) {
        sharedPrefs.save(Utilities.USER_CALENDAR_LIST, list)
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
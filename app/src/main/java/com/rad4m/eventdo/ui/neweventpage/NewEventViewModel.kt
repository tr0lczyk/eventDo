package com.rad4m.eventdo.ui.neweventpage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rad4m.eventdo.models.EventModel
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities
import javax.inject.Inject

class NewEventViewModel @Inject constructor(
    val sharedPrefs: SharedPreferences,
    application: Application
) :
    AndroidViewModel(application) {

    val startAddingNewEvent = MutableLiveData<Boolean>(false)
    val cancelAddingNewEvent = MutableLiveData<Boolean>(false)
    val selectedEvent = MutableLiveData<EventModel>()
    val eventTitle = MutableLiveData<String>()
    val eventLocation = MutableLiveData<String>()
    val eventShowAs = MutableLiveData<String>()
    val eventAllDay = MutableLiveData<Boolean>()
    val eventCalendar = MutableLiveData<String>()
    val eventStartDate = MutableLiveData<String>()
    val eventEndDate = MutableLiveData<String>()
    val eventUrl = MutableLiveData<String>()
    val eventNotes = MutableLiveData<String>()
    val openTimeDialogs = MutableLiveData<Boolean>()
    val endTimeDialogs = MutableLiveData<Boolean>()

    fun setSelectedEvent(eventModel: EventModel) {
        selectedEvent.value = eventModel
        eventTitle.value = eventModel.title
        eventLocation.value = eventModel.location
        eventStartDate.value = eventModel.dtStart
        eventEndDate.value = eventModel.dtEnd
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

    fun startTimeDialogs() {
        openTimeDialogs.value = true
    }

    fun stopStartTimeDialogs() {
        openTimeDialogs.value = false
    }

    fun endTimeDialogs() {
        endTimeDialogs.value = true
    }

    fun stopEndTimeDialogs() {
        endTimeDialogs.value = false
    }

    fun searchForCal(currentCalName: String) {
        eventCalendar.value = sharedPrefs.getCalendarList(Utilities.USER_CALENDAR_LIST)!!.filter {
            it.calName == currentCalName
        }[0].calID
    }
}
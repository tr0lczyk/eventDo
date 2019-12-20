package com.rad4m.eventdo.ui.neweventpage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rad4m.eventdo.models.EventModel
import javax.inject.Inject

class NewEventViewModel @Inject constructor(application: Application) :
    AndroidViewModel(application) {

    var startAddingNewEvent = MutableLiveData<Boolean>(false)
    var cancelAddingNewEvent = MutableLiveData<Boolean>(false)
    val selectedEvent = MutableLiveData<EventModel>()

    fun setSelectedEvent(eventModel: EventModel){
        selectedEvent.value = eventModel
    }

    fun addNewEventToCalendar() {
        startAddingNewEvent.value = true
    }

    fun stopAddingNewEvent() {
        startAddingNewEvent.value = false
    }

    fun startCancelling(){
        cancelAddingNewEvent.value = true
    }

    fun stopCancelling(){
        cancelAddingNewEvent.value = false
    }
}
package com.rad4m.eventdo.ui.mainfragment

import android.app.Application
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.rad4m.eventdo.R
import com.rad4m.eventdo.database.EventsDatabase
import com.rad4m.eventdo.models.DataItem
import com.rad4m.eventdo.models.EventModel
import com.rad4m.eventdo.models.Result
import com.rad4m.eventdo.networking.EventDoRepository
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities.Companion.USER_LAST_DATE
import com.rad4m.eventdo.utils.Utilities.Companion.USER_LOGOUT
import com.rad4m.eventdo.utils.Utilities.Companion.convertDateToString
import com.rad4m.eventdo.utils.Utilities.Companion.convertDateToStringWithZ
import com.rad4m.eventdo.utils.Utilities.Companion.convertStringToDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val repository: EventDoRepository,
    private val database: EventsDatabase,
    val sharedPrefs: SharedPreferences,
    application: Application
) : AndroidViewModel(application) {

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val upcomingTextColor = MutableLiveData<Int>()
    val pastTextColor = MutableLiveData<Int>()
    val emptyListText = MutableLiveData<String>()
    val swipeRefreshing = MutableLiveData<Boolean>()
    private val showUpcomingEvents = MutableLiveData<Boolean>()
    val eventList: LiveData<List<EventModel>> = Transformations.switchMap(showUpcomingEvents) {
        when (it) {
            true -> database.eventsDao().getUpcomingEvents(convertDateToStringWithZ(Date()))
            false -> database.eventsDao().getPastEvents(convertDateToStringWithZ(Date()))
        }
    }
    val emptyListInfoVisibility = MutableLiveData<Int>(View.GONE)
    val dataItemList = MutableLiveData<List<DataItem>>()
    val selectedEvent = MutableLiveData<EventModel>()

    init {
        swipeRefreshing.value = false
        upcomingButton()
        downloadEvents()
    }

    fun navigateToSelectedEvent(event: EventModel) {
        selectedEvent.value = event
    }

    fun upcomingButton() {
        showUpcomingEvents.value = true
        changeUpcomingColor(R.color.blue)
        changeEmptyListContent(R.string.you_have_no_events_upcoming)
        changePastColor(R.color.darkGray)
    }

    fun pastButton() {
        showUpcomingEvents.value = false
        changePastColor(R.color.blue)
        changeEmptyListContent(R.string.you_have_no_events_past)
        changeUpcomingColor(R.color.darkGray)
    }

    private fun changeUpcomingColor(colorInt: Int) {
        upcomingTextColor.value = ContextCompat.getColor(getApplication(), colorInt)
    }

    private fun changePastColor(colorInt: Int) {
        pastTextColor.value = ContextCompat.getColor(getApplication(), colorInt)
    }

    private fun changeEmptyListContent(text: Int) {
        emptyListText.value = getApplication<Application>().getString(text)
    }

    fun finishSelectedEvent() {
        selectedEvent.value = null
    }

    fun downloadEvents() {
        viewModelScope.launch {
            when (val response = repository.getEventsList()) {
                is Result.Success -> saveEvents(response.data!!.result)
                is Result.Failure -> Timber.i("failure")
                else -> Timber.i("else")
            }
        }
        sharedPrefs.save(USER_LAST_DATE, convertDateToString(Date()))
        swipeRefreshing.value = false
    }

    private suspend fun saveEvents(data: List<EventModel>) {
        database.eventsDao().insertEvents(data)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private fun checkEmptyInfoVisibility() {
        if (dataItemList.value!!.isEmpty()) {
            emptyListInfoVisibility.value = View.VISIBLE
        } else {
            emptyListInfoVisibility.value = View.GONE
        }
    }

    fun convertEventsToDataItems(list: List<EventModel>) {
        val temporaryList: MutableList<DataItem> = mutableListOf()
        for ((index, event) in list.withIndex()) {
            if (index == 0) {
                val firstHeader = DataItem.DataItemHeader(event.dtStart!!)
                temporaryList.add(firstHeader)
                val firstItem = DataItem.DataItemEventModel(event)
                temporaryList.add((firstItem))
            } else {
                val previousDate =
                    TimeUnit.MILLISECONDS.toDays(convertStringToDate(list[index - 1].dtStart!!).time)
                val currentDate =
                    TimeUnit.MILLISECONDS.toDays(convertStringToDate(event.dtStart!!).time)
                if (previousDate < currentDate) {
                    val currentHeader = DataItem.DataItemHeader(event.dtStart)
                    temporaryList.add(currentHeader)
                    val currentItem = DataItem.DataItemEventModel(event)
                    temporaryList.add(currentItem)
                } else {
                    val currentItem = DataItem.DataItemEventModel(event)
                    temporaryList.add(currentItem)
                }
            }
        }
        dataItemList.value = temporaryList
        checkEmptyInfoVisibility()
    }
}
package com.rad4m.eventdo.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import com.rad4m.eventdo.EventDoApplication
import com.rad4m.eventdo.R
import com.rad4m.eventdo.database.EventsDatabase
import com.rad4m.eventdo.models.EventModel
import com.rad4m.eventdo.models.MyCalendar
import com.rad4m.eventdo.utils.Utilities.Companion.NEW_CURSOR_EVENT
import com.rad4m.eventdo.utils.Utilities.Companion.NEW_EVENT_ID
import com.rad4m.eventdo.utils.Utilities.Companion.USER_MAIN_CALENDAR_ID
import com.rad4m.eventdo.utils.Utilities.Companion.USER_MAIN_CALENDAR_NAME
import com.rad4m.eventdo.utils.Utilities.Companion.convertStringToDate
import com.rad4m.eventdo.utils.Utilities.Companion.toastMessage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.TimeZone

class UtilitiesCalendar {

    companion object {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val application = EventDoApplication.instance
        val sharedPrefs = SharedPreferences(application, moshi)
        val database = EventsDatabase.invoke(application)
        private val utilitiesCalendarJob = Job()
        private val utilitiesCalendarScope = CoroutineScope(Dispatchers.IO + utilitiesCalendarJob)

        fun openCalendar(
            activity: FragmentActivity,
            event: EventModel
        ) {
            val builder = CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
            ContentUris.appendId(builder, convertStringToDate(event.dtStart!!).time)
            val insertCalendarIntent = Intent(Intent.ACTION_VIEW, builder.build())
            activity.startActivity(insertCalendarIntent)
        }

        fun saveEventToCalendar(
            event: EventModel,
            activity: FragmentActivity,
            calendarId: String?
        ) {
            val insertCalendarIntent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, event.title)
                .putExtra(
                    CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                    convertStringToDate(event.dtStart!!).time
                )
                .putExtra(
                    CalendarContract.EXTRA_EVENT_END_TIME,
                    convertStringToDate(event.dtEnd!!).time
                )
                .putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
                .putExtra(CalendarContract.Events.DESCRIPTION, event.description)
            if (!calendarId.isNullOrEmpty()) {
                insertCalendarIntent.putExtra(
                    CalendarContract.Events.CALENDAR_ID,
                    calendarId.toInt()
                )
            } else {
                insertCalendarIntent.putExtra(
                    CalendarContract.Events.CALENDAR_ID,
                    getCalendarId(application)
                )
            }
            sharedPrefs.save(NEW_CURSOR_EVENT, getNewEventId(activity.contentResolver).toString())
            sharedPrefs.save(NEW_EVENT_ID, event.id.toString())
            activity.startActivity(insertCalendarIntent)
        }

        private fun calendarIdKnown(values: ContentValues, event: EventModel, calendarId: String) {
            values.apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId.toInt())
                put(
                    CalendarContract.Events.DTSTART,
                    convertStringToDate(event.dtStart!!).time
                )
                put(
                    CalendarContract.Events.DTEND,
                    convertStringToDate(event.dtEnd!!).time
                )
                put(CalendarContract.Events.TITLE, event.title)
                put(CalendarContract.Events.DESCRIPTION, event.description)
                put(CalendarContract.Events.EVENT_LOCATION, event.location)
                put(CalendarContract.Events.EVENT_TIMEZONE, "${TimeZone.getDefault()}")
                put(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
            }
        }

        private fun calendarIdDontKnown(values: ContentValues, event: EventModel) {
            values.apply {
                put(CalendarContract.Events.CALENDAR_ID, getCalendarId(application))
                put(
                    CalendarContract.Events.DTSTART,
                    convertStringToDate(event.dtStart!!).time
                )
                put(
                    CalendarContract.Events.DTEND,
                    convertStringToDate(event.dtEnd!!).time
                )
                put(CalendarContract.Events.TITLE, event.title)
                put(CalendarContract.Events.DESCRIPTION, event.description)
                put(CalendarContract.Events.EVENT_LOCATION, event.location)
                put(CalendarContract.Events.EVENT_TIMEZONE, "${TimeZone.getDefault()}")
                put(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
            }
        }

        @SuppressLint("MissingPermission")
        fun saveCalEventContentResolver(
            event: EventModel,
            activity: FragmentActivity
        ) {
            val values = ContentValues()
            if (sharedPrefs.getValueString(
                    USER_MAIN_CALENDAR_ID
                ).isNullOrEmpty()
            ) {
                calendarIdDontKnown(values, event)
            } else {
                calendarIdKnown(
                    values, event, sharedPrefs.getValueString(
                        USER_MAIN_CALENDAR_ID
                    )!!
                )
            }
            val uri: Uri =
                activity.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)!!
            val eventID: Long = uri.lastPathSegment!!.toLong()
            event.apply {
                this.localEventId = eventID
            }
            updateEvent(event)
            Timber.i("eventr added")
        }

        private fun isEventInCal(id: Long): Boolean {
            val event =
                ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id)
            val cursor: Cursor? = application.contentResolver.query(event, null, null, null, null)
            return if (cursor != null && cursor.count == 1) { //the event exists?
                if (cursor.moveToFirst() && !TextUtils.equals(
                        cursor.getString(
                            cursor.getColumnIndex(
                                "deleted"
                            )
                        ), "1"
                    )
                ) {
                    cursor.close()
                    true
                } else {
                    false
                }
            } else false
        }

        fun deleteCalendarEntry(activity: FragmentActivity, event: EventModel) {
            val deleteUri: Uri =
                ContentUris.withAppendedId(
                    CalendarContract.Events.CONTENT_URI,
                    event.localEventId!!
                )
            activity.contentResolver.delete(deleteUri, null, null)
            toastMessage(activity, R.string.event_deleted)
            event.apply {
                this.localEventId = null
            }
            updateEvent(event)
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

        private fun getCalendarId(application: Application): Int {
            val projection = arrayOf("_id", "calendar_displayName")
            val calendars: Uri = Uri.parse("content://com.android.calendar/calendars")
            val contentResolver: ContentResolver = application.contentResolver
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
            return mCalendars[mCalendars.size - 1].calID.toInt()
        }

        fun saveMainCalendar(calendar: MyCalendar) {
            sharedPrefs.save(USER_MAIN_CALENDAR_NAME, calendar.calName)
            sharedPrefs.save(USER_MAIN_CALENDAR_ID, calendar.calID)
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

        fun searchForCal(currentCalName: String): MyCalendar {
            return sharedPrefs.getCalendarList(Utilities.USER_CALENDAR_LIST)!!.filter {
                it.calName == currentCalName
            }[0]
        }

        fun verifyIfEventDeleted() {
            utilitiesCalendarScope.launch {
                if (!database.eventsDao().getEventWithLocalEventId().isNullOrEmpty()) {
                    checkEventsInDatabase()
                }
            }
        }

        private fun checkEventsInDatabase() {
            for (i in database.eventsDao().getEventWithLocalEventId()!!) {
                if (!isEventInCal(i.localEventId!!)) {
                    updateEvent(i.apply {
                        i.localEventId = null
                    })
                }
            }
        }

        fun checkIfEventExist(event: EventModel) {
            if (!isEventInCal(event.id)) {
                updateEvent(event.apply {
                    event.localEventId = null
                })
            }
        }

        private fun updateEvent(event: EventModel) {
            utilitiesCalendarScope.launch {
                database.eventsDao().update(event)
            }
        }

        private fun getEventImplCursorId(eventId: Int, cursorId: Long) {
            utilitiesCalendarScope.launch {
                val newEvent = database.eventsDao().getEvent(eventId).apply {
                    this.localEventId = cursorId
                }
                updateEvent(newEvent)
                sharedPrefs.removeValue(NEW_EVENT_ID)
            }
        }

        @SuppressLint("MissingPermission")
        fun getNewEventId(cr: ContentResolver): Long {
            val cursor = cr.query(
                CalendarContract.Events.CONTENT_URI,
                arrayOf("MAX(_id) as max_id"),
                null,
                null,
                "_id"
            )
            cursor!!.moveToFirst()
            val max_val = cursor.getLong(cursor.getColumnIndex("max_id"))
            cursor.close()
            return max_val + 1
        }

        @SuppressLint("MissingPermission")
        fun getLastEventId(cr: ContentResolver): Long {
            val cursor = cr.query(
                CalendarContract.Events.CONTENT_URI,
                arrayOf("MAX(_id) as max_id"),
                null,
                null,
                "_id"
            )
            cursor!!.moveToFirst()
            val id = cursor.getLong(cursor.getColumnIndex("max_id"))
            cursor.close()
            return id
        }

        fun verifyLastIntentEvent(activity: FragmentActivity) {
            val prevId = getLastEventId(activity.contentResolver)
            when (sharedPrefs.getValueString(NEW_CURSOR_EVENT)?.toLong()) {
                prevId -> getEventImplCursorId(
                    sharedPrefs.getValueString(
                        NEW_EVENT_ID
                    )!!.toInt(), prevId
                )
            }
            sharedPrefs.removeValue(NEW_CURSOR_EVENT)
        }
    }
}
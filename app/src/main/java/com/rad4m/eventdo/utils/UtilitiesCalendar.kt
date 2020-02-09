package com.rad4m.eventdo.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.fragment.app.FragmentActivity
import com.rad4m.eventdo.EventDoApplication
import com.rad4m.eventdo.R
import com.rad4m.eventdo.models.EventIdTitle
import com.rad4m.eventdo.models.EventModel
import com.rad4m.eventdo.models.MyCalendar
import com.rad4m.eventdo.utils.Utilities.Companion.USER_MAIN_CALENDAR_ID
import com.rad4m.eventdo.utils.Utilities.Companion.USER_MAIN_CALENDAR_NAME
import com.rad4m.eventdo.utils.Utilities.Companion.convertStringToDate
import com.rad4m.eventdo.utils.Utilities.Companion.toastMessage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import timber.log.Timber
import java.util.TimeZone

class UtilitiesCalendar {

    companion object {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val application = EventDoApplication.instance
        val sharedPrefs = SharedPreferences(application, moshi)

        fun openCalendar(
            activity: FragmentActivity,
            event: EventModel
        ) {
            val builder = CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
            ContentUris.appendId(builder, convertStringToDate(event.dtStart!!).time)
            val insertCalendarIntent = Intent()
                .setData(builder.build())
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
                    getCalendarId(application).toInt()
                )
            }
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
            activity.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)!!
            Timber.i("eventr added")
        }

        @SuppressLint("MissingPermission")
        fun getEventIdList(activity: FragmentActivity): ArrayList<EventIdTitle> {
            val eventIdTitleList = ArrayList<EventIdTitle>()
            val EVENT_PROJECTION: Array<String> = arrayOf(
                CalendarContract.Events._ID, // 0
                CalendarContract.Events.TITLE  // 1
            )
            val PROJECTION_EVENT_ID_INDEX: Int = 0
            val PROJECTION_TITLE_INDEX: Int = 1

            val cursor = activity.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                EVENT_PROJECTION,
                "",
                arrayOf(),
                null
            )
            cursor!!.let {
                while (it.moveToNext()) {
                    val eventId = it.getLong(PROJECTION_EVENT_ID_INDEX)
                    val title = it.getString(PROJECTION_TITLE_INDEX)
                    eventIdTitleList.add(EventIdTitle(eventId, title))
                }
            }
            cursor.close()
            Timber.i("eventr list renewed")
            return eventIdTitleList
        }

        fun deleteCalendarEntry(activity: FragmentActivity, entryID: Long) {
            val deleteUri: Uri =
                ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, entryID)
            activity.contentResolver.delete(deleteUri, null, null)
            Timber.i("$entryID eventr deleted")
            toastMessage(activity, R.string.event_deleted)
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

        private fun getCalendarId(application: Application): String {
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
            return mCalendars[mCalendars.size - 1].calID
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
                listOfCalendars.add(i.calName!!)
            }
            return listOfCalendars
        }

        fun searchForCal(currentCalName: String): MyCalendar {
            return sharedPrefs.getCalendarList(Utilities.USER_CALENDAR_LIST)!!.filter {
                it.calName == currentCalName
            }[0]
        }
    }
}
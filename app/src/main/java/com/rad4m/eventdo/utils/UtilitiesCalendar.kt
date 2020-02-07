package com.rad4m.eventdo.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ComponentName
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.rad4m.eventdo.EventDoApplication
import com.rad4m.eventdo.R
import com.rad4m.eventdo.models.EventIdTitle
import com.rad4m.eventdo.models.EventModel
import com.rad4m.eventdo.models.MyCalendar
import com.rad4m.eventdo.utils.Utilities.Companion.EVENT_ID_TITLE
import com.rad4m.eventdo.utils.Utilities.Companion.convertStringToDate
import com.rad4m.eventdo.utils.Utilities.Companion.toastMessage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
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
            val cn =
                ComponentName("com.google.android.calendar", "com.android.calendar.LaunchActivity")
            val insertCalendarIntent = Intent()
                .setData(builder.build())
                .setComponent(cn)
            activity.startActivity(insertCalendarIntent)
        }

        private fun calendarIdKnown(values: ContentValues, event: EventModel, calendarId: String) {
            values.apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
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

//        private fun calendarIdDontKnown(values: ContentValues, event: EventModel) {
//            values.apply {
//                put(CalendarContract.Events.CALENDAR_ID, getCalendarId(application))
//                put(
//                    CalendarContract.Events.DTSTART,
//                    convertStringToDate(event.dtStart!!).time
//                )
//                put(
//                    CalendarContract.Events.DTEND,
//                    convertStringToDate(event.dtEnd!!).time
//                )
//                put(CalendarContract.Events.TITLE, event.title)
//                put(CalendarContract.Events.DESCRIPTION, event.description)
//                put(CalendarContract.Events.EVENT_LOCATION, event.location)
//                put(CalendarContract.Events.EVENT_TIMEZONE, "${TimeZone.getDefault()}")
//                put(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
//            }
//        }

        @SuppressLint("MissingPermission")
        fun saveCalEventContentResolver(
            event: EventModel,
            activity: FragmentActivity,
            calendarId: String?
        ) {
            val values = ContentValues()
            if (calendarId.isNullOrEmpty()) {
//                calendarIdDontKnown(values, event)
            } else {
                calendarIdKnown(values, event, calendarId)
            }
            activity.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)!!
            sharedPrefs.removeValue(EVENT_ID_TITLE)
            sharedPrefs.saveEventItTitleList(EVENT_ID_TITLE, getEventIdList(activity))
        }

        @SuppressLint("MissingPermission")
        fun saveEventFromNewEventPage(
            event: EventModel,
            activity: FragmentActivity,
            calendarId: String?
        ) {
            val values = ContentValues()
            if (calendarId.isNullOrEmpty()) {
//                calendarIdDontKnown(values, event)
            } else {
                calendarIdKnown(values, event, calendarId)
            }
            activity.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)!!
            sharedPrefs.removeValue(EVENT_ID_TITLE)
            sharedPrefs.saveEventItTitleList(EVENT_ID_TITLE, getEventIdList(activity))
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
            return eventIdTitleList
        }

        fun deleteCalendarEntry(activity: FragmentActivity, entryID: Long) {
            val deleteUri: Uri =
                ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, entryID)
            activity.contentResolver.delete(deleteUri, null, null)
            sharedPrefs.removeValue(EVENT_ID_TITLE)
            sharedPrefs.saveEventItTitleList(EVENT_ID_TITLE, getEventIdList(activity))
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
            sharedPrefs.saveCalendarId(Utilities.USER_MAIN_CALENDAR_ID, calendar.calID)
            sharedPrefs.saveCalendarName(Utilities.USER_MAIN_CALENDAR_NAME, calendar.calName)
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

        private fun getDateString(
            list: MutableList<Int>
        ): String? {
            val calendar = Calendar.getInstance()
            calendar.set(list[0], list[1], list[2], list[3], list[4])
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            return dateFormat.format(calendar.time)
        }

        fun dataPicker(
            activity: FragmentActivity,
            customDate: MutableLiveData<String>
        ) {
            val calendar = Calendar.getInstance()
            calendar.time = convertStringToDate(customDate.value!!)
            val year = calendar[Calendar.YEAR]
            val month = calendar[Calendar.MONTH]
            val day = calendar[Calendar.DAY_OF_MONTH]
            val dateDataList: MutableList<Int> = mutableListOf()
            val datePickerDialog = DatePickerDialog(
                activity,
                DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    dateDataList.add(year)
                    dateDataList.add(monthOfYear + 1)
                    dateDataList.add(dayOfMonth)
                    timePicker(activity, dateDataList, customDate)
                },
                year, month, day
            )
            datePickerDialog.show()
            datePickerDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(activity, R.color.darkRed))
            datePickerDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(activity, R.color.darkRed))
        }

        private fun timePicker(
            activity: FragmentActivity,
            dateDataList: MutableList<Int>,
            customDate: MutableLiveData<String>
        ) {
            val calendar = Calendar.getInstance()
            calendar.time = convertStringToDate(customDate.value!!)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minutem = calendar.get(Calendar.MINUTE)
            val tpd = TimePickerDialog(
                activity,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    dateDataList.add(hourOfDay)
                    dateDataList.add(minute)
                    customDate.value = getDateString(dateDataList)
                },
                hour,
                minutem, true
            )
            tpd.show()
            tpd.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(activity, R.color.darkRed))
            tpd.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(activity, R.color.darkRed))
        }
    }
}
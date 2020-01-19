package com.rad4m.eventdo.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.provider.CalendarContract
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.rad4m.eventdo.R
import com.rad4m.eventdo.models.EventModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class Utilities {

    companion object {

        const val USER_ID = "userId"
        const val USER_TOKEN = "token"
        const val USER_NUMBER = "phoneNumber"
        const val USER_LAST_DATE = "lastDate"
        const val ITEM_VIEW_TYPE_HEADER = 0
        const val ITEM_VIEW_TYPE_ITEM = 1
        const val NEW_EVENT_PAGE = "newEventPage"
        const val AUTO_ADD_EVENT = "autoAddEvent"
        const val USER_CALENDAR_LIST = "userCalendarList"
        const val USER_MAIN_CALENDAR_ID = "userMainCalendarId"
        const val USER_MAIN_CALENDAR_NAME = "userMainCalendarName"
        const val USER_LOGOUT = "userLogout"

        fun convertDateToString(date: Date): String {
            val originalFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            return originalFormat.format(date)
        }

        fun convertDateToStringWithZ(date: Date): String {
            val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            return originalFormat.format(date)
        }

        fun showKeyboard(activity: FragmentActivity) {
            val imm =
                activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }

        fun convertStringToDate(text: String): Date {
            return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(text)
        }

        private fun addCalendarId(
            calendarIntent: Intent,
            calendarId: String
        ) {
            calendarIntent.putExtra(
                CalendarContract.Events.CALENDAR_ID,
                calendarId
            )
        }

        fun saveEventToCalendar(
            event: EventModel,
            activity: FragmentActivity,
            calendarId: String?
        ) {
            val insertCalendarIntent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, event.title)
                .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
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
                addCalendarId(insertCalendarIntent, calendarId)
            }
            activity.startActivity(insertCalendarIntent)
        }

        private fun calendarIdKnown(values: ContentValues, event: EventModel, calendarId: String) {
            values.apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.DTSTART, convertStringToDate(event.dtStart!!).time)
                put(CalendarContract.Events.DTEND, convertStringToDate(event.dtEnd!!).time)
                put(CalendarContract.Events.TITLE, event.title)
                put(CalendarContract.Events.DESCRIPTION, event.description)
                put(CalendarContract.Events.EVENT_LOCATION, event.location)
                put(CalendarContract.Events.EVENT_TIMEZONE, "${TimeZone.getDefault()}")
                put(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
            }
        }

        private fun calendarIdDontKnown(values: ContentValues, event: EventModel) {
            values.apply {
                put(CalendarContract.Events.CALENDAR_ID, 1)
                put(CalendarContract.Events.DTSTART, convertStringToDate(event.dtStart!!).time)
                put(CalendarContract.Events.DTEND, convertStringToDate(event.dtEnd!!).time)
                put(CalendarContract.Events.TITLE, event.title)
                put(CalendarContract.Events.DESCRIPTION, event.description)
                put(CalendarContract.Events.EVENT_LOCATION, event.location)
                put(CalendarContract.Events.EVENT_TIMEZONE, "${TimeZone.getDefault()}")
                put(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
            }
        }

        fun saveCalEventContentResolver(
            event: EventModel,
            activity: FragmentActivity,
            calendarId: String?
        ) {
            val values = ContentValues()
            if (calendarId.isNullOrEmpty()) {
                calendarIdDontKnown(values, event)
            } else {
                calendarIdKnown(values, event, calendarId)
            }
            activity.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)!!
            Toast.makeText(activity, activity.getString(R.string.event_saved), Toast.LENGTH_LONG)
                .show()
        }

        fun showDialog(
            activity: FragmentActivity,
            message: String,
            title: String,
            yesButtonTitle: String,
            yesButton: () -> Unit,
            noButtonTitle: String
        ) {
            val dialog =
                AlertDialog.Builder(activity).setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(yesButtonTitle) { dialog, _ ->
                        yesButton()
                        dialog.dismiss()
                    }
                    .setNegativeButton(noButtonTitle) { dialog, _ ->
                        dialog.cancel()
                    }
                    .setCancelable(true)
                    .create()
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(activity.getColor(R.color.darkRed))
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(activity.getColor(R.color.darkRed))
        }

        fun Activity.makeStatusBarTransparent() {
            window.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }
                statusBarColor = Color.TRANSPARENT
            }
        }
    }
}
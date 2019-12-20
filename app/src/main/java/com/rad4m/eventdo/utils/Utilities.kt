package com.rad4m.eventdo.utils

import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity
import java.text.SimpleDateFormat
import java.util.Date

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

        fun convertDateToString(date: Date): String {
            val originalFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            return originalFormat.format(date)
        }

        fun convertDateToStringWithZ(date: Date): String {
            val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
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
    }
}
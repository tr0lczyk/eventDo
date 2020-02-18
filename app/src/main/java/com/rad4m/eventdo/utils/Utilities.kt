package com.rad4m.eventdo.utils

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.rad4m.eventdo.MainActivity
import com.rad4m.eventdo.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

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
        const val USER_MAIN_CALENDAR = "userMainCalendarName"
        const val USER_LOGOUT = "userLogout"
        const val CHANNEL_ID = "eventDoChannelId"
        const val CHANNEL_NAME = "eventDoChannelName"
        const val CHANNEL_DESC = "Notification channel"
        const val FIREBASE_TOKEN = "tokenFirebase"
        const val DEVICE_ID = "deviceId"
        const val NEW_CURSOR_EVENT = "newCursorEvent"
        const val NEW_EVENT_ID = "newEventId"

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
                .setTextColor(ContextCompat.getColor(activity, R.color.darkRed))
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(activity, R.color.darkRed))
        }

        fun showInformingDialog(
            activity: FragmentActivity,
            message: String,
            title: String
        ) {
            val dialog =
                AlertDialog.Builder(activity).setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(activity.getString(R.string.ok)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setCancelable(true)
                    .create()
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(activity, R.color.darkRed))
        }

        fun Activity.makeStatusBarTransparent() {
            window.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }
                statusBarColor = Color.TRANSPARENT
            }
        }

        fun Activity.makeStatusBarNotTransparent() {
            window.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }
                statusBarColor = Color.WHITE
            }
        }

        fun getUuidId(): String {
            return UUID.randomUUID().toString()
        }

        fun appInForeground(context: Context): Boolean {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningAppProcesses =
                activityManager.runningAppProcesses ?: return false
            return runningAppProcesses.any {
                it.processName == context.packageName &&
                        it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            }
        }

        fun isValidEmail(target: CharSequence): Boolean {
            return !TextUtils.isEmpty(target) &&
                    android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }

        fun toastMessage(context: Context, toastText: Int) {
            Toast.makeText(
                context,
                context.getString(toastText),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    object NotificationHelper {

        fun displayNotification(context: Context, title: String, body: String) {

            val intent = Intent(context, MainActivity::class.java)

            val pendingIntent = PendingIntent.getActivity(
                context,
                100,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )

            val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notifications_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            val mNotificationMgr = NotificationManagerCompat.from(context)
            mNotificationMgr.notify(1, mBuilder.build())
        }
    }
}
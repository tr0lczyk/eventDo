package com.rad4m.eventdo.utils

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rad4m.eventdo.utils.Utilities.Companion.appInForeground
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.sharedPrefs
import timber.log.Timber

class MyFirebaseMessagingService :
    FirebaseMessagingService() {


    override fun onNewToken(p0: String) {
        sharedPrefs.save(Utilities.FIREBASE_TOKEN, p0)
        Timber.d("======$p0")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (!appInForeground(applicationContext)) {
            Utilities.NotificationHelper.displayNotification(
                applicationContext,
                remoteMessage.notification!!.title!!,
                remoteMessage.notification!!.body!!
            )
        }
    }
}
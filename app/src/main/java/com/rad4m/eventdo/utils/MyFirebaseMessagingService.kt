package com.rad4m.eventdo.utils

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rad4m.eventdo.utils.Utilities.Companion.appInForeground

class MyFirebaseMessagingService : FirebaseMessagingService() {

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
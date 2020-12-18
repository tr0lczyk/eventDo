package com.rad4m.eventdo.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rad4m.eventdo.EventDoApplication
import com.rad4m.eventdo.models.EventModel
import com.rad4m.eventdo.utils.Utilities.Companion.NOTIFICATION_EVENT_DESCRIPTION
import com.rad4m.eventdo.utils.Utilities.Companion.NOTIFICATION_EVENT_DTEND
import com.rad4m.eventdo.utils.Utilities.Companion.NOTIFICATION_EVENT_DTSTART
import com.rad4m.eventdo.utils.Utilities.Companion.NOTIFICATION_EVENT_ID
import com.rad4m.eventdo.utils.Utilities.Companion.NOTIFICATION_EVENT_LOCATION
import com.rad4m.eventdo.utils.Utilities.Companion.NOTIFICATION_EVENT_TITLE
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.application
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.saveCalEventContentResolverBroadcast

class EventDoBroadcastReciever : BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {

        val eventModel = EventModel(
            id = p1!!.getStringExtra(NOTIFICATION_EVENT_ID).toLong(),
            title = p1.getStringExtra(NOTIFICATION_EVENT_TITLE),
            dtStart = p1.getStringExtra(NOTIFICATION_EVENT_DTSTART),
            dtEnd = p1.getStringExtra(NOTIFICATION_EVENT_DTEND),
            location = p1.getStringExtra(NOTIFICATION_EVENT_LOCATION)
        )

        saveCalEventContentResolverBroadcast(eventModel, EventDoApplication.instance)
    }
}
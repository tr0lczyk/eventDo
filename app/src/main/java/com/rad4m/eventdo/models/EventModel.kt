package com.rad4m.eventdo.models

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rad4m.eventdo.R
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "eventTable")
@Parcelize
data class EventModel(
    @Json(name = "createdBy")
    var createdBy: Int? = 0,
    @Json(name = "createdDate")
    var createdDate: String? ="",
    @Json(name = "description")
    var description: String? ="",
    @Json(name = "dtEnd")
    var dtEnd: String? ="",
    @Json(name = "dtStart")
    var dtStart: String? ="",
    @Json(name = "duration")
    var duration: String? = "00:00:00",
    @Json(name = "end")
    var end: String? ="",
    @Json(name = "geographicLocation")
    var geographicLocation: String? = null,
    @PrimaryKey
    @Json(name = "id")
    var id: Long =0,
    @Json(name = "isActive")
    var isActive: Boolean? = false,
    @Json(name = "isAllDay")
    var isAllDay: Boolean? = false,
    @Json(name = "location")
    var location: String? ="",
    @Json(name = "modifiedBy")
    var modifiedBy: Int?=0,
    @Json(name = "modifiedDate")
    var modifiedDate: String? ="",
    @Json(name = "phoneNumber")
    var phoneNumber: String? ="",
    @Json(name = "start")
    var start: String? ="",
    @Json(name = "status")
    var status: String? = null,
    @Json(name = "title")
    var title: String? ="",
    @Json(name = "vendorId")
    var vendorId: Int? = 0,
    var localEventId: Long? = null,
    var codedImage: String?= ""
) : Parcelable
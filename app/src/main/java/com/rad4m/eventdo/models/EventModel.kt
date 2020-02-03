package com.rad4m.eventdo.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "eventTable")
@Parcelize
data class EventModel(
    @Json(name = "createdBy")
    val createdBy: Int?,
    @Json(name = "createdDate")
    val createdDate: String?,
    @Json(name = "description")
    val description: String?,
    @Json(name = "dtEnd")
    val dtEnd: String?,
    @Json(name = "dtStart")
    val dtStart: String?,
    @Json(name = "duration")
    val duration: String?,
    @Json(name = "end")
    val end: String?,
    @Json(name = "geographicLocation")
    val geographicLocation: String?,
    @PrimaryKey
    @Json(name = "id")
    val id: Long,
    @Json(name = "isActive")
    val isActive: Boolean?,
    @Json(name = "isAllDay")
    val isAllDay: Boolean?,
    @Json(name = "location")
    val location: String?,
    @Json(name = "modifiedBy")
    val modifiedBy: Int?,
    @Json(name = "modifiedDate")
    val modifiedDate: String?,
    @Json(name = "phoneNumber")
    val phoneNumber: String?,
    @Json(name = "start")
    val start: String?,
    @Json(name = "status")
    val status: String?,
    @Json(name = "title")
    val title: String?,
    @Json(name = "vendorId")
    val vendorId: Int?
) : Parcelable
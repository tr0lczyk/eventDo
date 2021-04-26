package com.rad4m.eventdo.models

import com.squareup.moshi.Json

data class FireBaseToken(
    @Json(name = "fireToken")
    val fireToken: String,
    @Json(name = "phoneNumber")
    val phoneNumber: String,
    @Json(name = "deviceId")
    val deviceId: String
)
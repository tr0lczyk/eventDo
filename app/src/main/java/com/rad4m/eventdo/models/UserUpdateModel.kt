package com.rad4m.eventdo.models

import com.squareup.moshi.Json

data class UserUpdateModel(
    @Json(name = "phoneNumber")
    val phoneNumber: String?,
    @Json(name = "email")
    val email: String?,
    @Json(name = "surname")
    val surname: String?,
    @Json(name = "name")
    val name: String?
)
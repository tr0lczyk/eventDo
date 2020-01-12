package com.rad4m.eventdo.models

import com.squareup.moshi.Json

data class UserModel(
    @Json(name = "message")
    val message: String?,
    @Json(name = "result")
    val result: UserResult?,
    @Json(name = "resultId")
    val resultId: Int?
)

data class UserResult(
    @Json(name = "email")
    val email: String?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "phoneNumber")
    val phoneNumber: String?,
    @Json(name = "surname")
    val surname: String?
)
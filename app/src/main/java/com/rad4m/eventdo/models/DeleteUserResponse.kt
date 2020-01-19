package com.rad4m.eventdo.models

import com.squareup.moshi.Json

data class DeleteUserResponse(
    @Json(name = "message")
    val message: String?,
    @Json(name = "result")
    val result: String?,
    @Json(name = "resultId")
    val resultId: Int?
)
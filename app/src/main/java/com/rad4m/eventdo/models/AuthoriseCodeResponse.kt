package com.rad4m.eventdo.models

import com.squareup.moshi.Json

data class AuthoriseCodeResponse(
    @Json(name = "message")
    val message: String?,
    @Json(name = "result")
    val result: CodeResult?,
    @Json(name = "resultId")
    val resultId: Int?
)

data class CodeResult(
    @Json(name = "asyncState")
    val asyncState: Any?,
    @Json(name = "creationOptions")
    val creationOptions: Int?,
    @Json(name = "exception")
    val exception: Any?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "isCanceled")
    val isCanceled: Boolean?,
    @Json(name = "isCompleted")
    val isCompleted: Boolean?,
    @Json(name = "isCompletedSuccessfully")
    val isCompletedSuccessfully: Boolean?,
    @Json(name = "isFaulted")
    val isFaulted: Boolean?,
    @Json(name = "result")
    val result: ResultX?,
    @Json(name = "status")
    val status: Int?
)

data class ResultX(
    @Json(name = "contentTypes")
    val contentTypes: List<Any?>?,
    @Json(name = "declaredType")
    val declaredType: Any?,
    @Json(name = "formatters")
    val formatters: List<Any?>?,
    @Json(name = "statusCode")
    val statusCode: Int?,
    @Json(name = "value")
    val value: String?
)

data class CodeValue(
    @Json(name = "Access_token")
    val accessToken: String?,
    @Json(name = "VendorToken")
    val vendorToken: String?,
    @Json(name = "UserName")
    val userName: Any?
)
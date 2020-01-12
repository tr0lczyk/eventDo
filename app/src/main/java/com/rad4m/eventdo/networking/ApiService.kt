package com.rad4m.eventdo.networking

import com.rad4m.eventdo.models.AuthoriseCodeResponse
import com.rad4m.eventdo.models.AuthoriseNumberResponse
import com.rad4m.eventdo.models.EventsResponse
import com.rad4m.eventdo.models.UserModel
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

private const val AUTHORISE_MOBILE_NUMBER = "AuthoriseNumber"
private const val AUTHORISE_USER_CODE = "Authorise"
private const val GET_USER_EVENTS = "GetsEventsForUsers/{phoneNumber}/{lastDate}"
private const val GET_USER_PROFILE = "Getuserprofile/{phoneNumber}"
private const val UPDATE_USER_PROFILE = "UpdateUserProfile"

interface ApiService {

    @FormUrlEncoded
    @PUT(AUTHORISE_MOBILE_NUMBER)
    suspend fun authorisePhoneNumber(
        @Field("phonenumber") phonenumber: String
    ): Response<AuthoriseNumberResponse>

    @FormUrlEncoded
    @PUT(AUTHORISE_USER_CODE)
    suspend fun authoriseUserCode(
        @Field("userId") userId: Long,
        @Field("code") code: String
    ): Response<AuthoriseCodeResponse>

    @GET(GET_USER_EVENTS)
    suspend fun getEventsList(
        @Header("Authorization") token: String,
        @Path("phoneNumber") phoneNumber: String,
        @Path("lastDate") date: String
    ): Response<EventsResponse>

    @GET(GET_USER_PROFILE)
    suspend fun getUserProfile(
        @Header("Authorization") token: String,
        @Path("phoneNumber") phoneNumber: String
    ): Response<UserModel>

    @FormUrlEncoded
    @PUT
}
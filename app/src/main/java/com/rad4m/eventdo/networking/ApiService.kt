package com.rad4m.eventdo.networking

import com.rad4m.eventdo.models.*
import retrofit2.Response
import retrofit2.http.*

private const val AUTHORISE_MOBILE_NUMBER = "AuthoriseNumber"
private const val AUTHORISE_USER_CODE = "Authorise"
private const val GET_USER_EVENTS = "GetsEventsForUsers/{phoneNumber}/{lastDate}"
private const val GET_USER_PROFILE = "Getuserprofile/{phoneNumber}"
private const val UPDATE_USER_PROFILE = "UpdateUserProfile"
private const val DELETE_USER_ACCOUNT = "DisableUser"
private const val FIREBASE_TOKEN_UDPATE = "FirebaseToken"

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

    @PUT(UPDATE_USER_PROFILE)
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body userUpdateModel: UserUpdateModel
    ): Response<UserUpdateResponse>

    @FormUrlEncoded
    @PUT(DELETE_USER_ACCOUNT)
    suspend fun deleteUserAccount(
        @Header("Authorization") token: String,
        @Field("phonenumber") phoneNumber: String
    ): Response<DeleteUserResponse>

    @Headers("Accept: application/json")
    @POST(FIREBASE_TOKEN_UDPATE)
    suspend fun updateFirebaseToken(
        @Header("Authorization") token: String,
        @Body fireBaseToken: FireBaseToken
    ): Response<FirebaseTokenUpdateResponseModel>
}
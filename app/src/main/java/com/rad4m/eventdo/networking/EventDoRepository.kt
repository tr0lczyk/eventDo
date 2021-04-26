package com.rad4m.eventdo.networking

import com.rad4m.eventdo.models.*
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities.Companion.FIREBASE_TOKEN
import com.rad4m.eventdo.utils.Utilities.Companion.USER_LAST_DATE
import com.rad4m.eventdo.utils.Utilities.Companion.USER_NUMBER
import com.rad4m.eventdo.utils.Utilities.Companion.USER_TOKEN
import com.rad4m.eventdo.utils.Utilities.Companion.convertDateToString
import com.rad4m.eventdo.utils.Utilities.Companion.convertDateToStringWithZ
import com.rad4m.eventdo.utils.Utilities.Companion.getUuidId
import java.util.Date
import javax.inject.Inject

class EventDoRepository @Inject constructor(
    private val apiService: ApiService,
    val sharedPrefs: SharedPreferences
) : BaseRepository() {

    private val token = sharedPrefs.getValueString(USER_TOKEN)
    private val userToken = "bearer $token"
    private val userNumber = sharedPrefs.getValueString(USER_NUMBER)?.replace("+", "") ?: ""
    private val lastDate =
        /*sharedPrefs.getValueString(USER_LAST_DATE) ?:*/ convertDateToStringWithZ(Date(0))

    suspend fun putAuthoriseNumber(
        phonenumber: String
    ): Result<AuthoriseNumberResponse?> {
        return baseApiCall(
            block = {
                apiService.authorisePhoneNumber(
                    phonenumber
                )
            }
        )
    }

    suspend fun putAuthoriseUserCode(
        userId: Long,
        code: String
    ): Result<AuthoriseCodeResponse?> {
        return baseApiCall(
            block = {
                apiService.authoriseUserCode(
                    userId, code
                )
            }
        )
    }

    suspend fun getEventsList(
    ): Result<EventsResponse?> {
        return baseApiCall(
            block = {
                apiService.getEventsList(
                    userToken,
                    userNumber,
                    lastDate
                )
            }
        )
    }

    suspend fun getUserProfile(
    ): Result<UserModel?> {
        return baseApiCall(
            block = {
                apiService.getUserProfile(
                    userToken,
                    userNumber
                )
            }
        )
    }

    suspend fun updateUserProfile(
        userUpdateModel: UserUpdateModel
    ): Result<UserUpdateResponse?> {
        return baseApiCall(
            block = {
                apiService.updateUserProfile(
                    userToken,
                    userUpdateModel
                )
            }
        )
    }

    suspend fun deleteUserAccount(): Result<DeleteUserResponse?> {
        return baseApiCall(
            block = {
                apiService.deleteUserAccount(
                    userToken,
                    userNumber
                )
            }
        )
    }

    suspend fun updateFirebaseToken(): Result<FirebaseTokenUpdateResponseModel?> {
        return baseApiCall(
            block = {
                apiService.updateFirebaseToken(
                    userToken,
                    FireBaseToken(
                        sharedPrefs.getValueString(FIREBASE_TOKEN)!!,
                        "+$userNumber",
                        getUuidId()
                    )
                )
            }
        )
    }
}
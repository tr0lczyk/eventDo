package com.rad4m.eventdo.networking

import com.rad4m.eventdo.models.AuthoriseCodeResponse
import com.rad4m.eventdo.models.AuthoriseNumberResponse
import com.rad4m.eventdo.models.DeleteUserResponse
import com.rad4m.eventdo.models.EventsResponse
import com.rad4m.eventdo.models.Result
import com.rad4m.eventdo.models.UserModel
import com.rad4m.eventdo.models.UserUpdateModel
import com.rad4m.eventdo.models.UserUpdateResponse
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities.Companion.USER_NUMBER
import com.rad4m.eventdo.utils.Utilities.Companion.USER_TOKEN
import com.rad4m.eventdo.utils.Utilities.Companion.convertDateToString
import java.util.Date
import javax.inject.Inject

class EventDoRepository @Inject constructor(
    private val apiService: ApiService,
    sharedPrefs: SharedPreferences
) : BaseRepository() {

    private val token = sharedPrefs.getValueString(USER_TOKEN)
    private val userToken = "bearer $token"
    private val userNumber = sharedPrefs.getValueString(USER_NUMBER)?.replace("+", "") ?: ""
    private val lastDate =
        /*sharedPrefs.getValueString(USER_LAST_DATE) ?:*/ convertDateToString(Date(0))

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
}
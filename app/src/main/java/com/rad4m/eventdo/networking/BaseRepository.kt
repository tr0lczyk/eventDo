package com.rad4m.eventdo.networking

import com.rad4m.eventdo.models.Result
import retrofit2.Response
import timber.log.Timber

open class BaseRepository {

    suspend fun <T : Any> baseApiCall(block: suspend () -> Response<T>): Result<T?> {
        return try {
            val response = block.invoke()
            if (response.isSuccessful) {
                Result.Success(response.body())
            } else {
                Timber.i(response.message())
                Result.Failure(response.code().toString())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "")
        }
    }
}
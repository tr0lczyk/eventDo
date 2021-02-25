package com.rad4m.eventdo.utils

import android.content.Context
import android.text.format.DateUtils
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rad4m.eventdo.EventDoApplication
import com.rad4m.eventdo.R
import com.rad4m.eventdo.database.EventsDatabase
import com.rad4m.eventdo.di.ApiModule
import com.rad4m.eventdo.models.EventModel
import com.rad4m.eventdo.networking.ApiService
import com.rad4m.eventdo.networking.EventDoRepository
import com.rad4m.eventdo.utils.Utilities.Companion.USER_LAST_DATE
import com.rad4m.eventdo.utils.Utilities.Companion.addEachNewEventToCalendar
import com.rad4m.eventdo.utils.Utilities.Companion.convertDateToStringWithZ
import com.rad4m.eventdo.utils.Utilities.Companion.convertDateToStringWithZWithoutOneMinute
import com.rad4m.eventdo.utils.Utilities.Companion.convertStringToDate
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.*

class EventsDownloadWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParams
) {

    val sharedPrefs = SharedPreferences(
        EventDoApplication.instance, Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    )

    val apiService = Retrofit.Builder().addConverterFactory(
        MoshiConverterFactory.create(
            Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
        )
    )
        .baseUrl(ApiModule.BASE_URL)
        .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }).build())
        .build().create<ApiService>(ApiService::class.java)

    val repository = EventDoRepository(
        apiService, sharedPrefs
    )

    val database = EventsDatabase(appContext)


    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        try {
            Timber.i("=========log")
            when (val response = repository.getEventsList()) {
                is com.rad4m.eventdo.models.Result.Success -> {
                    if (sharedPrefs.getValueBoolean(Utilities.AUTO_ADD_EVENT) == true) {
                        addEachNewEventToCalendar(
                            response.data!!.result,
                            appContext
                        )
                    } else {
                        saveEvents(response.data!!.result)
                    }
                    sharedPrefs.save(
                        USER_LAST_DATE,
                        convertDateToStringWithZ(Date())
                    )
                    Result.success()
                }
                is com.rad4m.eventdo.models.Result.Failure -> {
                    Timber.i("failure")
                    Result.failure()
                }
                is com.rad4m.eventdo.models.Result.Error -> {
                    if (Utilities.appInForeground(appContext)) {
                        Utilities.toastMessage(
                            appContext,
                            R.string.download_events_internet_fail
                        )
                    }
                    Result.failure()
                }
                else -> Result.failure()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun saveEvents(data: List<EventModel>) {
        for (i in data) {
            i.apply {
                dtStart =
                    convertDateToStringWithZWithoutOneMinute(Date(convertStringToDate(dtStart!!).time + 1 * DateUtils.HOUR_IN_MILLIS))
                dtEnd =
                    convertDateToStringWithZWithoutOneMinute(Date(convertStringToDate(dtEnd!!).time + 1 * DateUtils.HOUR_IN_MILLIS))
            }
        }
        database.eventsDao().insertEvents(data)
    }
}

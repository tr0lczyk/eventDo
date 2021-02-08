package com.rad4m.eventdo.ui.mainfragment

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.format.DateUtils
import androidx.work.ListenableWorker
import com.rad4m.eventdo.EventDoApplication
import com.rad4m.eventdo.R
import com.rad4m.eventdo.database.EventsDatabase
import com.rad4m.eventdo.di.ApiModule
import com.rad4m.eventdo.models.EventModel
import com.rad4m.eventdo.models.Result
import com.rad4m.eventdo.networking.ApiService
import com.rad4m.eventdo.networking.EventDoRepository
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.*

class NetworkService : Service() {

    val appContext = EventDoApplication.instance
    val job = Job()
    val serviceScope = CoroutineScope(Dispatchers.Main + job)
    val mainHandler = Handler(Looper.getMainLooper())

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

    val database = EventsDatabase(EventDoApplication.instance)

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mainHandler.post(object : Runnable {
            override fun run() {
                actualTask()
                mainHandler.postDelayed(this, 8000)
            }
        })
        return START_NOT_STICKY
    }

    fun actualTask() {
        serviceScope.launch {
            when (val response = repository.getEventsList()) {
                is Result.Success -> {
                    if (sharedPrefs.getValueBoolean(Utilities.AUTO_ADD_EVENT) == true) {
                        Utilities.addEachNewEventToCalendar(
                            response.data!!.result,
                            appContext
                        )
                    } else {
                        saveEvents(response.data!!.result)
                    }
                    sharedPrefs.save(
                        Utilities.USER_LAST_DATE,
                        Utilities.convertDateToStringWithZ(Date())
                    )
                    ListenableWorker.Result.success()
                }
                is Result.Failure -> {
                    Timber.i("failure")
                }
                is Result.Error -> {
                    if (Utilities.appInForeground(appContext)) {
                        Utilities.toastMessage(
                            appContext,
                            R.string.download_events_internet_fail
                        )
                    }
                }
            }
        }
    }

    suspend fun saveEvents(data: List<EventModel>) {
        for (i in data) {
            i.apply {
                dtStart =
                    Utilities.convertDateToStringWithZ(
                        Date(
                            Utilities.convertStringToDate(
                                dtStart!!
                            ).time + 1 * DateUtils.HOUR_IN_MILLIS
                        )
                    )
                dtEnd =
                    Utilities.convertDateToStringWithZ(Date(Utilities.convertStringToDate(dtEnd!!).time + 1 * DateUtils.HOUR_IN_MILLIS))
            }
        }
        database.eventsDao().insertEvents(data)
    }


    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
    }
}

package com.rad4m.eventdo.ui.settingsfragment

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rad4m.eventdo.R
import com.rad4m.eventdo.models.Result
import com.rad4m.eventdo.networking.EventDoRepository
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities
import com.rad4m.eventdo.utils.Utilities.Companion.AUTO_ADD_EVENT
import com.rad4m.eventdo.utils.Utilities.Companion.NEW_EVENT_PAGE
import com.rad4m.eventdo.utils.Utilities.Companion.PUSH_NOTIFICATION
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    val sharedPrefs: SharedPreferences,
    application: Application,
    private val repository: EventDoRepository
) : AndroidViewModel(application) {

    val backIconTintColor = MutableLiveData<Int>()
    val backNavigation = MutableLiveData<Boolean>()
    val isNewEventPageOn = MutableLiveData<Boolean>()
    val isAutoAddEventOn = MutableLiveData<Boolean>()
    val isPushEnabled = MutableLiveData<Boolean>()

    init {
        changeBackButtonColor(R.color.darkGray)
        isNewEventPageOn.value = sharedPrefs.getValueBoolean(NEW_EVENT_PAGE)
        isAutoAddEventOn.value = sharedPrefs.getValueBoolean(AUTO_ADD_EVENT)
        isPushEnabled.value = sharedPrefs.getValueBoolean(PUSH_NOTIFICATION)

    }

    fun startBackNavigation() {
        backNavigation.value = true
        changeBackButtonColor(R.color.blue)
    }

    fun stopBackNavigation() {
        backNavigation.value = false
        changeBackButtonColor(R.color.darkGray)
    }

    private fun changeBackButtonColor(colorInt: Int) {
        backIconTintColor.value = ContextCompat.getColor(getApplication(), colorInt)
    }

    fun updateFirebaseToken() {
        viewModelScope.launch {
            when (val response = repository.updateFirebaseToken()) {
                is Result.Success -> sharedPrefs.save(
                    Utilities.DEVICE_ID,
                    response.data!!.result!!
                )
                is Result.Failure -> Timber.i(response.failure)
                is Result.Error -> Timber.i(response.error)
            }
        }
    }
}
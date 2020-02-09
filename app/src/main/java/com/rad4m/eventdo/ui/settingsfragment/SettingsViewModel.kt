package com.rad4m.eventdo.ui.settingsfragment

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rad4m.eventdo.R
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities.Companion.AUTO_ADD_EVENT
import com.rad4m.eventdo.utils.Utilities.Companion.NEW_EVENT_PAGE
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    val sharedPrefs: SharedPreferences, application: Application
) : AndroidViewModel(application) {

    val backIconTintColor = MutableLiveData<Int>()
    val backNavigation = MutableLiveData<Boolean>()
    val isNewEventPageOn = MutableLiveData<Boolean>()
    val isAutoAddEventOn = MutableLiveData<Boolean>()

    init {
        changeBackButtonColor(R.color.darkGray)
        isNewEventPageOn.value = sharedPrefs.getValueBoolean(NEW_EVENT_PAGE) ?: false
        isAutoAddEventOn.value = sharedPrefs.getValueBoolean(AUTO_ADD_EVENT) ?: true
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
}
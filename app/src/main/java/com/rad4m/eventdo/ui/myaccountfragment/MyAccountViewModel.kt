package com.rad4m.eventdo.ui.myaccountfragment

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rad4m.eventdo.R
import com.rad4m.eventdo.networking.EventDoRepository
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities.Companion.USER_NUMBER
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject

class MyAccountViewModel @Inject constructor(
    private val repository: EventDoRepository,
    val sharedPrefs: SharedPreferences,
    application: Application
) : AndroidViewModel(application) {

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val backIconTintColor = MutableLiveData<Int>()
    val backNavigation = MutableLiveData<Boolean>()
    val phoneNumber = MutableLiveData<String>()
    val userName = MutableLiveData<String>()
    val userSurname = MutableLiveData<String>()
    val userMail = MutableLiveData<String>()

    init {
        changeBackButtonColor(R.color.darkGray)
        phoneNumber.value = sharedPrefs.getValueString(USER_NUMBER)
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
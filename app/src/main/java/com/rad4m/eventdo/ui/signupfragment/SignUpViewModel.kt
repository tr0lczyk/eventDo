package com.rad4m.eventdo.ui.signupfragment

import android.app.Application
import android.telephony.PhoneNumberUtils
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rad4m.eventdo.R
import com.rad4m.eventdo.models.Result
import com.rad4m.eventdo.networking.EventDoRepository
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities.Companion.USER_ID
import com.rad4m.eventdo.utils.Utilities.Companion.USER_NUMBER
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SignUpViewModel @Inject constructor(
    private val repository: EventDoRepository,
    private val sharedPrefs: SharedPreferences,
    application: Application
) : AndroidViewModel(application) {

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val termsStarted = MutableLiveData<Boolean>()
    val policyStarted = MutableLiveData<Boolean>()
    val prefix = MutableLiveData<String>()
    val phoneNumber = MutableLiveData<String>()
    val navigateToVerification = MutableLiveData<Boolean>()
    private var isNumberSend: Boolean = false

    fun openTerms() {
        termsStarted.value = true
    }

    fun openPolicy() {
        policyStarted.value = true
    }

    private fun openVerification(result: Long) {
        sharedPrefs.save(USER_ID, result)
        navigateToVerification.value = true
    }

    private fun connectionFailure() {
        Toast.makeText(
            getApplication(),
            getApplication<Application>().getString(R.string.sign_up_internet_fail),
            Toast.LENGTH_LONG
        ).show()
    }

    fun sendNumber() {
        if (!isNumberSend) {
            isNumberSend = true
            val number = "${prefix.value}${phoneNumber.value}"
            sharedPrefs.save(USER_NUMBER, "${prefix.value}${phoneNumber.value}")
            viewModelScope.launch {
                if (PhoneNumberUtils.isGlobalPhoneNumber(number)) {
                    when (val response = repository.putAuthoriseNumber(number)) {
                        is Result.Success -> openVerification(response.data!!.result)
                        is Result.Failure -> Timber.i("failure")
                        is Result.Error -> connectionFailure()
                    }
                } else {
                    Toast.makeText(
                        getApplication(),
                        getApplication<Application>().getString(R.string.proper_phone),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                isNumberSend = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
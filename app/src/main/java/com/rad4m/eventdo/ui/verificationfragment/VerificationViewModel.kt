package com.rad4m.eventdo.ui.verificationfragment

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rad4m.eventdo.R
import com.rad4m.eventdo.models.Result
import com.rad4m.eventdo.networking.EventDoRepository
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities.Companion.USER_ID
import com.rad4m.eventdo.utils.Utilities.Companion.USER_TOKEN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

class VerificationViewModel @Inject constructor(
    private val repository: EventDoRepository,
    private val sharedPrefs: SharedPreferences,
    application: Application
) : AndroidViewModel(application) {

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val code = MutableLiveData<String>()
    val navigateToMain = MutableLiveData<Boolean>()
    val codeIncorrect = MutableLiveData<Boolean>()

    fun sendUserCode() {
        val userId = sharedPrefs.getValueLong(USER_ID)
        viewModelScope.launch {
            when (val response = repository.putAuthoriseUserCode(userId!!, code.value!!)) {
                is Result.Success -> codeVerifiedCorrectly(response.data!!.result!!.result!!.value!!)
                is Result.Failure -> wrongCode()
                is Result.Error -> verifyErrorType(response.error)
            }
        }
    }

    private fun verifyErrorType(error: String) {
        if (error == "Expected BEGIN_OBJECT but was STRING at path \$.result") {
            wrongCode()
        } else {
            connectionFailure()
        }
    }

    private fun connectionFailure() {
        Toast.makeText(
            getApplication(),
            getApplication<Application>().getString(R.string.verification_internet_fail),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun wrongCode() {
        Toast.makeText(
            getApplication(),
            getApplication<Application>().getString(R.string.code_not_correct),
            Toast.LENGTH_SHORT
        ).show()
        codeIncorrect.value = true
    }

    private fun codeVerifiedCorrectly(response: String) {
        val answer = JSONObject(response)
        val token = answer.getString("Access_token")
        sharedPrefs.save(USER_TOKEN, token)
        Toast.makeText(
            getApplication(),
            getApplication<Application>().getString(R.string.code_verified_correctly),
            Toast.LENGTH_SHORT
        ).show()
        openMain()
    }

    private fun openMain() {
        navigateToMain.postValue(true)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}

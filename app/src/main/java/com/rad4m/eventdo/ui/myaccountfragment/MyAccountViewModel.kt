package com.rad4m.eventdo.ui.myaccountfragment

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rad4m.eventdo.R
import com.rad4m.eventdo.database.EventsDatabase
import com.rad4m.eventdo.models.Result
import com.rad4m.eventdo.models.UserResult
import com.rad4m.eventdo.models.UserUpdateModel
import com.rad4m.eventdo.networking.EventDoRepository
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities.Companion.USER_LOGOUT
import com.rad4m.eventdo.utils.Utilities.Companion.USER_NUMBER
import com.rad4m.eventdo.utils.Utilities.Companion.isValidEmail
import com.rad4m.eventdo.utils.Utilities.Companion.toastMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MyAccountViewModel @Inject constructor(
    private val repository: EventDoRepository,
    val sharedPrefs: SharedPreferences,
    private val database: EventsDatabase,
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
    val navigateToLogin = MutableLiveData<Boolean>()
    val showDeleteUserDialog = MutableLiveData<Boolean>()

    val userBaseName = MutableLiveData<String>(application.getString(R.string.first_name_my_account))
    val userBaseSurname = MutableLiveData<String>(application.getString(R.string.surname_my_account))
    val userBaseMail = MutableLiveData<String>(application.getString(R.string.email_my_account))

    init {
        changeBackButtonColor(R.color.darkGray)
        phoneNumber.value = sharedPrefs.getValueString(USER_NUMBER)
        getUserProfile()
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

    private fun getUserProfile() {
        viewModelScope.launch {
            when (val response = repository.getUserProfile()) {
                is Result.Success -> setUserData(response.data!!.result!!)
                is Result.Failure -> Timber.i(response.failure)
                is Result.Error -> toastMessage(
                    getApplication(),
                    R.string.my_account_internet_fail
                )
            }
        }
    }

    private fun setUserData(userData: UserResult) {
        if (!userData.name.isNullOrEmpty()) {
            userBaseName.value = userData.name
        }
        if (!userData.surname.isNullOrEmpty()) {
            userBaseSurname.value = userData.surname
        }
        if (!userData.email.isNullOrEmpty()) {
            userBaseMail.value = userData.email
        }
    }

    fun updateUserProfile() {
        viewModelScope.launch {
            val credentials = UserUpdateModel(
                phoneNumber.value,
                userMail.value,
                userSurname.value,
                userName.value
            )
            when (val response = repository.updateUserProfile(
                credentials
            )) {
                is Result.Success -> toastMessage(
                    getApplication(),
                    R.string.account_detail_updated
                )
                is Result.Failure -> Timber.i(response.failure)
                is Result.Error -> toastMessage(
                    getApplication(),
                    R.string.update_account_internet_fail
                )
            }
        }
        getUserProfile()
    }

    fun checkIfChangePossible() {
        if (isValidEmail(userMail.value.toString())) {
            updateUserProfile()
        } else {
            toastMessage(
                getApplication(),
                R.string.email_invalid
            )
        }
    }

    fun askUserIfDelete() {
        showDeleteUserDialog.value = true
    }

    fun deleteUserAccount() {
        viewModelScope.launch {
            when (val response = repository.deleteUserAccount()) {
                is Result.Success -> deleteUserAccountSuccess()
                is Result.Failure -> Timber.i(response.failure)
                is Result.Error -> toastMessage(
                    getApplication(),
                    R.string.delete_account_internet_fail
                )
            }
        }
    }

    private fun deleteAllEvents() {
        viewModelScope.launch {
            database.eventsDao().deleteEvents()
        }
    }

    private fun deleteUserAccountSuccess() {
        Timber.i("Success")
        navigateToLogin.value = true
        sharedPrefs.clearSharedPreference()
        sharedPrefs.save(USER_LOGOUT, true)
        deleteAllEvents()
    }
}
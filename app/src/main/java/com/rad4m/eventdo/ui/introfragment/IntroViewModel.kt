package com.rad4m.eventdo.ui.introfragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class IntroViewModel : ViewModel() {

    val navigateToSignUp = MutableLiveData<Boolean>()

    fun openSignUpScreen() {
        navigateToSignUp.value = true
    }
}
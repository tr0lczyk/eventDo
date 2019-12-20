package com.rad4m.eventdo.ui.myaccountfragment

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rad4m.eventdo.R

class MyAccountViewModel(application: Application) : AndroidViewModel(application){

    val backIconTintColor = MutableLiveData<Int>()
    val backNavigation = MutableLiveData<Boolean>()

    init {
        changeBackButtonColor(R.color.darkGray)
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
package com.rad4m.eventdo.di

import android.app.Activity
import android.app.Service
import androidx.fragment.app.Fragment

val Activity.appComponent: AppComponent
    get() = (application as AppComponentProvider).appComponent

val Fragment.appComponent: AppComponent
    get() = requireActivity().appComponent
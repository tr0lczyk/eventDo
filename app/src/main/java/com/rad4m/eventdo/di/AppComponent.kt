package com.rad4m.eventdo.di

import android.app.Application
import com.rad4m.eventdo.EventDoApplication
import com.rad4m.eventdo.MainActivity
import com.rad4m.eventdo.SplashActivity
import com.rad4m.eventdo.ui.introfragment.IntroFragment
import com.rad4m.eventdo.ui.mainfragment.MainFragment
import com.rad4m.eventdo.ui.myaccountfragment.MyAccountFragment
import com.rad4m.eventdo.ui.settingsfragment.SettingsFragment
import com.rad4m.eventdo.ui.signupfragment.SignUpFragment
import com.rad4m.eventdo.ui.verificationfragment.VerificationFragment
import com.rad4m.eventdo.utils.MyFirebaseMessagingService
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApiModule::class, DbModule::class, ViewModelModule::class])
interface AppComponent {

    fun application(): Application

    fun inject(activity: MainActivity)

    fun inject(activity: SplashActivity)

    fun inject(activity: MainFragment)

    fun inject(activity: IntroFragment)

    fun inject(activity: VerificationFragment)

    fun inject(activity: SignUpFragment)

    fun inject(activity: MyAccountFragment)

    fun inject(activity: SettingsFragment)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(context: Application): Builder

        fun build(): AppComponent
    }
}
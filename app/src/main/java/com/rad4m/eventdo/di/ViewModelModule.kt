package com.rad4m.eventdo.di

import androidx.lifecycle.ViewModel
import com.rad4m.eventdo.ui.mainfragment.MainViewModel
import com.rad4m.eventdo.ui.myaccountfragment.MyAccountViewModel
import com.rad4m.eventdo.ui.neweventpage.NewEventViewModel
import com.rad4m.eventdo.ui.settingsfragment.SettingsViewModel
import com.rad4m.eventdo.ui.signupfragment.SignUpViewModel
import com.rad4m.eventdo.ui.verificationfragment.VerificationViewModel
import com.rad4m.eventdo.utils.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
@Suppress("unused")
internal abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SignUpViewModel::class)
    abstract fun bindSignUpViewModel(viewModel: SignUpViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VerificationViewModel::class)
    abstract fun bindVerificationViewModel(viewModel: VerificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NewEventViewModel::class)
    abstract fun bindNewEventViewModel(viewModel: NewEventViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MyAccountViewModel::class)
    abstract fun bindMyAccountViewModel(viewModel: MyAccountViewModel): ViewModel
}
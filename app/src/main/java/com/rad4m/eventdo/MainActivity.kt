package com.rad4m.eventdo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities.Companion.USER_TOKEN
import kotlinx.android.synthetic.main.activity_main.myNavHostFragment
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        setContentView(R.layout.activity_main)
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.introFragment, true)
            .build()
        sharedPrefs.getValueString(USER_TOKEN)?.let {
            NavHostFragment.findNavController(myNavHostFragment)
                .navigate(R.id.action_introFragment_to_mainFragment, null, navOptions)
        }
    }

    override fun onSupportNavigateUp() =
        NavHostFragment.findNavController(myNavHostFragment).navigateUp()
}

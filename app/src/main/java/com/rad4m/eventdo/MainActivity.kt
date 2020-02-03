package com.rad4m.eventdo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities.Companion.CHANNEL_DESC
import com.rad4m.eventdo.utils.Utilities.Companion.CHANNEL_ID
import com.rad4m.eventdo.utils.Utilities.Companion.CHANNEL_NAME
import com.rad4m.eventdo.utils.Utilities.Companion.FIREBASE_TOKEN
import com.rad4m.eventdo.utils.Utilities.Companion.USER_TOKEN
import com.rad4m.eventdo.utils.Utilities.Companion.makeStatusBarTransparent
import kotlinx.android.synthetic.main.activity_main.myNavHostFragment
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        makeStatusBarTransparent()
        setContentView(R.layout.activity_main)
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                val token = task.result?.token
                sharedPrefs.save(FIREBASE_TOKEN, "$token")
                Timber.d("======$token")
            })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = CHANNEL_DESC
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

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

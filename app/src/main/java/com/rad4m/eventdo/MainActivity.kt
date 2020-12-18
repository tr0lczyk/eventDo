package com.rad4m.eventdo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.models.EventModel
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities
import com.rad4m.eventdo.utils.Utilities.Companion.CHANNEL_DESC
import com.rad4m.eventdo.utils.Utilities.Companion.CHANNEL_ID
import com.rad4m.eventdo.utils.Utilities.Companion.CHANNEL_NAME
import com.rad4m.eventdo.utils.Utilities.Companion.USER_TOKEN
import com.rad4m.eventdo.utils.Utilities.Companion.makeStatusBarTransparent
import kotlinx.android.synthetic.main.activity_main.*
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
            if (intent.hasExtra("id")){
                val eventModel = EventModel(
                    id = intent!!.getStringExtra(Utilities.NOTIFICATION_EVENT_ID).toLong(),
                    title = intent.getStringExtra(Utilities.NOTIFICATION_EVENT_TITLE),
                    dtStart = intent.getStringExtra(Utilities.NOTIFICATION_EVENT_DTSTART),
                    dtEnd = intent.getStringExtra(Utilities.NOTIFICATION_EVENT_DTEND),
                    location = intent.getStringExtra(Utilities.NOTIFICATION_EVENT_LOCATION)
                )
                val bundle = bundleOf("mainBundle" to eventModel)
                NavHostFragment.findNavController(myNavHostFragment)
                    .navigate(R.id.action_introFragment_to_mainFragment, bundle, navOptions)
            } else {
                NavHostFragment.findNavController(myNavHostFragment)
                    .navigate(R.id.action_introFragment_to_mainFragment, null, navOptions)
            }

        }
    }

    companion object {

        lateinit var instance: MainActivity

        fun getInstancem(): MainActivity {

            return instance
        }
    }

    override fun onSupportNavigateUp() =
        NavHostFragment.findNavController(myNavHostFragment).navigateUp()
}

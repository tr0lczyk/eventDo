package com.rad4m.eventdo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.models.EventModel
import com.rad4m.eventdo.ui.mainfragment.NetworkService
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities
import com.rad4m.eventdo.utils.Utilities.Companion.FIREBASE_TOKEN
import com.rad4m.eventdo.utils.Utilities.Companion.USER_TOKEN
import com.rad4m.eventdo.utils.Utilities.Companion.makeStatusBarTransparent
import com.rad4m.eventdo.utils.UtilitiesCalendar
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    private val RECORD_REQUEST_CODE = 101

    lateinit var mMessageReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        makeStatusBarTransparent()
        setContentView(R.layout.activity_main)
        mMessageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val eventModel =
                    intent!!.getParcelableExtra<EventModel>(Utilities.EXTRA_RETURN_MESSAGE)
                Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.events_added_to_calendar),
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.show_calendar) {
                        UtilitiesCalendar.openCalendarFromActivity(
                            this@MainActivity,
                            eventModel
                        )
                    }
                    .show()
            }
        }
        if (sharedPrefs.getValueString(FIREBASE_TOKEN).isNullOrEmpty()) {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        return@OnCompleteListener
                    }
                })
        }
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.introFragment, true)
            .build()
        sharedPrefs.getValueString(USER_TOKEN)?.let {
            NavHostFragment.findNavController(myNavHostFragment)
                .navigate(R.id.action_introFragment_to_mainFragment, null, navOptions)
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mMessageReceiver, IntentFilter("EVENT_SNACKBAR"))
    }

    companion object {

        lateinit var instance: MainActivity

        fun getInstancem(): MainActivity {

            return instance
        }
    }

    override fun onSupportNavigateUp() =
        NavHostFragment.findNavController(myNavHostFragment).navigateUp()

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(EventDoApplication.instance, NetworkService::class.java))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}

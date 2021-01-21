package com.rad4m.eventdo

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.ui.mainfragment.NetworkService
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities.Companion.FIREBASE_TOKEN
import com.rad4m.eventdo.utils.Utilities.Companion.USER_TOKEN
import com.rad4m.eventdo.utils.Utilities.Companion.makeStatusBarTransparent
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    private val RECORD_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        makeStatusBarTransparent()
        setContentView(R.layout.activity_main)
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

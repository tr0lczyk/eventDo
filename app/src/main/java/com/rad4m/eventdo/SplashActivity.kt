package com.rad4m.eventdo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities.Companion.EVENT_ID_NOTIFICATION
import javax.inject.Inject

class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        if (intent.extras != null) {
            intent.extras!!.getString("id")?.let {
                sharedPreferences.save(EVENT_ID_NOTIFICATION,
                    it
                )
            }
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
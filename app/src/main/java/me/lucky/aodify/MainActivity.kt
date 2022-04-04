package me.lucky.aodify

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity

import me.lucky.aodify.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: Preferences
    private lateinit var dozeManager: DozeManager

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == Preferences.SERVICE_ENABLED) binding.toggle.isChecked = prefs.isServiceEnabled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        setup()
    }

    override fun onStart() {
        super.onStart()
        prefs.registerListener(prefsListener)
        update()
    }

    override fun onStop() {
        super.onStop()
        prefs.unregisterListener(prefsListener)
    }

    private fun init() {
        prefs = Preferences(this)
        dozeManager = DozeManager(this)
        binding.apply {
            wssState.paintFlags = wssState.paintFlags.or(Paint.UNDERLINE_TEXT_FLAG)
            if (!Utils.isAodAvailable()) toggle.isEnabled = false
        }
    }

    private fun setup() {
        binding.apply {
            notificationListenerButton.setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
            toggle.setOnCheckedChangeListener { _, isChecked ->
                prefs.isServiceEnabled = isChecked
                dozeManager.setAlwaysOn(if (isChecked) 1 else 0)
            }
        }
    }

    private fun update() {
        binding.apply {
            wssState.text = getText(
                if (Utils.hasWriteSecureSettingsPermission(this@MainActivity))
                    R.string.wss_state_yes
                else R.string.wss_state_no
            )
            toggle.isChecked = prefs.isServiceEnabled
        }
    }
}

package me.lucky.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.*
import kotlin.concurrent.timerTask

class NotificationListenerService : NotificationListenerService() {
    private lateinit var prefs: Preferences
    private lateinit var dozeController: DozeController
    private lateinit var powerManager: PowerManager
    private val screenReceiver = ScreenReceiver()

    override fun onCreate() {
        super.onCreate()
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        deinit()
    }

    private fun init() {
        prefs = Preferences(this)
        dozeController = DozeController(this)
        powerManager = getSystemService(PowerManager::class.java)
        registerReceiver(screenReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        })
    }

    private fun deinit() {
        unregisterReceiver(screenReceiver)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (!prefs.isServiceEnabled || powerManager.isInteractive) return
        screenReceiver.timer?.cancel()
        dozeController.setDozeAlwaysOn(1)
        dozeController.sendPulse()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            migrateNotificationFilter(0, null)
    }

    private class ScreenReceiver : BroadcastReceiver() {
        companion object {
            private const val ALWAYS_OFF_DELAY = 2000L
        }
        var timer: Timer? = null

        override fun onReceive(context: Context?, intent: Intent?) {
            if (!Preferences(context ?: return).isServiceEnabled) return
            val controller = DozeController(context)
            if (controller.isDozeEnabled()) return
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    timer?.cancel()
                    controller.setDozeAlwaysOn(1)
                }
                Intent.ACTION_SCREEN_OFF -> {
                    timer?.cancel()
                    timer = Timer()
                    timer?.schedule(timerTask {
                        controller.setDozeAlwaysOn(0)
                        controller.sendPulse()
                    }, ALWAYS_OFF_DELAY)
                }
            }
        }
    }

    private class DozeController(private val ctx: Context) {
        companion object {
            private const val SETTINGS_DOZE_ALWAYS_ON = "doze_always_on"
            private const val SETTINGS_DOZE_ENABLED = "doze_enabled"
            private const val PULSE_PACKAGE = "com.android.systemui"
            private const val PULSE_ACTION = "$PULSE_PACKAGE.doze.pulse"
        }

        fun isDozeEnabled(): Boolean {
            return try {
                android.provider.Settings.Secure
                    .getInt(ctx.contentResolver, SETTINGS_DOZE_ENABLED) == 1
            } catch (exc: SecurityException) { false }
        }

        fun setDozeAlwaysOn(state: Int) {
            assert(state == 0 || state == 1)
            try {
                android.provider.Settings.Secure
                    .putInt(ctx.contentResolver, SETTINGS_DOZE_ALWAYS_ON, state)
            } catch (exc: SecurityException) {}
        }

        fun sendPulse() = ctx.sendBroadcast(Intent(PULSE_ACTION).setPackage(PULSE_PACKAGE))
    }
}

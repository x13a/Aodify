package me.lucky.aodify

import android.app.NotificationManager
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
    private lateinit var dozeManager: DozeManager
    private lateinit var powerManager: PowerManager
    private lateinit var notificationManager: NotificationManager
    private val screenReceiver = ScreenReceiver()
    private val notifications = setOf<String>()

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
        dozeManager = DozeManager(this)
        powerManager = getSystemService(PowerManager::class.java)
        notificationManager = getSystemService(NotificationManager::class.java)
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
        if (sbn == null || !prefs.isServiceEnabled || (
            notificationManager.currentInterruptionFilter
                != NotificationManager.INTERRUPTION_FILTER_ALL &&
            notificationManager.currentInterruptionFilter
                != NotificationManager.INTERRUPTION_FILTER_UNKNOWN
        )) return
        notifications.plus(sbn.key)
        screenReceiver.offTimer?.cancel()
        screenReceiver.hasNotifications = true
        dozeManager.setAlwaysOn(1)
        if (!powerManager.isInteractive) dozeManager.sendPulse()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null || !prefs.isServiceEnabled) return
        notifications.minus(sbn.key)
        if (notifications.isNotEmpty()) return
        screenReceiver.offTimer?.cancel()
        screenReceiver.hasNotifications = false
        if (powerManager.isInteractive) return
        dozeManager.setAlwaysOn(0)
        dozeManager.sendPulse()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            migrateNotificationFilter(0, null)
    }

    private class ScreenReceiver : BroadcastReceiver() {
        companion object {
            private const val OFF_DELAY = 2000L
        }

        var hasNotifications = false
        var offTimer: Timer? = null

        override fun onReceive(context: Context?, intent: Intent?) {
            if (!Preferences(context ?: return).isServiceEnabled) return
            val dozeManager = DozeManager(context)
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    offTimer?.cancel()
                    dozeManager.setAlwaysOn(1)
                }
                Intent.ACTION_SCREEN_OFF -> {
                    if (hasNotifications) return
                    offTimer?.cancel()
                    offTimer = Timer()
                    offTimer?.schedule(timerTask {
                        dozeManager.setAlwaysOn(0)
                        dozeManager.sendPulse()
                    }, OFF_DELAY)
                }
            }
        }
    }
}

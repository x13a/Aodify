package me.lucky.aodify

import android.content.Context
import android.content.Intent
import android.provider.Settings

class DozeManager(private val ctx: Context) {
    companion object {
        private const val SETTINGS_ALWAYS_ON = "doze_always_on"
//        private const val SETTINGS_ENABLED = "doze_enabled"
        private const val PULSE_PACKAGE = "com.android.systemui"
        private const val PULSE_ACTION = "$PULSE_PACKAGE.doze.pulse"
    }

//        fun isEnabled(): Boolean {
//            return try {
//                Settings.Secure.getInt(ctx.contentResolver, SETTINGS_ENABLED) == 1
//            } catch (exc: SecurityException) { false }
//        }

    fun setAlwaysOn(state: Int) {
        assert(state == 0 || state == 1)
        try {
            Settings.Secure.putInt(ctx.contentResolver, SETTINGS_ALWAYS_ON, state)
        } catch (exc: SecurityException) {}
    }

    fun sendPulse() = ctx.sendBroadcast(Intent(PULSE_ACTION).setPackage(PULSE_PACKAGE))
}

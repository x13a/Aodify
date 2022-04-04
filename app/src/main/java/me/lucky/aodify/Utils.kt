package me.lucky.aodify

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import java.lang.Exception

object Utils {
    fun hasWriteSecureSettingsPermission(ctx: Context) =
        ctx.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) ==
            PackageManager.PERMISSION_GRANTED

    fun isAodAvailable(): Boolean {
        val res = Resources.getSystem()
        return try {
            res.getBoolean(res.getIdentifier(
                "config_dozeAlwaysOnDisplayAvailable",
                "bool",
                "android",
            ))
        } catch (exc: Resources.NotFoundException) { false }
    }
}

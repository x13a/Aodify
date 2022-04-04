package me.lucky.aodify

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class TileService : TileService() {
    private lateinit var prefs: Preferences
    private lateinit var dozeManager: DozeManager
    private var isAodAvailable = false

    override fun onCreate() {
        super.onCreate()
        init()
    }

    override fun onClick() {
        super.onClick()
        val state = qsTile.state == Tile.STATE_INACTIVE
        prefs.isServiceEnabled = state
        dozeManager.setAlwaysOn(if (state) 1 else 0)
        update()
    }

    override fun onStartListening() {
        super.onStartListening()
        update()
    }

    private fun init() {
        prefs = Preferences(this)
        dozeManager = DozeManager(this)
        isAodAvailable = Utils.isAodAvailable()
    }

    private fun update() {
        qsTile.state = when {
            !isAodAvailable || !Utils.hasWriteSecureSettingsPermission(this) ->
                Tile.STATE_UNAVAILABLE
            prefs.isServiceEnabled -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }
        qsTile.updateTile()
    }
}

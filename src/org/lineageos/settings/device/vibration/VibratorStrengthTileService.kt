/*
 * Copyright (C) 2018-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.device.vibration

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import org.lineageos.settings.device.R

class VibratorStrengthTileService : TileService() {

    private fun updateUI(percent: Int) {
        qsTile?.let { tile ->
            if (!VibratorUtils.isSupported()) {
                tile.state = Tile.STATE_UNAVAILABLE
                tile.subtitle = null
            } else if (percent == 0) {
                tile.state = Tile.STATE_INACTIVE
                tile.subtitle = getString(R.string.vibrator_level_off)
            } else {
                tile.state = Tile.STATE_ACTIVE
                tile.subtitle = getString(R.string.vibrator_tile_percent_format, percent)
            }
            tile.updateTile()
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        updateUI(VibratorUtils.getStrength(this))
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        if (tile.state == Tile.STATE_UNAVAILABLE) return

        val current = VibratorUtils.getStrength(this)
        // Cycle: Light (30%) -> Medium (75%) -> Strong (100%) -> Light (30%)
        val next = when {
            current < 50 -> 75
            current < 90 -> 100
            else -> 30
        }
        VibratorUtils.setStrength(this, next)
        updateUI(next)
        VibratorUtils.testVibration(this)
    }

    companion object {
        @JvmStatic
        fun updateTile(context: Context) {
            requestListeningState(
                context,
                ComponentName(context, VibratorStrengthTileService::class.java)
            )
        }
    }
}

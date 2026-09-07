/*
 * Copyright (C) 2018-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.device.power

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import org.lineageos.settings.device.R

class BypassChargingTileService : TileService() {

    private fun updateUI(mode: Int) {
        qsTile?.let { tile ->
            when (mode) {
                BypassChargingUtils.MODE_STANDARD -> {
                    tile.state = Tile.STATE_ACTIVE
                    tile.subtitle = getString(R.string.bypass_charging_mode_1_title)
                }
                BypassChargingUtils.MODE_GAMING -> {
                    tile.state = Tile.STATE_ACTIVE
                    tile.subtitle = getString(R.string.bypass_charging_mode_2_title)
                }
                else -> {
                    tile.state = Tile.STATE_INACTIVE
                    tile.subtitle = getString(R.string.bypass_charging_mode_0_title)
                }
            }
            tile.updateTile()
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        updateUI(BypassChargingUtils.getMode(this))
    }

    override fun onClick() {
        super.onClick()
        val currentMode = BypassChargingUtils.getMode(this)
        // Cycle: Normal (0) -> Standard (1) -> Gaming (2) -> Normal (0)
        val nextMode = (currentMode + 1) % 3
        BypassChargingUtils.setMode(this, nextMode)
        updateUI(nextMode)
    }

    companion object {
        @JvmStatic
        fun updateTile(context: Context) {
            requestListeningState(
                context,
                ComponentName(context, BypassChargingTileService::class.java)
            )
        }
    }
}

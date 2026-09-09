/*
 * Copyright (C) 2018-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.settings.oplusparts.touch

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class GloveModeTileService : TileService() {

    private fun updateUI(enabled: Boolean) {
        qsTile?.let { tile ->
            if (!TouchUtils.isGloveModeSupported()) {
                tile.state = Tile.STATE_UNAVAILABLE
            } else {
                tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            }
            tile.updateTile()
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        updateUI(TouchUtils.isGloveModeEnabled(this))
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        if (tile.state == Tile.STATE_UNAVAILABLE) return
        val newState = tile.state == Tile.STATE_INACTIVE
        TouchUtils.setGloveModeEnabled(this, newState)
        updateUI(newState)
    }

    companion object {
        @JvmStatic
        fun updateTile(context: Context) {
            requestListeningState(
                context,
                ComponentName(context, GloveModeTileService::class.java)
            )
        }
    }
}

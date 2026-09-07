/*
 * Copyright (C) 2018-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.device.power

import android.content.Context
import androidx.preference.PreferenceManager
import org.lineageos.settings.device.BYPASS_CHARGING_NODE
import org.lineageos.settings.device.FileUtils
import org.lineageos.settings.device.PREF_BYPASS_CHARGING_MODE

object BypassChargingUtils {

    const val MODE_NORMAL = 0
    const val MODE_STANDARD = 1
    const val MODE_GAMING = 2

    fun isSupported(): Boolean = FileUtils.fileExists(BYPASS_CHARGING_NODE)

    fun getMode(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(PREF_BYPASS_CHARGING_MODE, MODE_NORMAL)
    }

    fun getHardwareMode(): Int {
        val value = FileUtils.readOneLine(BYPASS_CHARGING_NODE)?.trim() ?: return MODE_NORMAL
        return value.toIntOrNull() ?: MODE_NORMAL
    }

    fun setMode(context: Context, mode: Int): Boolean {
        val targetMode = mode.coerceIn(MODE_NORMAL, MODE_GAMING)
        val success = FileUtils.writeLine(BYPASS_CHARGING_NODE, targetMode.toString())
        if (success) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().putInt(PREF_BYPASS_CHARGING_MODE, targetMode).apply()
            BypassChargingTileService.updateTile(context)
        }
        return success
    }

    fun restoreOnBoot(context: Context) {
        if (!isSupported()) return
        val mode = getMode(context)
        if (mode != MODE_NORMAL) {
            FileUtils.writeLine(BYPASS_CHARGING_NODE, mode.toString())
        }
    }
}

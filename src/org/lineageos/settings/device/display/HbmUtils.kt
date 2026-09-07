/*
 * Copyright (C) 2018-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.device.display

import android.content.Context
import androidx.preference.PreferenceManager
import org.lineageos.settings.device.FileUtils
import org.lineageos.settings.device.HBM_NODE
import org.lineageos.settings.device.PREF_HBM_KEY

object HbmUtils {

    fun isSupported(): Boolean = FileUtils.fileExists(HBM_NODE)

    fun isEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(PREF_HBM_KEY, false)
    }

    fun isEnabledInHardware(): Boolean =
        FileUtils.getFileValueAsBoolean(HBM_NODE, false)

    fun setEnabled(context: Context, enabled: Boolean): Boolean {
        val success = FileUtils.writeLine(HBM_NODE, if (enabled) "1" else "0")
        if (success) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().putBoolean(PREF_HBM_KEY, enabled).apply()
            HbmTileService.updateTile(context)
        }
        return success
    }

    fun restoreOnBoot(context: Context) {
        if (!isSupported()) return
        val enabled = isEnabled(context)
        if (enabled) {
            FileUtils.writeLine(HBM_NODE, "1")
        }
    }
}

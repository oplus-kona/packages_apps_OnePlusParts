/*
 * Copyright (C) 2018-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.device.vibration

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.preference.PreferenceManager
import org.lineageos.settings.device.FileUtils
import org.lineageos.settings.device.PREF_VIBRATOR_STRENGTH_KEY
import org.lineageos.settings.device.VIBRATOR_ACTIVATE_NODE
import org.lineageos.settings.device.VIBRATOR_DURATION_NODE
import org.lineageos.settings.device.VIBRATOR_GAIN_NODE
import org.lineageos.settings.device.VIBRATOR_LEVEL_NODE
import org.lineageos.settings.device.VIBRATOR_VMAX_NODE

object VibratorUtils {

    const val MIN_STRENGTH = 0
    const val MAX_STRENGTH = 100
    const val DEFAULT_STRENGTH = 75 // ~2000 mV (Balanced, crisp haptic feedback)

    fun isSupported(): Boolean {
        return FileUtils.fileExists(VIBRATOR_VMAX_NODE) ||
                FileUtils.fileExists(VIBRATOR_GAIN_NODE) ||
                FileUtils.fileExists(VIBRATOR_LEVEL_NODE)
    }

    fun getStrength(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(PREF_VIBRATOR_STRENGTH_KEY, DEFAULT_STRENGTH)
            .coerceIn(MIN_STRENGTH, MAX_STRENGTH)
    }

    fun setStrength(context: Context, percent: Int): Boolean {
        val clampedPercent = percent.coerceIn(MIN_STRENGTH, MAX_STRENGTH)

        var success = false
        if (clampedPercent == 0) {
            // Mute / Zero gain
            if (FileUtils.fileExists(VIBRATOR_VMAX_NODE)) {
                success = FileUtils.writeLine(VIBRATOR_VMAX_NODE, "0") || success
            }
            if (FileUtils.fileExists(VIBRATOR_GAIN_NODE)) {
                success = FileUtils.writeLine(VIBRATOR_GAIN_NODE, "0") || success
            }
        } else {
            // Scale voltage: 800 mV (Light) to 2400 mV (Strong)
            val vmaxVal = 800 + ((clampedPercent * (2400 - 800)) / 100)
            if (FileUtils.fileExists(VIBRATOR_VMAX_NODE)) {
                success = FileUtils.writeLine(VIBRATOR_VMAX_NODE, vmaxVal.toString()) || success
            }
            if (FileUtils.fileExists(VIBRATOR_LEVEL_NODE)) {
                val level = ((clampedPercent * 5) / 100).coerceIn(1, 5)
                FileUtils.writeLine(VIBRATOR_LEVEL_NODE, level.toString())
            }
        }

        if (success) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().putInt(PREF_VIBRATOR_STRENGTH_KEY, clampedPercent).apply()
            VibratorStrengthTileService.updateTile(context)
        }
        return success
    }

    fun testVibration(context: Context) {
        runCatching {
            if (FileUtils.fileExists(VIBRATOR_DURATION_NODE) && FileUtils.fileExists(VIBRATOR_ACTIVATE_NODE)) {
                FileUtils.writeLine(VIBRATOR_DURATION_NODE, "60")
                FileUtils.writeLine(VIBRATOR_ACTIVATE_NODE, "1")
            }
        }
        runCatching {
            val vibrator = context.getSystemService(Vibrator::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(60)
            }
        }
    }

    fun restoreOnBoot(context: Context) {
        if (!isSupported()) return
        val strength = getStrength(context)
        setStrength(context, strength)
    }
}

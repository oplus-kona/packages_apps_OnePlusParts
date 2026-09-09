/*
 * Copyright (C) 2018-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.settings.oplusparts.touch

import android.content.Context
import androidx.preference.PreferenceManager
import org.settings.oplusparts.FileUtils
import org.settings.oplusparts.GAME_TOUCH_NODE
import org.settings.oplusparts.GLOVE_MODE_NODE
import org.settings.oplusparts.PREF_GAME_TOUCH_KEY
import org.settings.oplusparts.PREF_GLOVE_MODE_KEY
import org.settings.oplusparts.PREF_TOUCH_SENSITIVITY_KEY
import org.settings.oplusparts.PREF_TOUCH_SMOOTHING_KEY
import org.settings.oplusparts.TOUCH_SENSITIVITY_NODE
import org.settings.oplusparts.TOUCH_SMOOTHING_NODE

object TouchUtils {

    // Game Touch Mode
    fun isGameTouchSupported(): Boolean = FileUtils.fileExists(GAME_TOUCH_NODE)

    fun isGameTouchEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(PREF_GAME_TOUCH_KEY, false)
    }

    fun getHardwareGameTouch(): Boolean {
        val line = FileUtils.readOneLine(GAME_TOUCH_NODE) ?: return false
        val token = line.substringBefore(",").trim()
        val intVal = token.toIntOrNull(16) ?: 0
        return intVal > 0
    }

    fun setGameTouchEnabled(context: Context, enabled: Boolean): Boolean {
        val success = FileUtils.writeLine(GAME_TOUCH_NODE, if (enabled) "1" else "0")
        if (success) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().putBoolean(PREF_GAME_TOUCH_KEY, enabled).apply()
            GameTouchTileService.updateTile(context)
        }
        return success
    }

    // Touch Sensitivity (Level 0 - 5)
    fun isSensitivitySupported(): Boolean = FileUtils.fileExists(TOUCH_SENSITIVITY_NODE)

    fun getSensitivityLevel(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(PREF_TOUCH_SENSITIVITY_KEY, 0)
    }

    fun getHardwareSensitivity(): Int {
        val line = FileUtils.readOneLine(TOUCH_SENSITIVITY_NODE)?.trim() ?: return 0
        val level = line.toIntOrNull() ?: 0
        return if (level < 0) 0 else level.coerceIn(0, 5)
    }

    fun setSensitivityLevel(context: Context, level: Int): Boolean {
        val target = level.coerceIn(0, 5)
        val success = FileUtils.writeLine(TOUCH_SENSITIVITY_NODE, target.toString())
        if (success) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().putInt(PREF_TOUCH_SENSITIVITY_KEY, target).apply()
        }
        return success
    }

    // Touch Smoothing (Level 0 - 5)
    fun isSmoothingSupported(): Boolean = FileUtils.fileExists(TOUCH_SMOOTHING_NODE)

    fun getSmoothingLevel(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(PREF_TOUCH_SMOOTHING_KEY, 0)
    }

    fun getHardwareSmoothing(): Int {
        val line = FileUtils.readOneLine(TOUCH_SMOOTHING_NODE)?.trim() ?: return 0
        val level = line.toIntOrNull() ?: 0
        return if (level < 0) 0 else level.coerceIn(0, 5)
    }

    fun setSmoothingLevel(context: Context, level: Int): Boolean {
        val target = level.coerceIn(0, 5)
        val success = FileUtils.writeLine(TOUCH_SMOOTHING_NODE, target.toString())
        if (success) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().putInt(PREF_TOUCH_SMOOTHING_KEY, target).apply()
        }
        return success
    }

    // Glove Mode
    fun isGloveModeSupported(): Boolean = FileUtils.fileExists(GLOVE_MODE_NODE)

    fun isGloveModeEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(PREF_GLOVE_MODE_KEY, false)
    }

    fun getHardwareGloveMode(): Boolean {
        val line = FileUtils.readOneLine(GLOVE_MODE_NODE)?.trim() ?: return false
        return line == "1"
    }

    fun setGloveModeEnabled(context: Context, enabled: Boolean): Boolean {
        val success = FileUtils.writeLine(GLOVE_MODE_NODE, if (enabled) "1" else "0")
        if (success) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().putBoolean(PREF_GLOVE_MODE_KEY, enabled).apply()
            GloveModeTileService.updateTile(context)
        }
        return success
    }

    // General support check
    fun isSupported(): Boolean =
        isGameTouchSupported() || isSensitivitySupported() || isSmoothingSupported() || isGloveModeSupported()

    fun restoreOnBoot(context: Context) {
        if (isGameTouchSupported() && isGameTouchEnabled(context)) {
            FileUtils.writeLine(GAME_TOUCH_NODE, "1")
        }
        if (isSensitivitySupported()) {
            val level = getSensitivityLevel(context)
            if (level != 0) {
                FileUtils.writeLine(TOUCH_SENSITIVITY_NODE, level.toString())
            }
        }
        if (isSmoothingSupported()) {
            val level = getSmoothingLevel(context)
            if (level != 0) {
                FileUtils.writeLine(TOUCH_SMOOTHING_NODE, level.toString())
            }
        }
        if (isGloveModeSupported() && isGloveModeEnabled(context)) {
            FileUtils.writeLine(GLOVE_MODE_NODE, "1")
        }
    }
}

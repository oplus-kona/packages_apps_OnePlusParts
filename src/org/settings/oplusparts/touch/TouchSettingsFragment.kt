/*
 * Copyright (C) 2018-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.settings.oplusparts.touch

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import com.android.settingslib.widget.SliderPreference
import org.settings.oplusparts.PREF_GAME_TOUCH_KEY
import org.settings.oplusparts.PREF_GLOVE_MODE_KEY
import org.settings.oplusparts.R

private const val PREF_TOUCH_SENSITIVITY_STATUS = "touch_sensitivity_status"
private const val PREF_TOUCH_SENSITIVITY_SLIDER = "touch_sensitivity_slider"
private const val PREF_TOUCH_SMOOTHING_STATUS = "touch_smoothing_status"
private const val PREF_TOUCH_SMOOTHING_SLIDER = "touch_smoothing_slider"
private const val CATEGORY_TOUCH_MODES = "touch_modes_category"
private const val CATEGORY_TOUCH_TUNING = "touch_tuning_category"

class TouchSettingsFragment : SettingsBasePreferenceFragment(),
    Preference.OnPreferenceChangeListener {

    private var gameTouchPref: SwitchPreferenceCompat? = null
    private var gloveModePref: SwitchPreferenceCompat? = null
    private var sensitivityStatusPref: Preference? = null
    private var sensitivitySliderPref: SliderPreference? = null
    private var smoothingStatusPref: Preference? = null
    private var smoothingSliderPref: SliderPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.touch_settings, rootKey)

        val modesCategory = findPreference<PreferenceCategory>(CATEGORY_TOUCH_MODES)
        val tuningCategory = findPreference<PreferenceCategory>(CATEGORY_TOUCH_TUNING)

        // Game Touch Mode
        gameTouchPref = findPreference(PREF_GAME_TOUCH_KEY)
        if (gameTouchPref != null) {
            if (!TouchUtils.isGameTouchSupported()) {
                modesCategory?.removePreference(gameTouchPref!!)
                gameTouchPref = null
            } else {
                gameTouchPref?.isChecked = TouchUtils.isGameTouchEnabled(requireContext())
                gameTouchPref?.onPreferenceChangeListener = this
            }
        }

        // Glove Mode
        gloveModePref = findPreference(PREF_GLOVE_MODE_KEY)
        if (gloveModePref != null) {
            if (!TouchUtils.isGloveModeSupported()) {
                modesCategory?.removePreference(gloveModePref!!)
                gloveModePref = null
            } else {
                gloveModePref?.isChecked = TouchUtils.isGloveModeEnabled(requireContext())
                gloveModePref?.onPreferenceChangeListener = this
            }
        }

        if (modesCategory != null && modesCategory.preferenceCount == 0) {
            preferenceScreen.removePreference(modesCategory)
        }

        // Touch Sensitivity Slider
        sensitivityStatusPref = findPreference(PREF_TOUCH_SENSITIVITY_STATUS)
        sensitivitySliderPref = findPreference(PREF_TOUCH_SENSITIVITY_SLIDER)
        if (sensitivitySliderPref != null) {
            if (!TouchUtils.isSensitivitySupported()) {
                sensitivityStatusPref?.let { tuningCategory?.removePreference(it) }
                tuningCategory?.removePreference(sensitivitySliderPref!!)
                sensitivityStatusPref = null
                sensitivitySliderPref = null
            } else {
                sensitivitySliderPref?.min = 0
                sensitivitySliderPref?.max = 5
                sensitivitySliderPref?.sliderIncrement = 1
                sensitivitySliderPref?.setTickVisible(true)
                sensitivitySliderPref?.setHapticFeedbackMode(
                    SliderPreference.HAPTIC_FEEDBACK_MODE_ON_TICKS
                )
                val currentLevel = TouchUtils.getSensitivityLevel(requireContext())
                sensitivitySliderPref?.value = currentLevel
                sensitivitySliderPref?.onPreferenceChangeListener = this
                updateSensitivityStatus(currentLevel)
            }
        }

        // Touch Smoothing Slider
        smoothingStatusPref = findPreference(PREF_TOUCH_SMOOTHING_STATUS)
        smoothingSliderPref = findPreference(PREF_TOUCH_SMOOTHING_SLIDER)
        if (smoothingSliderPref != null) {
            if (!TouchUtils.isSmoothingSupported()) {
                smoothingStatusPref?.let { tuningCategory?.removePreference(it) }
                tuningCategory?.removePreference(smoothingSliderPref!!)
                smoothingStatusPref = null
                smoothingSliderPref = null
            } else {
                smoothingSliderPref?.min = 0
                smoothingSliderPref?.max = 5
                smoothingSliderPref?.sliderIncrement = 1
                smoothingSliderPref?.setTickVisible(true)
                smoothingSliderPref?.setHapticFeedbackMode(
                    SliderPreference.HAPTIC_FEEDBACK_MODE_ON_TICKS
                )
                val currentLevel = TouchUtils.getSmoothingLevel(requireContext())
                smoothingSliderPref?.value = currentLevel
                smoothingSliderPref?.onPreferenceChangeListener = this
                updateSmoothingStatus(currentLevel)
            }
        }

        if (tuningCategory != null && tuningCategory.preferenceCount == 0) {
            preferenceScreen.removePreference(tuningCategory)
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        return when (preference.key) {
            PREF_GAME_TOUCH_KEY -> {
                val enabled = newValue as Boolean
                TouchUtils.setGameTouchEnabled(requireContext(), enabled)
            }
            PREF_GLOVE_MODE_KEY -> {
                val enabled = newValue as Boolean
                TouchUtils.setGloveModeEnabled(requireContext(), enabled)
            }
            PREF_TOUCH_SENSITIVITY_SLIDER -> {
                val level = (newValue as Int).coerceIn(0, 5)
                val success = TouchUtils.setSensitivityLevel(requireContext(), level)
                if (success) {
                    updateSensitivityStatus(level)
                }
                success
            }
            PREF_TOUCH_SMOOTHING_SLIDER -> {
                val level = (newValue as Int).coerceIn(0, 5)
                val success = TouchUtils.setSmoothingLevel(requireContext(), level)
                if (success) {
                    updateSmoothingStatus(level)
                }
                success
            }
            else -> false
        }
    }

    private fun updateSensitivityStatus(level: Int) {
        val levelName = when (level) {
            0 -> getString(R.string.touch_sensitivity_level_0)
            1 -> getString(R.string.touch_sensitivity_level_1)
            2 -> getString(R.string.touch_sensitivity_level_2)
            3 -> getString(R.string.touch_sensitivity_level_3)
            4 -> getString(R.string.touch_sensitivity_level_4)
            5 -> getString(R.string.touch_sensitivity_level_5)
            else -> getString(R.string.touch_sensitivity_level_0)
        }
        sensitivityStatusPref?.summary = getString(
            R.string.touch_sensitivity_status_format,
            level,
            levelName
        )
    }

    private fun updateSmoothingStatus(level: Int) {
        val levelName = when (level) {
            0 -> getString(R.string.touch_smoothing_level_0)
            1 -> getString(R.string.touch_smoothing_level_1)
            2 -> getString(R.string.touch_smoothing_level_2)
            3 -> getString(R.string.touch_smoothing_level_3)
            4 -> getString(R.string.touch_smoothing_level_4)
            5 -> getString(R.string.touch_smoothing_level_5)
            else -> getString(R.string.touch_smoothing_level_0)
        }
        smoothingStatusPref?.summary = getString(
            R.string.touch_smoothing_status_format,
            level,
            levelName
        )
    }
}

/*
 * Copyright (C) 2018-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.device.vibration

import android.os.Bundle
import androidx.preference.Preference
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import com.android.settingslib.widget.SliderPreference
import org.lineageos.settings.device.R

private const val PREF_STATUS = "vibrator_strength_status"
private const val PREF_SLIDER = "vibrator_strength_slider"
private const val PREF_TEST = "vibrator_test_button"

class VibratorStrengthFragment : SettingsBasePreferenceFragment(),
    Preference.OnPreferenceChangeListener,
    Preference.OnPreferenceClickListener {

    private lateinit var statusPref: Preference
    private lateinit var sliderPref: SliderPreference
    private lateinit var testPref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.vibrator_strength_settings, rootKey)

        statusPref = findPreference(PREF_STATUS)!!
        sliderPref = findPreference(PREF_SLIDER)!!
        testPref = findPreference(PREF_TEST)!!

        sliderPref.min = VibratorUtils.MIN_STRENGTH
        sliderPref.max = VibratorUtils.MAX_STRENGTH
        sliderPref.sliderIncrement = 5
        sliderPref.setTickVisible(true)
        sliderPref.setHapticFeedbackMode(SliderPreference.HAPTIC_FEEDBACK_MODE_ON_TICKS)

        val currentStrength = VibratorUtils.getStrength(requireContext())
        sliderPref.value = currentStrength
        sliderPref.onPreferenceChangeListener = this
        testPref.onPreferenceClickListener = this

        updateStatus(currentStrength)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        if (preference.key == PREF_SLIDER) {
            val percent = (newValue as Int).coerceIn(
                VibratorUtils.MIN_STRENGTH,
                VibratorUtils.MAX_STRENGTH
            )
            val success = VibratorUtils.setStrength(requireContext(), percent)
            if (success) {
                updateStatus(percent)
                VibratorUtils.testVibration(requireContext())
            }
            return success
        }
        return false
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (preference.key == PREF_TEST) {
            VibratorUtils.testVibration(requireContext())
            return true
        }
        return false
    }

    private fun updateStatus(percent: Int) {
        val levelDesc = when {
            percent == 0 -> getString(R.string.vibrator_level_off)
            percent <= 25 -> getString(R.string.vibrator_level_light)
            percent <= 60 -> getString(R.string.vibrator_level_medium)
            percent <= 85 -> getString(R.string.vibrator_level_strong)
            else -> getString(R.string.vibrator_level_maximum)
        }
        statusPref.summary = getString(R.string.vibrator_strength_status_format, percent, levelDesc)
    }
}

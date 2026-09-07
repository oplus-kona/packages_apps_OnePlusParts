/*
 * Copyright (C) 2018-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.device

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.lineageos.settings.device.display.DcDimmingUtils
import org.lineageos.settings.device.display.HbmUtils
import org.lineageos.settings.device.power.BypassChargingUtils
import org.lineageos.settings.device.power.GentleChargingUtils
import org.lineageos.settings.device.touch.TouchUtils

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_BOOT_COMPLETED -> {
                DcDimmingUtils.restoreOnBoot(context)
                HbmUtils.restoreOnBoot(context)
                BypassChargingUtils.restoreOnBoot(context)
                GentleChargingUtils.restoreOnBoot(context)
                TouchUtils.restoreOnBoot(context)
            }
        }
    }
}



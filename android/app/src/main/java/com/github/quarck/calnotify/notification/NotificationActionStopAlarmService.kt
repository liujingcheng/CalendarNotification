//
//   Calendar Notifications Plus
//   Copyright (C) 2026
//
//   This program is free software; you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation; either version 3 of the License, or
//   (at your option) any later version.
//

package com.github.quarck.calnotify.notification

import android.app.IntentService
import android.content.Intent
import com.github.quarck.calnotify.logs.DevLog

/** Stops the currently playing Xiaomi alarm fallback without dismissing its notification. */
class NotificationActionStopAlarmService : IntentService("NotificationActionStopAlarmService") {

    override fun onHandleIntent(intent: Intent?) {
        XiaomiAlarmSoundFallback.stop()
        DevLog.info(LOG_TAG, "Alarm sound stopped from notification action")
    }

    companion object {
        private const val LOG_TAG = "NotificationStopAlarm"
    }
}

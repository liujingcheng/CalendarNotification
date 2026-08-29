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

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * HyperOS may acknowledge an alarm notification but add FLAG_SILENT before
 * dispatching it, even when the channel uses USAGE_ALARM. On Xiaomi devices we
 * therefore play the configured system alarm sound directly as a fallback.
 */
object XiaomiAlarmSoundFallback {

    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private var currentRingtone: Ringtone? = null
    private var monitoredNotificationId: Int? = null
    private var monitoredContext: Context? = null

    private val stopRunnable = Runnable { stopOnMainThread() }
    private val notificationMonitorRunnable = object : Runnable {
        override fun run() {
            val context = monitoredContext
            val notificationId = monitoredNotificationId
            if (context == null || notificationId == null || currentRingtone == null) return

            if (!isNotificationActive(context, notificationId)) {
                Log.i(LOG_TAG, "Stopping Xiaomi alarm sound because notification $notificationId was removed")
                stopOnMainThread()
                return
            }

            handler.postDelayed(this, NOTIFICATION_MONITOR_INTERVAL_MS)
        }
    }

    fun play(context: Context, notificationId: Int) {
        if (!isXiaomiDevice(Build.MANUFACTURER)) return

        val appContext = context.applicationContext
        handler.post {
            try {
                stopOnMainThread()

                val soundUri = RingtoneManager.getActualDefaultRingtoneUri(
                    appContext,
                    RingtoneManager.TYPE_ALARM
                ) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                  ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                val ringtone = RingtoneManager.getRingtone(appContext, soundUri) ?: return@post
                ringtone.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone.isLooping = true
                }

                currentRingtone = ringtone
                monitoredContext = appContext
                monitoredNotificationId = notificationId
                ringtone.play()
                handler.postDelayed(stopRunnable, MAX_PLAY_DURATION_MS)
                // play() runs just before NotificationManager.notify(), so wait for
                // the notification to be posted before starting lifecycle checks.
                handler.postDelayed(notificationMonitorRunnable, NOTIFICATION_MONITOR_INITIAL_DELAY_MS)
                Log.i(LOG_TAG, "Started Xiaomi alarm sound fallback")
            } catch (exception: Exception) {
                currentRingtone = null
                Log.e(LOG_TAG, "Unable to play Xiaomi alarm sound fallback", exception)
            }
        }
    }

    fun stop() {
        handler.post { stopOnMainThread() }
    }

    internal fun isXiaomiDevice(manufacturer: String?): Boolean =
        manufacturer.equals("Xiaomi", ignoreCase = true)

    private fun stopOnMainThread() {
        handler.removeCallbacks(stopRunnable)
        handler.removeCallbacks(notificationMonitorRunnable)
        try {
            currentRingtone?.stop()
        } catch (exception: Exception) {
            Log.w(LOG_TAG, "Unable to stop Xiaomi alarm sound fallback", exception)
        } finally {
            currentRingtone = null
            monitoredContext = null
            monitoredNotificationId = null
        }
    }

    private fun isNotificationActive(context: Context, notificationId: Int): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true

        return try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.activeNotifications.any {
                it.id == notificationId && it.tag == null
            }
        } catch (exception: Exception) {
            Log.w(LOG_TAG, "Unable to check Xiaomi alarm notification state", exception)
            true
        }
    }

    internal const val MAX_PLAY_DURATION_MS = 2 * 60_000L
    internal const val NOTIFICATION_MONITOR_INITIAL_DELAY_MS = 1_000L
    internal const val NOTIFICATION_MONITOR_INTERVAL_MS = 250L
    private const val LOG_TAG = "XiaomiAlarmSound"
}

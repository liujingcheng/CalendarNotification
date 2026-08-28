//
//   Calendar Notifications Plus
//   Copyright (C) 2026
//
//   This program is free software; you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation; either version 3 of the License, or
//   (at your option) any later version.
//

package com.github.quarck.calnotify.calendar

import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import org.json.JSONObject

/**
 * Reads the alarm toggle used by Xiaomi Calendar.
 *
 * Xiaomi stores this separately from the standard calendar reminder row, in an
 * ExtendedProperties entry named "agenda_info".  The value is JSON containing
 * a boolean "need_alarm" member.
 */
object XiaomiCalendarAlarmDetector {

    fun isAlarm(context: Context, eventId: Long): Boolean {
        if (eventId <= 0L) return false

        return try {
            context.contentResolver.query(
                CalendarContract.ExtendedProperties.CONTENT_URI,
                arrayOf(CalendarContract.ExtendedProperties.VALUE),
                "${CalendarContract.ExtendedProperties.EVENT_ID}=? AND " +
                    "${CalendarContract.ExtendedProperties.NAME}=?",
                arrayOf(eventId.toString(), AGENDA_INFO_PROPERTY),
                null
            )?.use { cursor ->
                val valueColumn = cursor.getColumnIndex(CalendarContract.ExtendedProperties.VALUE)
                while (valueColumn >= 0 && cursor.moveToNext()) {
                    if (valueEnablesAlarm(cursor.getString(valueColumn))) return@use true
                }
                false
            } ?: false
        } catch (exception: Exception) {
            // ExtendedProperties is optional and vendor-specific. Other calendar
            // providers may reject this query, so retain the portable #alarm path.
            Log.d(LOG_TAG, "Unable to read Xiaomi alarm flag for event $eventId", exception)
            false
        }
    }

    internal fun valueEnablesAlarm(value: String?): Boolean {
        if (value.isNullOrBlank()) return false
        return try {
            JSONObject(value).optBoolean(NEED_ALARM_FIELD, false)
        } catch (_: Exception) {
            false
        }
    }

    private const val AGENDA_INFO_PROPERTY = "agenda_info"
    private const val NEED_ALARM_FIELD = "need_alarm"
    private const val LOG_TAG = "XiaomiAlarmDetector"
}

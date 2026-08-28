package com.github.quarck.calnotify.notification

import java.util.Calendar
import java.util.TimeZone

internal object SnoozeTargetTimeCalculator {

    fun delayUntilHour(
        now: Long,
        hourOfDay: Int,
        forceTomorrow: Boolean,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long {
        require(hourOfDay in 0..23)

        val target = Calendar.getInstance(timeZone).apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (forceTomorrow || target.timeInMillis <= now) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return target.timeInMillis - now
    }
}

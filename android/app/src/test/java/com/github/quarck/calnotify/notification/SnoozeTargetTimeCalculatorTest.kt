package com.github.quarck.calnotify.notification

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

class SnoozeTargetTimeCalculatorTest {

    private val utc = TimeZone.getTimeZone("UTC")

    @Test
    fun `uses today's target hour when it is still ahead`() {
        val now = timeAt(14, 0)

        val delay = SnoozeTargetTimeCalculator.delayUntilHour(now, 15, false, utc)

        assertEquals(60L * 60L * 1000L, delay)
    }

    @Test
    fun `uses tomorrow's target hour when today's has passed`() {
        val now = timeAt(16, 0)

        val delay = SnoozeTargetTimeCalculator.delayUntilHour(now, 15, false, utc)

        assertEquals(23L * 60L * 60L * 1000L, delay)
    }

    @Test
    fun `tomorrow shortcut always advances one calendar day`() {
        val now = timeAt(9, 0)

        val delay = SnoozeTargetTimeCalculator.delayUntilHour(now, 10, true, utc)

        assertEquals(25L * 60L * 60L * 1000L, delay)
    }

    private fun timeAt(hour: Int, minute: Int): Long =
        GregorianCalendar(utc).apply {
            clear()
            set(2026, Calendar.AUGUST, 28, hour, minute, 0)
        }.timeInMillis
}

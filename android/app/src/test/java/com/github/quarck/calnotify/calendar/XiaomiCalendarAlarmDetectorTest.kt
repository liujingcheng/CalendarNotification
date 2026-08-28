package com.github.quarck.calnotify.calendar

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class XiaomiCalendarAlarmDetectorTest {

    @Test
    fun `recognizes enabled Xiaomi alarm`() {
        assertTrue(XiaomiCalendarAlarmDetector.valueEnablesAlarm("{\"need_alarm\":true}"))
    }

    @Test
    fun `does not treat disabled or malformed values as alarms`() {
        assertFalse(XiaomiCalendarAlarmDetector.valueEnablesAlarm("{\"need_alarm\":false}"))
        assertFalse(XiaomiCalendarAlarmDetector.valueEnablesAlarm("{}"))
        assertFalse(XiaomiCalendarAlarmDetector.valueEnablesAlarm("not-json"))
        assertFalse(XiaomiCalendarAlarmDetector.valueEnablesAlarm(null))
    }
}

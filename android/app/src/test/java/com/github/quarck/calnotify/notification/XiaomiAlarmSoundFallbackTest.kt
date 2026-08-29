package com.github.quarck.calnotify.notification

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class XiaomiAlarmSoundFallbackTest {

    @Test
    fun `enables fallback only for Xiaomi manufacturer`() {
        assertTrue(XiaomiAlarmSoundFallback.isXiaomiDevice("Xiaomi"))
        assertTrue(XiaomiAlarmSoundFallback.isXiaomiDevice("xiaomi"))
        assertFalse(XiaomiAlarmSoundFallback.isXiaomiDevice("Google"))
        assertFalse(XiaomiAlarmSoundFallback.isXiaomiDevice(null))
    }

    @Test
    fun `limits fallback alarm sound to two minutes`() {
        assertEquals(120_000L, XiaomiAlarmSoundFallback.MAX_PLAY_DURATION_MS)
    }

    @Test
    fun `checks promptly for dismissed alarm notification`() {
        assertEquals(1_000L, XiaomiAlarmSoundFallback.NOTIFICATION_MONITOR_INITIAL_DELAY_MS)
        assertEquals(250L, XiaomiAlarmSoundFallback.NOTIFICATION_MONITOR_INTERVAL_MS)
    }
}

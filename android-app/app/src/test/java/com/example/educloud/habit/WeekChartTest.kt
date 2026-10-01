package com.example.educloud.habit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class WeekChartTest {

    @Test
    fun `last seven days end on today and light up only recorded days`() {
        val today = 20_000L
        val days = WeekChart.lastSevenDays(today, setOf(today - 1, today), Locale.US)
        assertEquals(7, days.size)
        assertEquals(today - 6, days.first().epochDay)
        assertTrue(days.last().isToday)
        assertTrue(days[5].active)
        assertTrue(days[6].active)
        assertFalse(days[0].active)
    }
}

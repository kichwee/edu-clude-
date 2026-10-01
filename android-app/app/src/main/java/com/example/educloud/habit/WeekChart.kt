package com.example.educloud.habit

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class WeekDayActivity(
    val epochDay: Long,
    val label: String,
    val dayOfMonth: Int,
    val active: Boolean,
    val isToday: Boolean,
)

/**
 * Seven-day activity chart for the Progress tab. Honest: a day lights up only
 * when [activeEpochDays] contains that local calendar day.
 */
object WeekChart {
    fun lastSevenDays(
        todayEpochDay: Long,
        activeEpochDays: Set<Long>,
        locale: Locale = Locale.US,
    ): List<WeekDayActivity> {
        val start = todayEpochDay - 6
        return (0..6).map { offset ->
            val day = start + offset
            val date = LocalDate.ofEpochDay(day)
            WeekDayActivity(
                epochDay = day,
                label = date.dayOfWeek.getDisplayName(TextStyle.NARROW, locale),
                dayOfMonth = date.dayOfMonth,
                active = day in activeEpochDays,
                isToday = day == todayEpochDay,
            )
        }
    }
}

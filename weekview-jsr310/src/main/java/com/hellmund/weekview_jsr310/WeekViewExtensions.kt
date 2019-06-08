package com.hellmund.weekview_jsr310

import com.alamkanak.weekview.OnMonthChangeListener
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date

fun <T> WeekViewEvent.Builder<T>.setStartTime(startTime: LocalDateTime): WeekViewEvent.Builder<T> {
    return setStartTime(startTime.toCalendar())
}

fun <T> WeekViewEvent.Builder<T>.setEndTime(endTime: LocalDateTime): WeekViewEvent.Builder<T> {
    return setEndTime(endTime.toCalendar())
}

fun <T> WeekView<T>.setOnMonthChangeListener(
    block: (
        startDate: LocalDate,
        endDate: LocalDate
    ) -> List<WeekViewDisplayable<T>>
) {
    onMonthChangeListener = object : OnMonthChangeListener<T> {
        override fun onMonthChange(
            startDate: Calendar,
            endDate: Calendar
        ): List<WeekViewDisplayable<T>> {
            return block(startDate.toLocalDate(), endDate.toLocalDate())
        }
    }
}

internal fun Calendar.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate()
}

internal fun LocalDateTime.toCalendar(): Calendar {
    val instant = atZone(ZoneId.systemDefault()).toInstant()
    val calendar = Calendar.getInstance()
    calendar.time = Date.from(instant)
    return calendar
}

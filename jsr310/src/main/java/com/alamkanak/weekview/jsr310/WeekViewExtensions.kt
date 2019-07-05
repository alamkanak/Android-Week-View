package com.alamkanak.weekview.jsr310

import com.alamkanak.weekview.OnEmptyViewClickListener
import com.alamkanak.weekview.OnEmptyViewLongPressListener
import com.alamkanak.weekview.OnMonthChangeListener
import com.alamkanak.weekview.ScrollListener
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

fun <T> WeekView<T>.goToDate(date: LocalDate) {
    goToDate(date.toCalendar())
}

fun <T> WeekView<T>.setScrollListener(
    block: (
        newFirstVisibleDay: LocalDate,
        oldFirstVisibleDay: LocalDate?
    ) -> Unit
) {
    scrollListener = object : ScrollListener {
        override fun onFirstVisibleDayChanged(
            newFirstVisibleDay: Calendar,
            oldFirstVisibleDay: Calendar?
        ) {
            block(newFirstVisibleDay.toLocalDate(), oldFirstVisibleDay?.toLocalDate())
        }
    }
}

fun <T> WeekView<T>.setOnEmptyViewClickListener(
    block: (time: LocalDateTime) -> Unit
) {
    onEmptyViewClickListener = object : OnEmptyViewClickListener {
        override fun onEmptyViewClicked(time: Calendar) {
            block(time.toLocalDateTime())
        }
    }
}

fun <T> WeekView<T>.setOnEmptyViewLongPressListener(
    block: (time: LocalDateTime) -> Unit
) {
    onEmptyViewLongPressListener = object : OnEmptyViewLongPressListener {
        override fun onEmptyViewLongPress(time: Calendar) {
            block(time.toLocalDateTime())
        }
    }
}

internal fun Calendar.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate()
}

internal fun Calendar.toLocalDateTime(): LocalDateTime {
    return Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
}

internal fun LocalDate.toCalendar(): Calendar {
    val instant = atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
    val calendar = Calendar.getInstance()
    calendar.time = Date.from(instant)
    return calendar
}

internal fun LocalDateTime.toCalendar(): Calendar {
    val instant = atZone(ZoneId.systemDefault()).toInstant()
    val calendar = Calendar.getInstance()
    calendar.time = Date.from(instant)
    return calendar
}

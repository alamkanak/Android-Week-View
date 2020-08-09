package com.alamkanak.weekview.jodatime

import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

fun <T : Any> WeekViewEvent.Builder<T>.setStartTime(startTime: LocalDateTime): WeekViewEvent.Builder<T> {
    return setStartTime(startTime.toCalendar())
}

fun <T : Any> WeekViewEvent.Builder<T>.setEndTime(endTime: LocalDateTime): WeekViewEvent.Builder<T> {
    return setEndTime(endTime.toCalendar())
}

val WeekView.jodaTimeAdapter: WeekViewJodaTimeAdapter
    get() = WeekViewJodaTimeAdapter(this)

fun WeekView.goToDate(date: LocalDate) {
    goToDate(date.toCalendar())
}

fun WeekView.setDateFormatter(formatter: (LocalDate) -> String) {
    setDateFormatter { formatter(it.toLocalDate()) }
}

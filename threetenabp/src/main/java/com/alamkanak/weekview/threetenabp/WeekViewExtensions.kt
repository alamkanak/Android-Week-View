package com.alamkanak.weekview.threetenabp

import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

fun <T : Any> WeekViewEvent.Builder<T>.setStartTime(startTime: LocalDateTime): WeekViewEvent.Builder<T> {
    return setStartTime(startTime.toCalendar())
}

fun <T : Any> WeekViewEvent.Builder<T>.setEndTime(endTime: LocalDateTime): WeekViewEvent.Builder<T> {
    return setEndTime(endTime.toCalendar())
}

var WeekView.minDateAsLocalDate: LocalDate?
    get() = minDate?.toLocalDate()
    set(value) {
        minDate = value?.toCalendar()
    }

var WeekView.maxDateAsLocalDate: LocalDate?
    get() = maxDate?.toLocalDate()
    set(value) {
        maxDate = value?.toCalendar()
    }

val WeekView.firstVisibleDateAsLocalDate: LocalDate
    get() = firstVisibleDate.toLocalDate()

val WeekView.lastVisibleDateAsLocalDate: LocalDate
    get() = lastVisibleDate.toLocalDate()

fun WeekView.goToDate(date: LocalDate) {
    goToDate(date.toCalendar())
}

fun WeekView.setDateFormatter(formatter: (LocalDate) -> String) {
    setDateFormatter { formatter(it.toLocalDate()) }
}

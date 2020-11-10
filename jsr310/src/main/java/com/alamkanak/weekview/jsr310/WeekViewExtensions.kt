package com.alamkanak.weekview.jsr310

import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun <T : Any> WeekViewEvent.Builder<T>.setStartTime(startTime: LocalDateTime): WeekViewEvent.Builder<T> {
    return setStartTime(startTime.toCalendar())
}

fun <T : Any> WeekViewEvent.Builder<T>.setEndTime(endTime: LocalDateTime): WeekViewEvent.Builder<T> {
    return setEndTime(endTime.toCalendar())
}

/**
 * Returns the minimum date that [WeekView] will display as a [LocalDate], or null if none is set.
 * Events before this date will not be shown.
 */
var WeekView.minDateAsLocalDate: LocalDate?
    get() = minDate?.toLocalDate()
    set(value) {
        minDate = value?.toCalendar()
    }

/**
 * Returns the maximum date that [WeekView] will display as a [LocalDate], or null if none is set.
 * Events after this date will not be shown.
 */
var WeekView.maxDateAsLocalDate: LocalDate?
    get() = maxDate?.toLocalDate()
    set(value) {
        maxDate = value?.toCalendar()
    }

/**
 * Returns the first visible date as a [LocalDate].
 */
val WeekView.firstVisibleDateAsLocalDate: LocalDate
    get() = firstVisibleDate.toLocalDate()

/**
 * Returns the last visible date as a [LocalDate].
 */
val WeekView.lastVisibleDateAsLocalDate: LocalDate
    get() = lastVisibleDate.toLocalDate()

/**
 * Scrolls to the specified date. If it is before [WeekView.minDate] or after [WeekView.maxDate],
 * these will be shown instead.
 *
 * @param date The [LocalDate] to show.
 */
@Deprecated(
    message = "This method has been renamed to scrollTo().",
    replaceWith = ReplaceWith(expression = "scrollTo")
)
fun WeekView.goToDate(date: LocalDate) {
    scrollToDate(date)
}

/**
 * Scrolls to the specified date. Any provided [LocalDate] that falls outside the range of
 * [WeekView.minDate] and [WeekView.maxDate] will be adjusted to fit into this range.
 *
 * @param date The [LocalDate] to scroll to.
 */
fun WeekView.scrollToDate(date: LocalDate) {
    scrollToDate(date.toCalendar())
}

/**
 * Scrolls to the specified date time. Any provided [LocalDateTime] that falls outside the range of
 * [WeekView.minDate] and [WeekView.maxDate], or [WeekView.minHour] and [WeekView.maxHour], will be
 * adjusted to fit into these ranges.
 *
 * @param dateTime The [LocalDateTime] to scroll to.
 */
fun WeekView.scrollToDateTime(dateTime: LocalDateTime) {
    scrollToDateTime(dateTime.toCalendar())
}

/**
 * Scrolls to the specified time. Any provided [LocalTime] that falls outside the range of
 * [WeekView.minHour] and [WeekView.maxHour] will be adjusted to fit into these ranges.
 *
 * @param time The [LocalTime] to scroll to.
 */
fun WeekView.scrollToTime(time: LocalTime) {
    scrollToTime(time.hour, time.minute)
}

fun WeekView.setDateFormatter(formatter: (LocalDate) -> String) {
    setDateFormatter { formatter(it.toLocalDate()) }
}

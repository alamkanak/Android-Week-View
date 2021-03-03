package com.alamkanak.weekview.jsr310

import com.alamkanak.weekview.PublicApi
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@PublicApi
fun <T : Any> WeekViewEntity.Event.Builder<T>.setStartTime(
    startTime: LocalDateTime
) = setStartTime(startTime.toCalendar())

@PublicApi
fun <T : Any> WeekViewEntity.Event.Builder<T>.setEndTime(
    endTime: LocalDateTime
) = setEndTime(endTime.toCalendar())

@PublicApi
fun WeekViewEntity.BlockedTime.Builder.setStartTime(
    startTime: LocalDateTime
) = setStartTime(startTime.toCalendar())

@PublicApi
fun WeekViewEntity.BlockedTime.Builder.setEndTime(
    endTime: LocalDateTime
) = setEndTime(endTime.toCalendar())

/**
 * Returns the minimum date that [WeekView] will display as a [LocalDate], or null if none is set.
 * Events before this date will not be shown.
 */
@PublicApi
var WeekView.minDateAsLocalDate: LocalDate?
    get() = minDate?.toLocalDate()
    set(value) {
        minDate = value?.toCalendar()
    }

/**
 * Returns the maximum date that [WeekView] will display as a [LocalDate], or null if none is set.
 * Events after this date will not be shown.
 */
@PublicApi
var WeekView.maxDateAsLocalDate: LocalDate?
    get() = maxDate?.toLocalDate()
    set(value) {
        maxDate = value?.toCalendar()
    }

/**
 * Returns the first visible date as a [LocalDate].
 */
@PublicApi
val WeekView.firstVisibleDateAsLocalDate: LocalDate
    get() = firstVisibleDate.toLocalDate()

/**
 * Returns the last visible date as a [LocalDate].
 */
@PublicApi
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
    replaceWith = ReplaceWith(expression = "scrollTo"),
    level = DeprecationLevel.ERROR
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
@PublicApi
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
@PublicApi
fun WeekView.scrollToDateTime(dateTime: LocalDateTime) {
    scrollToDateTime(dateTime.toCalendar())
}

/**
 * Scrolls to the specified time. Any provided [LocalTime] that falls outside the range of
 * [WeekView.minHour] and [WeekView.maxHour] will be adjusted to fit into these ranges.
 *
 * @param time The [LocalTime] to scroll to.
 */
@PublicApi
fun WeekView.scrollToTime(time: LocalTime) {
    scrollToTime(time.hour, time.minute)
}

@PublicApi
fun WeekView.setDateFormatter(formatter: (LocalDate) -> String) {
    setDateFormatter { formatter(it.toLocalDate()) }
}

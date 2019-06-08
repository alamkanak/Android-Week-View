package com.hellmund.weekview_jodatime

import com.alamkanak.weekview.OnMonthChangeListener
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.util.Calendar

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
    val dateTimeZone = DateTimeZone.forID(timeZone.id)
    val dateTime = DateTime(timeInMillis, dateTimeZone)
    return dateTime.toLocalDate()
}

internal fun LocalDateTime.toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = toDate()
    return calendar
}

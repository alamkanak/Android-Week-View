package com.alamkanak.weekview

import org.threeten.bp.ZonedDateTime
import java.util.*

internal fun <T> WeekViewEvent<T>.copy(
        startTime: Calendar = this.startTime,
        endTime: Calendar = this.endTime
): WeekViewEvent<T> {
    return WeekViewEvent.Builder<T>()
            .setId(id)
            .setTitle(title)
            .setStartTime(startTime)
            .setEndTime(endTime)
            .setAllDay(isAllDay)
            .setStyle(style)
            .setData(data)
            .build()
}

internal fun <T> WeekViewEvent<T>.copy(
        startTime: ZonedDateTime = this.startDateTime,
        endTime: ZonedDateTime = this.endDateTime
): WeekViewEvent<T> {
    return copy(startTime.toCalendar(), endTime.toCalendar())
}

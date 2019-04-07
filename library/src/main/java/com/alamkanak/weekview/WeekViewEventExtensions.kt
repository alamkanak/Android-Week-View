package com.alamkanak.weekview

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

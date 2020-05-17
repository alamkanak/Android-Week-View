package com.alamkanak.weekview.util

import com.alamkanak.weekview.ResolvedWeekViewEvent
import com.alamkanak.weekview.WeekViewEvent
import java.util.Calendar

internal fun createDate(
    year: Int,
    month: Int,
    dayOfMonth: Int
) = Calendar.getInstance().apply {
    set(Calendar.YEAR, year)
    set(Calendar.MONTH, month)
    set(Calendar.DAY_OF_MONTH, dayOfMonth)
}

internal fun createWeekViewEvent(
    startTime: Calendar,
    endTime: Calendar
): WeekViewEvent<Unit> {
    return WeekViewEvent(
        titleResource = WeekViewEvent.TextResource.Value("Title"),
        startTime = startTime,
        endTime = endTime,
        data = Unit
    )
}

internal fun createResolvedWeekViewEvent(
    startTime: Calendar,
    endTime: Calendar
): ResolvedWeekViewEvent<Unit> {
    return ResolvedWeekViewEvent(
        id = 0,
        title = "Title",
        startTime = startTime,
        endTime = endTime,
        location = null,
        isAllDay = false,
        style = ResolvedWeekViewEvent.Style(
            backgroundColor = 0,
            borderColor = 0,
            borderWidth = 0,
            textColor = 0,
            isTextStrikeThrough = false
        ),
        data = Unit
    )
}

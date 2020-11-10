package com.alamkanak.weekview.util

import com.alamkanak.weekview.ResolvedWeekViewEntity
import java.util.Calendar

internal fun createResolvedWeekViewEvent(
    startTime: Calendar,
    endTime: Calendar
): ResolvedWeekViewEntity = ResolvedWeekViewEntity.Event(
    id = 0,
    title = "Title",
    startTime = startTime,
    endTime = endTime,
    subtitle = null,
    isAllDay = false,
    style = ResolvedWeekViewEntity.Style(),
    data = Unit
)

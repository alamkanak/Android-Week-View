package com.alamkanak.weekview.model

import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import java.util.Calendar

data class Event(
    val startTime: Calendar,
    val endTime: Calendar
) : WeekViewDisplayable<Event> {

    override fun toWeekViewEvent(): WeekViewEvent<Event> {
        return WeekViewEvent.Builder(this)
            .setId(1)
            .setTitle("")
            .setStartTime(startTime)
            .setEndTime(endTime)
            .build()
    }
}

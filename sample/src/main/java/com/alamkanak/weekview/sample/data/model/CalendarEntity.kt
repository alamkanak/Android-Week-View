package com.alamkanak.weekview.sample.data.model

import java.util.Calendar

sealed class CalendarEntity {

    data class Event(
        val id: Long,
        val title: CharSequence,
        val startTime: Calendar,
        val endTime: Calendar,
        val location: CharSequence,
        val color: Int,
        val isAllDay: Boolean,
        val isCanceled: Boolean
    ) : CalendarEntity()

    data class BlockedTimeSlot(
        val id: Long,
        val startTime: Calendar,
        val endTime: Calendar
    ) : CalendarEntity()
}

package com.alamkanak.weekview.sample.apiclient

import android.graphics.Color
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import java.util.*

class Event(
        val id: Long,
        val title: String,
        val startTime: Calendar,
        val endTime: Calendar,
        val location: String,
        val color: Int,
        val isAllDay: Boolean
) : WeekViewDisplayable<Event> {

    override fun toWeekViewEvent(): WeekViewEvent<Event> {
        val style = WeekViewEvent.Style.Builder()
                .setTextColor(Color.WHITE)
                .setBackgroundColor(color)
                .setBorderWidth(3)
                .setBorderColor(Color.RED)
                .build()

        return WeekViewEvent.Builder<Event>()
                .setId(id)
                .setTitle(title)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setLocation(location)
                .setAllDay(isAllDay)
                .setStyle(style)
                .setData(this)
                .build()
    }

}

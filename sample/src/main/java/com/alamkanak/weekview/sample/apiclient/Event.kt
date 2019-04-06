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
        val isAllDay: Boolean,
        val isCanceled: Boolean
) : WeekViewDisplayable<Event> {

    override fun toWeekViewEvent(): WeekViewEvent<Event> {
        val backgroundColor = if (!isCanceled) color else Color.WHITE
        val textColor = if (!isCanceled) Color.WHITE else color
        val borderWidth = if (!isCanceled) 0 else 4

        val style = WeekViewEvent.Style.Builder()
                .setTextColor(textColor)
                .setBackgroundColor(backgroundColor)
                .setTextStrikeThrough(isCanceled)
                .setBorderWidth(borderWidth)
                .setBorderColor(color)
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

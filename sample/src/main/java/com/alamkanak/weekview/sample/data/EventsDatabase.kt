package com.alamkanak.weekview.sample.data

import android.content.Context
import androidx.core.content.ContextCompat
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.sample.R
import com.alamkanak.weekview.sample.data.model.Event
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar

class EventsDatabase(context: Context) {

    private val color1 = ContextCompat.getColor(context, R.color.event_color_01)
    private val color2 = ContextCompat.getColor(context, R.color.event_color_02)
    private val color3 = ContextCompat.getColor(context, R.color.event_color_03)
    private val color4 = ContextCompat.getColor(context, R.color.event_color_04)

    fun getEventsInRange(
        startDate: Calendar,
        endDate: Calendar
    ): List<WeekViewDisplayable<Event>> {
        val newYear = startDate.get(Calendar.YEAR)
        val newMonth = startDate.get(Calendar.MONTH)

        val idOffset = newYear + 10L * newMonth

        val events = mutableListOf<WeekViewDisplayable<Event>>()
        var event: Event

        var startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 28)
        startTime.set(Calendar.HOUR_OF_DAY, 16)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        var endTime = startTime.clone() as Calendar
        endTime.add(Calendar.MINUTE, 90)
        endTime.set(Calendar.MONTH, newMonth)

        event = createEvent(idOffset + 1, startTime, endTime, color1, isAllDay = false, isCanceled = false)
        events.add(event)

        // Add multi-day event
        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 28)
        startTime.set(Calendar.HOUR_OF_DAY, 20)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.DAY_OF_MONTH, 1)
        endTime.set(Calendar.HOUR_OF_DAY, 1)
        endTime.set(Calendar.MINUTE, 0)
        endTime.set(Calendar.MONTH, newMonth)

        event = createEvent(idOffset + 2, startTime, endTime, color4, isAllDay = false, isCanceled = false)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 28)
        startTime.set(Calendar.HOUR_OF_DAY, 9)
        startTime.set(Calendar.MINUTE, 30)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.set(Calendar.HOUR_OF_DAY, 10)
        endTime.set(Calendar.MINUTE, 30)
        endTime.set(Calendar.MONTH, newMonth)

        event = createEvent(idOffset + 3, startTime, endTime, color2, isAllDay = false, isCanceled = true)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 28)
        startTime.set(Calendar.HOUR_OF_DAY, 10)
        startTime.set(Calendar.MINUTE, 30)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.set(Calendar.HOUR_OF_DAY, 11)
        endTime.set(Calendar.MINUTE, 15)

        event = createEvent(idOffset + 4, startTime, endTime, color3, isAllDay = false, isCanceled = false)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 28)
        startTime.set(Calendar.HOUR_OF_DAY, 12)
        startTime.set(Calendar.MINUTE, 30)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 2)
        endTime.set(Calendar.MONTH, newMonth)

        event = createEvent(idOffset + 5, startTime, endTime, color2, isAllDay = false, isCanceled = false)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 17)
        startTime.set(Calendar.HOUR_OF_DAY, 11)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 4)

        event = createEvent(idOffset + 6, startTime, endTime, color3, isAllDay = false, isCanceled = false)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 15)
        startTime.set(Calendar.HOUR_OF_DAY, 3)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)

        event = createEvent(idOffset + 7, startTime, endTime, color4, isAllDay = false, isCanceled = true)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 1)
        startTime.set(Calendar.HOUR_OF_DAY, 9)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)

        event = createEvent(idOffset + 8, startTime, endTime, color1, isAllDay = false, isCanceled = false)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.YEAR, newYear)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.DAY_OF_MONTH, startTime.getActualMaximum(Calendar.DAY_OF_MONTH))
        startTime.set(Calendar.HOUR_OF_DAY, 15)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)

        event = createEvent(idOffset + 9, startTime, endTime, color2, isAllDay = false, isCanceled = false)
        events.add(event)

        // All-day event
        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 28)
        startTime.set(Calendar.HOUR_OF_DAY, 0)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 23)

        event = createEvent(idOffset + 10, startTime, endTime, color4, isAllDay = true, isCanceled = false)
        events.add(event)

        event = createEvent(idOffset + 11, startTime, endTime, color2, isAllDay = true, isCanceled = false)
        events.add(event)

        // All-day event until 00:00 next day
        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 14)
        startTime.set(Calendar.HOUR_OF_DAY, 0)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.SECOND, 0)
        startTime.set(Calendar.MILLISECOND, 0)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.DAY_OF_MONTH, 1)

        event = createEvent(idOffset + 12, startTime, endTime, color1, isAllDay = true, isCanceled = false)
        events.add(event)

        return events
    }

    private fun createEvent(
        id: Long,
        startTime: Calendar,
        endTime: Calendar,
        color: Int,
        isAllDay: Boolean,
        isCanceled: Boolean
    ) = Event(id, buildEventTitle(startTime), startTime, endTime, "Location $id", color, isAllDay, isCanceled)

    private fun buildEventTitle(time: Calendar): String {
        val sdf = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        val formattedDate = sdf.format(time.time)
        val hour = time.get(Calendar.HOUR_OF_DAY)
        val minute = time.get(Calendar.MINUTE)
        return String.format("Event of %02d:%02d %s", hour, minute, formattedDate)
    }
}

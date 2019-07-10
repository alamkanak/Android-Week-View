package com.alamkanak.weekview.sample.data

import android.content.Context
import androidx.core.content.ContextCompat
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.sample.R
import com.alamkanak.weekview.sample.apiclient.Event
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

interface EventsDatabase {
    fun getEventsInRange(startDate: Calendar, endDate: Calendar): List<WeekViewDisplayable<Event>>
}

class FakeEventsDatabase(private val context: Context) : EventsDatabase {

    override fun getEventsInRange(
        startDate: Calendar,
        endDate: Calendar
    ): List<WeekViewDisplayable<Event>> {
        val newYear = startDate.get(Calendar.YEAR)
        val newMonth = startDate.get(Calendar.MONTH)

        val color1 = ContextCompat.getColor(context, R.color.event_color_01)
        val color2 = ContextCompat.getColor(context, R.color.event_color_02)
        val color3 = ContextCompat.getColor(context, R.color.event_color_03)
        val color4 = ContextCompat.getColor(context, R.color.event_color_04)

        val events = mutableListOf<WeekViewDisplayable<Event>>()
        var event: Event

        var startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 8)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        var endTime = startTime.clone() as Calendar
        endTime.add(Calendar.MINUTE, 30)
        endTime.set(Calendar.MONTH, newMonth)

        event = Event(1, getEventTitle(startTime), startTime, endTime, "", color1, false, false)
        events.add(event)

        // Add multi-day event
        startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 17)
        startTime.set(Calendar.MINUTE, 30)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.DAY_OF_MONTH, 1)
        endTime.set(Calendar.HOUR_OF_DAY, 4)
        endTime.set(Calendar.MINUTE, 30)
        endTime.set(Calendar.MONTH, newMonth)

        event = Event(2, getEventTitle(startTime), startTime, endTime, "", color4, false, false)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 3)
        startTime.set(Calendar.MINUTE, 30)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.set(Calendar.HOUR_OF_DAY, 4)
        endTime.set(Calendar.MINUTE, 30)
        endTime.set(Calendar.MONTH, newMonth)

        event = Event(3, getEventTitle(startTime), startTime, endTime, "", color2, false, true)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 4)
        startTime.set(Calendar.MINUTE, 30)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.set(Calendar.HOUR_OF_DAY, 5)
        endTime.set(Calendar.MINUTE, 0)

        event = Event(4, getEventTitle(startTime), startTime, endTime, "", color3, false, false)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 5)
        startTime.set(Calendar.MINUTE, 30)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 2)
        endTime.set(Calendar.MONTH, newMonth)

        event = Event(5, getEventTitle(startTime), startTime, endTime, "", color2, false, false)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 5)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        startTime.add(Calendar.DATE, 1)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)
        endTime.set(Calendar.MONTH, newMonth)

        event = Event(6, getEventTitle(startTime), startTime, endTime, "", color3, false, false)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 15)
        startTime.set(Calendar.HOUR_OF_DAY, 3)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)

        event = Event(7, getEventTitle(startTime), startTime, endTime, "Location", color4, false, true)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 1)
        startTime.set(Calendar.HOUR_OF_DAY, 3)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)

        event = Event(8, getEventTitle(startTime), startTime, endTime, "", color1, false, false)
        events.add(event)

        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, startTime.getActualMaximum(Calendar.DAY_OF_MONTH))
        startTime.set(Calendar.HOUR_OF_DAY, 15)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 3)

        event = Event(9, getEventTitle(startTime), startTime, endTime, "", color2, false, false)
        events.add(event)

        // All-day event
        startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 0)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.add(Calendar.HOUR_OF_DAY, 23)

        event = Event(10, getEventTitle(startTime), startTime, endTime, "", color4, true, false)
        events.add(event)

        event = Event(11, getEventTitle(startTime), startTime, endTime, "", color2, true, false)
        events.add(event)

        // All-day event until 00:00 next day
        startTime = Calendar.getInstance()
        startTime.set(Calendar.DAY_OF_MONTH, 10)
        startTime.set(Calendar.HOUR_OF_DAY, 0)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.SECOND, 0)
        startTime.set(Calendar.MILLISECOND, 0)
        startTime.set(Calendar.MONTH, newMonth)
        startTime.set(Calendar.YEAR, newYear)
        endTime = startTime.clone() as Calendar
        endTime.set(Calendar.DAY_OF_MONTH, 11)

        event = Event(12, getEventTitle(startTime), startTime, endTime, "", color1, true, false)
        events.add(event)

        return events
    }

    private fun getEventTitle(time: Calendar): String {
        val sdf = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM)
        val formattedDate = sdf.format(time.time)

        val hour = time.get(Calendar.HOUR_OF_DAY)
        val minute = time.get(Calendar.MINUTE)

        return String.format(Locale.getDefault(), "Event of %02d:%02d %s", hour, minute, formattedDate)
    }

}

package com.alamkanak.weekview.sample.data

import android.content.Context
import androidx.core.content.ContextCompat
import com.alamkanak.weekview.sample.R
import com.alamkanak.weekview.sample.data.model.CalendarEntity
import com.alamkanak.weekview.sample.util.toCalendar
import java.util.Calendar
import java.util.TimeZone
import org.threeten.bp.LocalDate

class EventsDatabase(context: Context) {

    private val color1 = ContextCompat.getColor(context, R.color.event_color_01)
    private val color2 = ContextCompat.getColor(context, R.color.event_color_02)
    private val color3 = ContextCompat.getColor(context, R.color.event_color_03)
    private val color4 = ContextCompat.getColor(context, R.color.event_color_04)

    fun getEntitiesInRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CalendarEntity> {
        val events = getEventsInRange(startDate, endDate)
        val blockedTimes = getBlockedTimesInRange(startDate)
        return events + blockedTimes
    }

    private fun getEventsInRange(
        startDate: LocalDate,
        endDate: LocalDate
    ) = getEventsInRange(startDate.toCalendar(), endDate.toCalendar())

    private fun getBlockedTimesInRange(
        startDate: LocalDate
    ): List<CalendarEntity> {
        val start = startDate.toCalendar()
        val year = start.get(Calendar.YEAR)
        val month = start.get(Calendar.MONTH)
        val dayOfMonth = start.get(Calendar.DAY_OF_MONTH)

        return listOf(
            newBlockedTime(
                id = 123456789L,
                year = year,
                month = month,
                dayOfMonth = dayOfMonth,
                hour = 16,
                duration = 2 * 60
            ),
            newBlockedTime(
                id = 123456790L,
                year = year,
                month = month,
                dayOfMonth = dayOfMonth,
                hour = 19,
                duration = 2 * 60
            )
        )
    }

    fun getEventsInRange(
        startDate: Calendar,
        endDate: Calendar
    ): List<CalendarEntity.Event> {
        val monthStartDates = mutableListOf<Calendar>()
        val currentStartDate = startDate.clone() as Calendar
        while (currentStartDate < endDate) {
            monthStartDates.add(currentStartDate.clone() as Calendar)
            currentStartDate.add(Calendar.MONTH, 1)
        }
        return monthStartDates.flatMap(this::simulateEventsForRange)
    }

    private fun simulateEventsForRange(
        startDate: Calendar
    ): List<CalendarEntity.Event> {
        val year = startDate.get(Calendar.YEAR)
        val month = startDate.get(Calendar.MONTH)

        val idOffset = "$year${month}00".toLong()
        val events = mutableListOf<CalendarEntity.Event>()

        events += newEvent(
            id = idOffset + 1,
            year = year,
            month = month,
            dayOfMonth = 28,
            hour = 16,
            minute = 0,
            duration = 90,
            color = color1
        )

        // Add multi-day event
        events += newEvent(
            id = idOffset + 2,
            year = year,
            month = month,
            dayOfMonth = 27,
            hour = 20,
            minute = 0,
            duration = 5 * 60,
            color = color4
        )

        events += newEvent(
            id = idOffset + 3,
            year = year,
            month = month,
            dayOfMonth = 28,
            hour = 9,
            minute = 30,
            duration = 60,
            color = color4,
            isCanceled = true
        )

        events += newEvent(
            id = idOffset + 4,
            year = year,
            month = month,
            dayOfMonth = 28,
            hour = 10,
            minute = 30,
            duration = 45,
            color = color3
        )

        events += newEvent(
            id = idOffset + 5,
            year = year,
            month = month,
            dayOfMonth = 28,
            hour = 12,
            minute = 30,
            duration = 2 * 60,
            color = color2
        )

        events += newEvent(
            id = idOffset + 6,
            year = year,
            month = month,
            dayOfMonth = 17,
            hour = 11,
            minute = 0,
            duration = 4 * 60,
            color = color3
        )

        events += newEvent(
            id = idOffset + 7,
            year = year,
            month = month,
            dayOfMonth = 15,
            hour = 3,
            minute = 0,
            duration = 3 * 60,
            color = color4,
            isCanceled = true
        )

        events += newEvent(
            id = idOffset + 9,
            year = year,
            month = month,
            dayOfMonth = startDate.getActualMaximum(Calendar.DAY_OF_MONTH),
            hour = 15,
            minute = 0,
            duration = 3 * 60,
            color = color2
        )

        // All-day event
        events += newEvent(
            id = idOffset + 10,
            year = year,
            month = month,
            dayOfMonth = 28,
            hour = 0,
            minute = 0,
            duration = 24 * 60,
            isAllDay = true,
            color = color4
        )

        // All-day event
        events += newEvent(
            id = idOffset + 11,
            year = year,
            month = month,
            dayOfMonth = 28,
            hour = 0,
            minute = 0,
            duration = 24 * 60,
            isAllDay = true,
            color = color2
        )

        // All-day event
        events += newEvent(
            id = idOffset + 12,
            year = year,
            month = month,
            dayOfMonth = 28,
            hour = 0,
            minute = 0,
            duration = 24 * 60,
            isAllDay = true,
            color = color2
        )

        // All-day event until 00:00 next day
        events += newEvent(
            id = idOffset + 13,
            year = year,
            month = month,
            dayOfMonth = 14,
            hour = 0,
            minute = 0,
            duration = 10 * 60,
            isAllDay = true,
            color = color4
        )

        events += newEvent(
            id = idOffset + 13,
            year = year,
            month = month,
            dayOfMonth = 1,
            hour = 0,
            minute = 0,
            duration = 3 * 60,
            color = color1,
            title = "Event in London",
            timeZone = TimeZone.getTimeZone("Europe/London")
        )

        events += newEvent(
            id = idOffset + 14,
            year = year,
            month = month,
            dayOfMonth = 1,
            hour = 0,
            minute = 0,
            duration = 3 * 60,
            color = color2,
            title = "Event in Toronto",
            timeZone = TimeZone.getTimeZone("America/Toronto")
        )

        events += newEvent(
            id = idOffset + 15,
            year = year,
            month = month,
            dayOfMonth = 1,
            hour = 0,
            minute = 0,
            duration = 3 * 60,
            color = color3,
            title = "Event in LA",
            timeZone = TimeZone.getTimeZone("America/Los_Angeles")
        )

        return events
    }

    private fun newEvent(
        id: Long,
        year: Int,
        month: Int,
        dayOfMonth: Int,
        hour: Int,
        minute: Int,
        duration: Int,
        color: Int,
        timeZone: TimeZone = TimeZone.getDefault(),
        title: String = "Event $id",
        isAllDay: Boolean = false,
        isCanceled: Boolean = false
    ): CalendarEntity.Event {
        val startTime = Calendar.getInstance(timeZone).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endTime = startTime.clone() as Calendar
        endTime.add(Calendar.MINUTE, duration)

        return CalendarEntity.Event(
            id = id,
            title = title,
            startTime = startTime,
            endTime = endTime,
            location = "Location 123",
            color = color,
            isAllDay = isAllDay,
            isCanceled = isCanceled
        )
    }

    private fun newBlockedTime(
        id: Long,
        year: Int,
        month: Int,
        dayOfMonth: Int,
        hour: Int,
        minute: Int = 0,
        duration: Int
    ): CalendarEntity.BlockedTimeSlot {
        val startTime = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endTime = startTime.clone() as Calendar
        endTime.add(Calendar.MINUTE, duration)

        return CalendarEntity.BlockedTimeSlot(
            id = id,
            startTime = startTime,
            endTime = endTime
        )
    }
}

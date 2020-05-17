package com.alamkanak.weekview.sample.data

import android.content.Context
import android.text.SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.SpannableStringBuilder
import android.text.style.StrikethroughSpan
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
        val year = startDate.get(Calendar.YEAR)
        val month = startDate.get(Calendar.MONTH)

        val idOffset = year + 10L * month
        val events = mutableListOf<WeekViewDisplayable<Event>>()

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
            id = idOffset + 3,
            year = year,
            month = month,
            dayOfMonth = 28,
            hour = 9,
            minute = 30,
            duration = 60,
            color = color2
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
            id = idOffset + 8,
            year = year,
            month = month,
            dayOfMonth = 1,
            hour = 9,
            minute = 0,
            duration = 3 * 60,
            color = color1
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

        // All-day event until 00:00 next day
        events += newEvent(
            id = idOffset + 12,
            year = year,
            month = month,
            dayOfMonth = 14,
            hour = 0,
            minute = 0,
            duration = 10 * 60,
            isAllDay = true,
            color = color4
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
        isAllDay: Boolean = false,
        isCanceled: Boolean = false
    ): Event {
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

        val title = buildEventTitle(startTime)

        val spannableTitle = SpannableStringBuilder(title).apply {
//            setSpan(BackgroundColorSpan(Color.RED), 0, title.length, SPAN_EXCLUSIVE_EXCLUSIVE)
//            setSpan(StyleSpan(Typeface.BOLD_ITALIC), 0, title.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StrikethroughSpan(), 0, title.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return Event(
            id = id,
            title = spannableTitle,
            startTime = startTime,
            endTime = endTime,
            location = "Location $id",
            color = color,
            isAllDay = isAllDay,
            isCanceled = isCanceled
        )
    }

    private fun buildEventTitle(time: Calendar): String {
        val sdf = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM)
        val formattedDate = sdf.format(time.time)
        val hour = time.get(Calendar.HOUR_OF_DAY)
        val minute = time.get(Calendar.MINUTE)
        return String.format("🦄 Event of %02d:%02d %s", hour, minute, formattedDate)
    }
}

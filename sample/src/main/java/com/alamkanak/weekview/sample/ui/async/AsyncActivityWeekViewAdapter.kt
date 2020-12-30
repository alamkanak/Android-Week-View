package com.alamkanak.weekview.sample.ui.async

import android.graphics.Color
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.sample.data.model.ApiEvent
import com.alamkanak.weekview.sample.util.showToast
import com.alamkanak.weekview.threetenabp.WeekViewSimpleAdapterThreeTenAbp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

internal class AsyncActivityWeekViewAdapter(
    private val eventClickHandler: (ApiEvent) -> Unit
) : WeekViewSimpleAdapterThreeTenAbp<ApiEvent>() {

    private val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateEntity(item: ApiEvent): WeekViewEntity {
        val id = item.title.split(" ").last().toLong()

        val start = checkNotNull(timeFormat.parse(item.startTime))
        val end = checkNotNull(timeFormat.parse(item.endTime))

        val now = Calendar.getInstance()

        val startTime = now.clone() as Calendar
        startTime.timeInMillis = start.time
        startTime.set(Calendar.YEAR, now.get(Calendar.YEAR))
        startTime.set(Calendar.MONTH, now.get(Calendar.MONTH))
        startTime.set(Calendar.DAY_OF_MONTH, item.dayOfMonth)

        val endTime = startTime.clone() as Calendar
        endTime.timeInMillis = end.time
        endTime.set(Calendar.YEAR, startTime.get(Calendar.YEAR))
        endTime.set(Calendar.MONTH, startTime.get(Calendar.MONTH))
        endTime.set(Calendar.DAY_OF_MONTH, startTime.get(Calendar.DAY_OF_MONTH))

        val color = Color.parseColor(item.color)
        val style = WeekViewEntity.Style.Builder()
            .setBackgroundColor(color)
            .build()

        return WeekViewEntity.Event.Builder(item)
            .setId(id)
            .setTitle(item.title)
            .setStartTime(startTime)
            .setEndTime(endTime)
            .setAllDay(false)
            .setStyle(style)
            .build()
    }

    override fun onEventClick(data: ApiEvent) {
        eventClickHandler(data)
        context.showToast("Removed ${data.title}")
    }

    override fun onEmptyViewClick(time: LocalDateTime) {
        context.showToast("Empty view clicked at ${formatter.format(time)}")
    }

    override fun onEventLongClick(data: ApiEvent) {
        context.showToast("Long-clicked ${data.title}")
    }

    override fun onEmptyViewLongClick(time: LocalDateTime) {
        context.showToast("Empty view long-clicked at ${formatter.format(time)}")
    }
}

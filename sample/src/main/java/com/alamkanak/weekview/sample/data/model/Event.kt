package com.alamkanak.weekview.sample.data.model

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.StrikethroughSpan
import android.text.style.TypefaceSpan
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import com.alamkanak.weekview.sample.R
import java.util.Calendar

data class Event(
    val id: Long,
    val title: CharSequence,
    private val startTime: Calendar,
    private val endTime: Calendar,
    private val location: CharSequence,
    private val color: Int,
    private val isAllDay: Boolean,
    private val isCanceled: Boolean
) : WeekViewDisplayable<Event> {

    override fun toWeekViewEvent(): WeekViewEvent<Event> {
        val backgroundColor = if (!isCanceled) color else Color.WHITE
        val textColor = if (!isCanceled) Color.WHITE else color
        val borderWidthResId = if (!isCanceled) R.dimen.no_border_width else R.dimen.border_width

        val style = WeekViewEvent.Style.Builder()
            .setTextColor(textColor)
            .setBackgroundColor(backgroundColor)
            .setBorderWidthResource(borderWidthResId)
            .setBorderColor(color)
            .build()

        val styledTitle = SpannableStringBuilder(title).apply {
            val titleSpan = TypefaceSpan("sans-serif-medium")
            setSpan(titleSpan, 0, title.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            if (isCanceled) {
                setSpan(StrikethroughSpan(), 0, title.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        val styledLocation = SpannableStringBuilder(location).apply {
            if (isCanceled) {
                setSpan(StrikethroughSpan(), 0, title.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        return WeekViewEvent.Builder(this)
            .setId(id)
            .setTitle(styledTitle)
            .setStartTime(startTime)
            .setEndTime(endTime)
            .setLocation(styledLocation)
            .setAllDay(isAllDay)
            .setStyle(style)
            .build()
    }
}

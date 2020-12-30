package com.alamkanak.weekview.sample.ui.basic

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.text.style.TypefaceSpan
import androidx.core.content.ContextCompat
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.sample.R
import com.alamkanak.weekview.sample.data.model.CalendarEntity
import com.alamkanak.weekview.sample.util.showToast
import com.alamkanak.weekview.threetenabp.WeekViewPagingAdapterThreeTenAbp
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

internal class BasicActivityWeekViewAdapter(
    private val loadMoreHandler: (LoadParams) -> Unit
) : WeekViewPagingAdapterThreeTenAbp<CalendarEntity>() {

    private val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

    override fun onCreateEntity(item: CalendarEntity): WeekViewEntity {
        return when (item) {
            is CalendarEntity.Event -> createForEvent(item)
            is CalendarEntity.BlockedTimeSlot -> createForBlockedTimeSlot(item)
        }
    }

    private fun createForEvent(event: CalendarEntity.Event): WeekViewEntity {
        val backgroundColor = if (!event.isCanceled) event.color else Color.WHITE
        val textColor = if (!event.isCanceled) Color.WHITE else event.color
        val borderWidthResId = if (!event.isCanceled) R.dimen.no_border_width else R.dimen.border_width

        val style = WeekViewEntity.Style.Builder()
            .setTextColor(textColor)
            .setBackgroundColor(backgroundColor)
            .setBorderWidthResource(borderWidthResId)
            .setBorderColor(event.color)
            .build()

        val title = SpannableStringBuilder(event.title).apply {
            val titleSpan = TypefaceSpan("sans-serif-medium")
            setSpan(titleSpan, 0, event.title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (event.isCanceled) {
                setSpan(StrikethroughSpan(), 0, event.title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        val subtitle = SpannableStringBuilder(event.location).apply {
            if (event.isCanceled) {
                setSpan(StrikethroughSpan(), 0, event.location.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        return WeekViewEntity.Event.Builder(event)
            .setId(event.id)
            .setTitle(title)
            .setStartTime(event.startTime)
            .setEndTime(event.endTime)
            .setSubtitle(subtitle)
            .setAllDay(event.isAllDay)
            .setStyle(style)
            .build()
    }

    private fun createForBlockedTimeSlot(
        blockedTimeSlot: CalendarEntity.BlockedTimeSlot
    ): WeekViewEntity {
        val pattern = WeekViewEntity.Style.Pattern.Lined(
            color = ContextCompat.getColor(context, R.color.gray_500),
            strokeWidth = context.resources.getDimensionPixelSize(R.dimen.line_width),
            spacing = context.resources.getDimensionPixelSize(R.dimen.line_spacing),
            direction = WeekViewEntity.Style.Pattern.Lined.Direction.EndToStart
        )

        val style = WeekViewEntity.Style.Builder()
            .setPattern(pattern)
            .setBackgroundColorResource(R.color.gray_alpha10)
            .setCornerRadius(0)
            .build()

        return WeekViewEntity.BlockedTime.Builder()
            .setId(blockedTimeSlot.id)
            .setStartTime(blockedTimeSlot.startTime)
            .setEndTime(blockedTimeSlot.endTime)
            .setStyle(style)
            .build()
    }

    override fun onEventClick(data: CalendarEntity) {
        if (data is CalendarEntity.Event) {
            context.showToast("Clicked ${data.title}")
        }
    }

    override fun onEmptyViewClick(time: LocalDateTime) {
        context.showToast("Empty view clicked at ${formatter.format(time)}")
    }

    override fun onEventLongClick(data: CalendarEntity) {
        if (data is CalendarEntity.Event) {
            context.showToast("Long-clicked ${data.title}")
        }
    }

    override fun onEmptyViewLongClick(time: LocalDateTime) {
        context.showToast("Empty view long-clicked at ${formatter.format(time)}")
    }

    override fun onLoadMore(startDate: LocalDate, endDate: LocalDate) {
        val loadParams = LoadParams(startDate, endDate)
        loadMoreHandler(loadParams)
    }
}

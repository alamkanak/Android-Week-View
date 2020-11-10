package com.alamkanak.weekview.sample

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.text.style.TypefaceSpan
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.sample.data.EventsDatabase
import com.alamkanak.weekview.sample.data.model.CalendarEntity
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlinx.android.synthetic.main.activity_custom_font.weekView
import kotlinx.android.synthetic.main.view_toolbar.toolbar

class CustomFontActivity : AppCompatActivity() {

    private val database: EventsDatabase by lazy { EventsDatabase(this) }

    private val adapter: CustomFontActivityWeekViewAdapter by lazy {
        CustomFontActivityWeekViewAdapter(loadMoreHandler = this::onLoadMore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_font)
        toolbar.setupWithWeekView(weekView)
        weekView.adapter = adapter
    }

    private fun onLoadMore(startDate: Calendar, endDate: Calendar) {
        val events = database.getEventsInRange(startDate, endDate)
        adapter.submitList(events)
    }
}

private class CustomFontActivityWeekViewAdapter(
    private val loadMoreHandler: (startDate: Calendar, endDate: Calendar) -> Unit
) : WeekView.PagingAdapter<CalendarEntity.Event>() {

    private val formatter = SimpleDateFormat.getDateTimeInstance()

    override fun onCreateEntity(item: CalendarEntity.Event): WeekViewEntity {
        val backgroundColor = if (!item.isCanceled) item.color else Color.WHITE
        val textColor = if (!item.isCanceled) Color.WHITE else item.color
        val borderWidthResId = if (!item.isCanceled) R.dimen.no_border_width else R.dimen.border_width

        val style = WeekViewEntity.Style.Builder()
            .setTextColor(textColor)
            .setBackgroundColor(backgroundColor)
            .setBorderWidthResource(borderWidthResId)
            .setBorderColor(item.color)
            .build()

        val title = SpannableStringBuilder(item.title).apply {
            val titleSpan = TypefaceSpan("sans-serif-medium")
            setSpan(titleSpan, 0, item.title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (item.isCanceled) {
                setSpan(StrikethroughSpan(), 0, item.title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        val subtitle = SpannableStringBuilder(item.location).apply {
            if (item.isCanceled) {
                setSpan(StrikethroughSpan(), 0, item.location.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        return WeekViewEntity.Event.Builder(item)
            .setId(item.id)
            .setTitle(title)
            .setStartTime(item.startTime)
            .setEndTime(item.endTime)
            .setSubtitle(subtitle)
            .setAllDay(item.isAllDay)
            .setStyle(style)
            .build()
    }

    override fun onEventClick(data: CalendarEntity.Event) {
        context.showToast("Clicked ${data.title}")
    }

    override fun onEmptyViewClick(time: Calendar) {
        context.showToast("Empty view clicked at ${formatter.format(time.time)}")
    }

    override fun onEventLongClick(data: CalendarEntity.Event) {
        context.showToast("Long-clicked ${data.title}")
    }

    override fun onEmptyViewLongClick(time: Calendar) {
        context.showToast("Empty view long-clicked at ${formatter.format(time.time)}")
    }

    override fun onLoadMore(startDate: Calendar, endDate: Calendar) {
        loadMoreHandler(startDate, endDate)
    }
}

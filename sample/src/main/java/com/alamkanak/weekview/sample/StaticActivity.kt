package com.alamkanak.weekview.sample

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.text.style.TypefaceSpan
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.sample.data.model.CalendarEntity
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import com.alamkanak.weekview.threetenabp.WeekViewPagingAdapterThreeTenAbp
import com.alamkanak.weekview.threetenabp.firstVisibleDateAsLocalDate
import com.alamkanak.weekview.threetenabp.lastVisibleDateAsLocalDate
import java.util.Calendar
import kotlinx.android.synthetic.main.activity_static.dateRangeTextView
import kotlinx.android.synthetic.main.activity_static.leftNavigationButton
import kotlinx.android.synthetic.main.activity_static.rightNavigationButton
import kotlinx.android.synthetic.main.activity_static.weekView
import kotlinx.android.synthetic.main.view_toolbar.toolbar
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle.MEDIUM
import org.threeten.bp.format.FormatStyle.SHORT

class StaticActivity : AppCompatActivity() {

    private val eventsFetcher: EventsFetcher by lazy { EventsFetcher(this) }

    private val dateFormatter = DateTimeFormatter.ofLocalizedDate(MEDIUM)

    private val adapter: StaticActivityWeekViewAdapter by lazy {
        StaticActivityWeekViewAdapter(
            loadMoreHandler = this::onLoadMore,
            rangeChangeHandler = this::onRangeChanged
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_static)

        toolbar.setupWithWeekView(weekView)
        weekView.adapter = adapter

        dateRangeTextView.text = buildDateRangeText(
            startDate = weekView.firstVisibleDateAsLocalDate,
            endDate = weekView.lastVisibleDateAsLocalDate
        )

        leftNavigationButton.setOnClickListener {
            val cal = weekView.firstVisibleDate
            cal.add(Calendar.DATE, -7)
            weekView.goToDate(cal)
        }

        rightNavigationButton.setOnClickListener {
            val cal = weekView.firstVisibleDate
            cal.add(Calendar.DATE, 7)
            weekView.goToDate(cal)
        }
    }

    private fun onLoadMore(startDate: LocalDate, endDate: LocalDate) {
        eventsFetcher.fetch(startDate, endDate, adapter::submitList)
    }

    private fun onRangeChanged(startDate: LocalDate, endDate: LocalDate) {
        dateRangeTextView.text = buildDateRangeText(startDate, endDate)
    }

    private fun buildDateRangeText(startDate: LocalDate, endDate: LocalDate): String {
        val formattedFirstDay = dateFormatter.format(startDate)
        val formattedLastDay = dateFormatter.format(endDate)
        return getString(R.string.date_infos, formattedFirstDay, formattedLastDay)
    }
}

private class StaticActivityWeekViewAdapter(
    private val rangeChangeHandler: (startDate: LocalDate, endDate: LocalDate) -> Unit,
    private val loadMoreHandler: (startDate: LocalDate, endDate: LocalDate) -> Unit
) : WeekViewPagingAdapterThreeTenAbp<CalendarEntity.Event>() {

    private val formatter = DateTimeFormatter.ofLocalizedDateTime(MEDIUM, SHORT)

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

    override fun onEmptyViewClick(time: LocalDateTime) {
        context.showToast("Empty view clicked at ${formatter.format(time)}")
    }

    override fun onEventLongClick(data: CalendarEntity.Event) {
        context.showToast("Long-clicked ${data.title}")
    }

    override fun onEmptyViewLongClick(time: LocalDateTime) {
        context.showToast("Empty view long-clicked at ${formatter.format(time)}")
    }

    override fun onLoadMore(startDate: LocalDate, endDate: LocalDate) {
        loadMoreHandler(startDate, endDate)
    }

    override fun onRangeChanged(firstVisibleDate: LocalDate, lastVisibleDate: LocalDate) {
        rangeChangeHandler(firstVisibleDate, lastVisibleDate)
    }
}

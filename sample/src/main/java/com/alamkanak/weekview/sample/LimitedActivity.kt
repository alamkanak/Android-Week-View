package com.alamkanak.weekview.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.sample.data.model.Event
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import kotlinx.android.synthetic.main.activity_limited.weekView
import kotlinx.android.synthetic.main.view_toolbar.toolbar

class LimitedActivity : AppCompatActivity() {

    private val eventsFetcher: EventsFetcher by lazy { EventsFetcher(this) }

    private val adapter: LimitedActivityWeekViewAdapter by lazy {
        LimitedActivityWeekViewAdapter(loadMoreHandler = this::onLoadMore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_limited)

        toolbar.setupWithWeekView(weekView)
        setupDateRange()

        weekView.adapter = adapter
    }

    private fun setupDateRange() {
        val now = Calendar.getInstance()

        val min = now.clone() as Calendar
        min.set(DAY_OF_MONTH, 1)
        weekView.minDate = min

        val max = now.clone() as Calendar
        max.set(DAY_OF_MONTH, max.getActualMaximum(DAY_OF_MONTH))
        weekView.maxDate = max
    }

    private fun onLoadMore(startDate: Calendar, endDate: Calendar) {
        eventsFetcher.fetch(startDate, endDate, adapter::submit)
    }
}

private class LimitedActivityWeekViewAdapter(
    private val loadMoreHandler: (startDate: Calendar, endDate: Calendar) -> Unit
) : WeekView.PagingAdapter<Event>() {

    private val formatter = SimpleDateFormat.getDateTimeInstance()

    override fun onEventClick(data: Event) {
        context.showToast("Removed ${data.title}")
    }

    override fun onEmptyViewClick(time: Calendar) {
        context.showToast("Empty view clicked at ${formatter.format(time.time)}")
    }

    override fun onEventLongClick(data: Event) {
        context.showToast("Long-clicked ${data.title}")
    }

    override fun onEmptyViewLongClick(time: Calendar) {
        context.showToast("Empty view long-clicked at ${formatter.format(time.time)}")
    }

    override fun onLoadMore(startDate: Calendar, endDate: Calendar) {
        loadMoreHandler(startDate, endDate)
    }
}

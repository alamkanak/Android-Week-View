package com.alamkanak.weekview.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.sample.data.EventsDatabase
import com.alamkanak.weekview.sample.data.model.Event
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
        adapter.submit(events)
    }
}

private class CustomFontActivityWeekViewAdapter(
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

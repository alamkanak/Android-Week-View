package com.alamkanak.weekview.sample

import android.graphics.RectF
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.OnEmptyViewLongClickListener
import com.alamkanak.weekview.OnEventClickListener
import com.alamkanak.weekview.OnEventLongClickListener
import com.alamkanak.weekview.OnLoadMoreListener
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.sample.data.model.Event
import com.alamkanak.weekview.sample.util.lazyView
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import kotlinx.android.synthetic.main.view_toolbar.toolbar

class LimitedActivity : AppCompatActivity(), OnEventClickListener<Event>,
    OnLoadMoreListener, OnEventLongClickListener<Event>, OnEmptyViewLongClickListener {

    private val weekView: WeekView<Event> by lazyView(R.id.weekView)
    private val eventsFetcher: EventsFetcher by lazy { EventsFetcher(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_limited)

        toolbar.setupWithWeekView(weekView)

        weekView.onEventClickListener = this
        weekView.onLoadMoreListener = this
        weekView.onEventLongClickListener = this
        weekView.onEmptyViewLongClickListener = this

        setupDateRange()
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

    override fun onLoadMore(startDate: Calendar, endDate: Calendar) {
        eventsFetcher.fetch(startDate, endDate, weekView::submit)
    }

    override fun onEventClick(data: Event, eventRect: RectF) {
        showToast("Clicked ${data.title}")
    }

    override fun onEventLongClick(data: Event, eventRect: RectF) {
        showToast("Long-clicked ${data.title}")
        Toast.makeText(this, "Long pressed event: " + data.title, Toast.LENGTH_SHORT).show()
    }

    override fun onEmptyViewLongClick(time: Calendar) {
        val sdf = SimpleDateFormat.getDateTimeInstance()
        showToast("Empty view long-clicked at ${sdf.format(time.time)}")
    }
}

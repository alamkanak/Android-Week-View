package com.alamkanak.weekview.sample

import android.graphics.RectF
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.OnEmptyViewLongClickListener
import com.alamkanak.weekview.OnEventClickListener
import com.alamkanak.weekview.OnEventLongClickListener
import com.alamkanak.weekview.OnMonthChangeListener
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.sample.apiclient.Event
import com.alamkanak.weekview.sample.data.EventsDatabase
import com.alamkanak.weekview.sample.data.FakeEventsDatabase
import com.alamkanak.weekview.sample.util.lazyView
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import kotlinx.android.synthetic.main.view_toolbar.toolbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH

class LimitedActivity : AppCompatActivity(), OnEventClickListener<Event>,
    OnMonthChangeListener<Event>, OnEventLongClickListener<Event>, OnEmptyViewLongClickListener {

    private val weekView: WeekView<Event> by lazyView(R.id.weekView)
    private val database: EventsDatabase by lazy { FakeEventsDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_limited)

        toolbar.setupWithWeekView(weekView)

        weekView.onEventClickListener = this
        weekView.onMonthChangeListener = this
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

    override fun onMonthChange(
        startDate: Calendar,
        endDate: Calendar
    ) = database.getEventsInRange(startDate, endDate)

    override fun onEventClick(event: Event, eventRect: RectF) {
        showToast("Clicked ${event.title}")
    }

    override fun onEventLongClick(event: Event, eventRect: RectF) {
        showToast("Long-clicked ${event.title}")
        Toast.makeText(this, "Long pressed event: " + event.title, Toast.LENGTH_SHORT).show()
    }

    override fun onEmptyViewLongClick(time: Calendar) {
        val sdf = SimpleDateFormat.getDateTimeInstance()
        showToast("Empty view clicked at ${sdf.format(time.time)}")
    }
}

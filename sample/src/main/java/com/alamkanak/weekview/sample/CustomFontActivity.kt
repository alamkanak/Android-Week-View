package com.alamkanak.weekview.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

class CustomFontActivity : AppCompatActivity() {

    private val weekView: WeekView<Event> by lazyView(R.id.weekView)
    private val database: EventsDatabase by lazy { FakeEventsDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_font)

        toolbar.setupWithWeekView(weekView)

        weekView.setOnMonthChangeListener(this::onMonthChange)
        weekView.setOnEventClickListener { data, _ -> onEventClick(data) }
        weekView.setOnEventLongClickListener { data, _ -> onEventLongPress(data) }
        weekView.setOnEmptyViewLongClickListener(this::onEmptyViewLongPress)
    }

    private fun onMonthChange(
        startDate: Calendar,
        endDate: Calendar
    ) = database.getEventsInRange(startDate, endDate)

    private fun onEventClick(event: Event) {
        showToast("Clicked ${event.title}")
    }

    private fun onEventLongPress(event: Event) {
        showToast("Long-clicked ${event.title}")
    }

    private fun onEmptyViewLongPress(time: Calendar) {
        val sdf = SimpleDateFormat.getDateTimeInstance()
        showToast("Empty view clicked at ${sdf.format(time.time)}")
    }
}

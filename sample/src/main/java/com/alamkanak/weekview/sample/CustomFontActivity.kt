package com.alamkanak.weekview.sample

import android.graphics.RectF
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.sample.apiclient.Event
import com.alamkanak.weekview.sample.data.EventsDatabase
import com.alamkanak.weekview.sample.data.FakeEventsDatabase
import java.text.SimpleDateFormat
import java.util.Calendar

open class CustomFontActivity : AppCompatActivity() {

    private var weekViewType = TYPE_THREE_DAY_VIEW
    private lateinit var weekView: WeekView<Event>

    private val database: EventsDatabase by lazy {
        FakeEventsDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_font)

        weekView = findViewById(R.id.weekView)
        weekView.setOnEventClickListener(this::onEventClick)
        weekView.setOnMonthChangeListener(this::onMonthChange)
        weekView.setOnEventLongPressListener(this::onEventLongPress)
        weekView.setOnEmptyViewLongPressListener(this::onEmptyViewLongPress)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_today -> {
                weekView.goToToday()
                true
            }
            R.id.action_day_view -> {
                openDayView(item)
                true
            }
            R.id.action_three_day_view -> {
                openThreeDayView(item)
                true
            }
            R.id.action_week_view -> {
                openWeekView(item)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openDayView(item: MenuItem) {
        if (weekViewType == TYPE_DAY_VIEW) {
            return
        }

        item.isChecked = !item.isChecked
        weekViewType = TYPE_DAY_VIEW
        weekView.numberOfVisibleDays = 1
    }

    private fun openThreeDayView(item: MenuItem) {
        if (weekViewType == TYPE_THREE_DAY_VIEW) {
            return
        }

        item.isChecked = !item.isChecked
        weekViewType = TYPE_THREE_DAY_VIEW
        weekView.numberOfVisibleDays = 3
    }

    private fun openWeekView(item: MenuItem) {
        if (weekViewType == TYPE_WEEK_VIEW) {
            return
        }

        item.isChecked = !item.isChecked
        weekViewType = TYPE_WEEK_VIEW
        weekView.numberOfVisibleDays = 7
    }

    private fun onMonthChange(
        startDate: Calendar,
        endDate: Calendar
    ): List<WeekViewDisplayable<Event>> {
        return database.getEventsInRange(startDate, endDate)
    }

    private fun onEventClick(
        data: Event,
        eventRect: RectF
    ) {
        Toast.makeText(this, "Clicked " + data.title, Toast.LENGTH_SHORT).show()
    }

    private fun onEventLongPress(
        data: Event,
        eventRect: RectF
    ) {
        Toast.makeText(this, "Long pressed event: " + data.title, Toast.LENGTH_SHORT).show()
    }

    private fun onEmptyViewLongPress(
        time: Calendar
    ) {
        val sdf = SimpleDateFormat.getDateTimeInstance()
        Toast.makeText(this, "Empty view long pressed: " +
            sdf.format(time.time), Toast.LENGTH_SHORT).show()
    }

    private companion object {
        private const val TYPE_DAY_VIEW = 1
        private const val TYPE_THREE_DAY_VIEW = 2
        private const val TYPE_WEEK_VIEW = 3
    }
}

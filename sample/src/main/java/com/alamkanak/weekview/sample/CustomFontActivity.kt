package com.alamkanak.weekview.sample

import android.graphics.RectF
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.OnEmptyViewLongPressListener
import com.alamkanak.weekview.OnEventClickListener
import com.alamkanak.weekview.OnEventLongPressListener
import com.alamkanak.weekview.OnMonthChangeListener
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.sample.apiclient.Event
import com.alamkanak.weekview.sample.data.EventsDatabase
import com.alamkanak.weekview.sample.data.FakeEventsDatabase
import java.util.Calendar
import java.util.Locale

open class CustomFontActivity : AppCompatActivity(), OnEventClickListener<Event>,
    OnMonthChangeListener<Event>, OnEventLongPressListener<Event>, OnEmptyViewLongPressListener {

    private var weekViewType = TYPE_THREE_DAY_VIEW
    private lateinit var weekView: WeekView<Event>

    private val database: EventsDatabase by lazy {
        FakeEventsDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_font)

        weekView = findViewById(R.id.weekView)
        weekView.onEventClickListener = this
        weekView.onMonthChangeListener = this
        weekView.onEventLongPressListener = this
        weekView.onEmptyViewLongPressListener = this
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_today -> {
                weekView.goToToday()
                return true
            }
            R.id.action_day_view -> {
                openDayView(item)
                return true
            }
            R.id.action_three_day_view -> {
                openThreeDayView(item)
                return true
            }
            R.id.action_week_view -> {
                openWeekView(item)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
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

    private fun getEventTitle(time: Calendar): String {
        val hour = time.get(Calendar.HOUR_OF_DAY)
        val minute = time.get(Calendar.MINUTE)
        val month = time.get(Calendar.MONTH) + 1
        val dayOfMonth = time.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.getDefault(), "Event of %02d:%02d %s/%d", hour, minute, month, dayOfMonth)
    }

    override fun onMonthChange(startDate: Calendar,
                               endDate: Calendar): List<WeekViewDisplayable<Event>> {
        return database.getEventsInRange(startDate, endDate)
    }

    override fun onEventClick(data: Event, eventRect: RectF) {
        Toast.makeText(this, "Clicked " + data.title, Toast.LENGTH_SHORT).show()
    }

    override fun onEventLongPress(data: Event, eventRect: RectF) {
        Toast.makeText(this, "Long pressed event: " + data.title, Toast.LENGTH_SHORT).show()
    }

    override fun onEmptyViewLongPress(time: Calendar) {
        Toast.makeText(this, "Empty view long pressed: " + getEventTitle(time), Toast.LENGTH_SHORT).show()
    }

    private companion object {
        private const val TYPE_DAY_VIEW = 1
        private const val TYPE_THREE_DAY_VIEW = 2
        private const val TYPE_WEEK_VIEW = 3
    }

}

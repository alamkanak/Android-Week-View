package com.alamkanak.weekview.sample

import android.app.ProgressDialog
import android.graphics.RectF
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import com.alamkanak.weekview.sample.apiclient.ApiEvent
import com.alamkanak.weekview.sample.data.FakeEventsApi
import java.util.Calendar
import java.util.Locale

class AsyncActivity : AppCompatActivity() {

    private val events = arrayListOf<WeekViewDisplayable<ApiEvent>>()
    private var calledNetwork = false
    private var weekViewType = TYPE_THREE_DAY_VIEW

    private lateinit var weekView: WeekView<ApiEvent>

    private val apiService = FakeEventsApi(this)

    private val progressDialog: ProgressDialog by lazy {
        ProgressDialog(this).apply {
            setCancelable(false)
            setMessage("Loading events ...")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        weekView = findViewById(R.id.weekView)
        weekView.setOnEventClickListener(this::onEventClick)
        weekView.setOnEventLongPressListener(this::onEventLongPress)
        weekView.setOnMonthChangeListener(this::onMonthChange)
        weekView.setOnEmptyViewLongPressListener(this::onEmptyViewLongPress)

        progressDialog.show()
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

        item.isChecked = item.isChecked.not()
        weekViewType = TYPE_DAY_VIEW
        weekView.numberOfVisibleDays = 1
    }

    private fun openThreeDayView(item: MenuItem) {
        if (weekViewType == TYPE_THREE_DAY_VIEW) {
            return
        }

        item.isChecked = item.isChecked.not()
        weekViewType = TYPE_THREE_DAY_VIEW
        weekView.numberOfVisibleDays = 3
    }

    private fun openWeekView(item: MenuItem) {
        if (weekViewType == TYPE_WEEK_VIEW) {
            return
        }

        item.isChecked = item.isChecked.not()
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

    private fun onMonthChange(
            startDate: Calendar,
            endDate: Calendar
    ): List<WeekViewDisplayable<ApiEvent>> {
        val newYear = startDate.get(Calendar.YEAR)
        val newMonth = startDate.get(Calendar.MONTH)

        // Fetch events from network if it hasn't been done already
        if (!calledNetwork) {
            apiService.fetchEvents(this::onEventsFetched)
            calledNetwork = true
        }

        return events.filter { eventMatches(it.toWeekViewEvent(), newYear, newMonth) }
    }

    /**
     * Checks if an event falls into a specific year and month.
     * @param event The event to check for.
     * @param year The year.
     * @param month The month.
     * @return True if the event matches the year and month.
     */
    private fun eventMatches(event: WeekViewEvent<*>, year: Int, month: Int): Boolean {
        return event.startTime.get(Calendar.YEAR) == year
                && event.startTime.get(Calendar.MONTH) == month - 1
                || event.endTime.get(Calendar.YEAR) == year
                && event.endTime.get(Calendar.MONTH) == month - 1
    }

    private fun onEventsFetched(events: List<ApiEvent>) {
        this.events.clear()
        this.events.addAll(events)

        progressDialog.dismiss()
        weekView.notifyDataSetChanged()
    }

    private fun onEventClick(event: ApiEvent, eventRect: RectF) {
        Toast.makeText(this, "Removing ${event.name} ...", Toast.LENGTH_SHORT).show()
        events.remove(event)
        weekView.notifyDataSetChanged()
    }

    private fun onEventLongPress(event: ApiEvent, eventRect: RectF) {
        Toast.makeText(this, "Long pressed event: " + event.name, Toast.LENGTH_SHORT).show()
    }

    private fun onEmptyViewLongPress(time: Calendar) {
        Toast.makeText(this, "Empty view long pressed: " + getEventTitle(time), Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TYPE_DAY_VIEW = 1
        private const val TYPE_THREE_DAY_VIEW = 2
        private const val TYPE_WEEK_VIEW = 3
    }

}

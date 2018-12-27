package com.alamkanak.weekview.sample

import android.app.ProgressDialog
import android.graphics.RectF
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.util.TypedValue.*
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.alamkanak.weekview.*
import com.alamkanak.weekview.sample.apiclient.ApiEvent
import com.alamkanak.weekview.sample.apiclient.MyJsonService
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Response
import java.text.SimpleDateFormat
import java.util.*

class AsyncActivity : AppCompatActivity(), EventClickListener<ApiEvent>,
        MonthLoader.MonthChangeListener<ApiEvent>, EventLongPressListener<ApiEvent>,
        EmptyViewLongPressListener, Callback<List<ApiEvent>> {

    private val events = arrayListOf<WeekViewDisplayable<ApiEvent>>()
    private var calledNetwork = false
    private var weekViewType = TYPE_THREE_DAY_VIEW

    private lateinit var weekView: WeekView<ApiEvent>

    private val progressDialog: ProgressDialog by lazy {
        ProgressDialog(this).apply {
            setCancelable(true)
            setMessage("Loading events ...")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        progressDialog.show()
        setupDateTimeInterpreter()

        weekView = findViewById(R.id.weekView)
        weekView.setOnEventClickListener(this)
        weekView.setMonthChangeListener(this)
        weekView.setEventLongPressListener(this)
        weekView.emptyViewLongPressListener = this
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        setupDateTimeInterpreter()

        return when (item.itemId) {
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

        // Lets change some dimensions to best fit the view.
        weekView.columnGap = applyDimension(COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        weekView.setTimeColumnTextSize(applyDimension(COMPLEX_UNIT_SP, 12f, resources.displayMetrics).toInt())
        weekView.eventTextSize = applyDimension(COMPLEX_UNIT_SP, 12f, resources.displayMetrics).toInt()
    }

    private fun openThreeDayView(item: MenuItem) {
        if (weekViewType == TYPE_THREE_DAY_VIEW) {
            return
        }

        item.isChecked = item.isChecked.not()
        weekViewType = TYPE_THREE_DAY_VIEW
        weekView.numberOfVisibleDays = 3

        // Lets change some dimensions to best fit the view.
        weekView.columnGap = applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        weekView.setTimeColumnTextSize(applyDimension(COMPLEX_UNIT_SP, 12f, resources.displayMetrics).toInt())
        weekView.eventTextSize = applyDimension(COMPLEX_UNIT_SP, 12f, resources.displayMetrics).toInt()
    }

    private fun openWeekView(item: MenuItem) {
        if (weekViewType == TYPE_WEEK_VIEW) {
            return
        }

        item.isChecked = item.isChecked.not()
        weekViewType = TYPE_WEEK_VIEW
        weekView.numberOfVisibleDays = 7

        // Lets change some dimensions to best fit the view.
        weekView.columnGap = applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics).toInt()
        weekView.setTimeColumnTextSize(applyDimension(COMPLEX_UNIT_SP, 10f, resources.displayMetrics).toInt())
        weekView.eventTextSize = applyDimension(COMPLEX_UNIT_SP, 10f, resources.displayMetrics).toInt()
    }

    private fun setupDateTimeInterpreter() {
        weekView.dateTimeInterpreter = object : DateTimeInterpreter {

            private val weekdayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
            private val format = SimpleDateFormat(" M/d", Locale.getDefault())

            override fun interpretDate(date: Calendar): String {
                var weekday = weekdayNameFormat.format(date.time)
                if (weekView.numberOfVisibleDays == 7) {
                    weekday = weekday[0].toString()
                }
                return weekday.toUpperCase() + format.format(date.time)
            }

            override fun interpretTime(hour: Int): String {
                return when {
                    hour > 11 -> "${hour - 12} PM"
                    hour == 0 -> "12 AM"
                    else -> "$hour AM"
                }
            }
        }
    }

    private fun getEventTitle(time: Calendar): String {
        val hour = time.get(Calendar.HOUR_OF_DAY)
        val minute = time.get(Calendar.MINUTE)
        val month = time.get(Calendar.MONTH) + 1
        val dayOfMonth = time.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.getDefault(), "Event of %02d:%02d %s/%d", hour, minute, month, dayOfMonth)
    }

    override fun onMonthChange(startDate: Calendar, endDate: Calendar): List<WeekViewDisplayable<ApiEvent>> {
        val newYear = startDate.get(Calendar.YEAR)
        val newMonth = startDate.get(Calendar.MONTH)

        // Download events from network if it hasn't been done already. To understand how events are
        // downloaded using retrofit, visit http://square.github.io/retrofit
        if (!calledNetwork) {
            val retrofit = RestAdapter.Builder()
                    .setEndpoint("https://api.myjson.com/bins")
                    .build()

            val service = retrofit.create(MyJsonService::class.java)
            service.listEvents(this)
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

    override fun success(events: List<ApiEvent>, response: Response) {
        this.events.clear()
        this.events.addAll(events)

        progressDialog.dismiss()
        weekView.notifyDataSetChanged()
    }

    override fun failure(error: RetrofitError) {
        error.printStackTrace()
        progressDialog.dismiss()
        Toast.makeText(this, R.string.async_error, Toast.LENGTH_SHORT).show()
    }

    override fun onEventClick(event: ApiEvent, eventRect: RectF) {
        Toast.makeText(this, "Clicked " + event.name, Toast.LENGTH_SHORT).show()
    }

    override fun onEventLongPress(event: ApiEvent, eventRect: RectF) {
        Toast.makeText(this, "Long pressed event: " + event.name, Toast.LENGTH_SHORT).show()
    }

    override fun onEmptyViewLongPress(time: Calendar) {
        Toast.makeText(this, "Empty view long pressed: " + getEventTitle(time), Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TYPE_DAY_VIEW = 1
        private const val TYPE_THREE_DAY_VIEW = 2
        private const val TYPE_WEEK_VIEW = 3
    }

}

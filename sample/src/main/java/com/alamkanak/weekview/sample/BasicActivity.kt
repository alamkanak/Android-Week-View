package com.alamkanak.weekview.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.sample.data.EventsDatabase
import com.alamkanak.weekview.sample.data.model.Event
import com.alamkanak.weekview.sample.util.observe
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import com.alamkanak.weekview.sample.util.toCalendar
import com.alamkanak.weekview.threetenabp.setDateFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.android.synthetic.main.activity_basic.weekView
import kotlinx.android.synthetic.main.view_toolbar.toolbar
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

private class ViewModel(
    private val database: EventsDatabase
) {
    val events = MutableLiveData<List<WeekViewDisplayable<Event>>>()

    fun fetchEvents(startDate: LocalDate, endDate: LocalDate) {
        val values = database.getEventsInRange(startDate.toCalendar(), endDate.toCalendar())
        events.value = values
    }
}

class BasicActivity : AppCompatActivity() {

    private val weekdayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())

    private val viewModel: ViewModel by lazy {
        ViewModel(EventsDatabase(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic)

        toolbar.setupWithWeekView(weekView)

        WeekView.SimpleAdapter<Int>()

        val adapter = BasicActivityWeekViewAdapter(loadMoreHandler = viewModel::fetchEvents)
        weekView.adapter = adapter

        weekView.setDateFormatter { date: LocalDate ->
            val weekdayLabel = weekdayFormatter.format(date)
            val dateLabel = dateFormatter.format(date)
            weekdayLabel + "\n" + dateLabel
        }

        viewModel.events.observe(this) { events ->
            adapter.submit(events)
        }
    }
}

private class BasicActivityWeekViewAdapter(
    private val loadMoreHandler: (startDate: LocalDate, endDate: LocalDate) -> Unit
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
        loadMoreHandler(startDate.toLocalDate(), endDate.toLocalDate())
    }
}

private fun Calendar.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate()
}

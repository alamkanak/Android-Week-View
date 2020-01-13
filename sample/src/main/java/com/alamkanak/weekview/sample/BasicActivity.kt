package com.alamkanak.weekview.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.sample.data.EventsDatabase
import com.alamkanak.weekview.sample.data.model.Event
import com.alamkanak.weekview.sample.util.lazyView
import com.alamkanak.weekview.sample.util.observe
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import com.alamkanak.weekview.sample.util.toCalendar
import com.alamkanak.weekview.threetenabp.setOnEmptyViewClickListener
import com.alamkanak.weekview.threetenabp.setOnLoadMoreListener
import kotlinx.android.synthetic.main.view_toolbar.toolbar
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

private class ViewModel(
    private val database: EventsDatabase
) {
    val events = MutableLiveData<List<WeekViewDisplayable<Event>>>()

    fun fetchEvents(startDate: LocalDate, endDate: LocalDate) {
        events.value = database.getEventsInRange(startDate.toCalendar(), endDate.toCalendar())
    }
}

class BasicActivity : AppCompatActivity() {

    private val weekView: WeekView<Event> by lazyView(R.id.weekView)

    private val viewModel: ViewModel by lazy {
        ViewModel(EventsDatabase(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic)

        toolbar.setupWithWeekView(weekView)

        viewModel.events.observe(this) { events ->
            weekView.submit(events)
        }

        weekView.setOnLoadMoreListener { startDate: LocalDate, endDate: LocalDate ->
            viewModel.fetchEvents(startDate, endDate)
        }

        weekView.setOnEventClickListener { event, _ ->
            showToast("Clicked ${event.title}")
        }

        weekView.setOnEventLongClickListener { event, _ ->
            showToast("Long-clicked ${event.title}")
        }

        weekView.setOnEmptyViewClickListener { dateTime: LocalDateTime ->
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            showToast("Empty view clicked at ${formatter.format(dateTime)}")
        }
    }
}

package com.alamkanak.weekview.sample

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.sample.data.model.Event
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlinx.android.synthetic.main.activity_static.dateRangeTextView
import kotlinx.android.synthetic.main.activity_static.nextWeekButton
import kotlinx.android.synthetic.main.activity_static.previousWeekButton
import kotlinx.android.synthetic.main.activity_static.weekView
import kotlinx.android.synthetic.main.view_toolbar.toolbar

class StaticActivity : AppCompatActivity() {

    private val eventsFetcher: EventsFetcher by lazy { EventsFetcher(this) }

    private val dateFormatter = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM)

    private val adapter: StaticActivityWeekViewAdapter by lazy {
        StaticActivityWeekViewAdapter(
            context = this,
            loadMoreHandler = this::onLoadMore,
            rangeChangeHandler = this::onRangeChanged
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_static)

        toolbar.setupWithWeekView(weekView)
        weekView.adapter = adapter

        previousWeekButton.setOnClickListener {
            val cal = weekView.firstVisibleDate
            cal.add(Calendar.DATE, -7)
            weekView.goToDate(cal)
        }

        nextWeekButton.setOnClickListener {
            val cal = weekView.firstVisibleDate
            cal.add(Calendar.DATE, 7)
            weekView.goToDate(cal)
        }
    }

    private fun onLoadMore(startDate: Calendar, endDate: Calendar) {
        eventsFetcher.fetch(startDate, endDate, adapter::submit)
    }

    private fun onRangeChanged(startDate: Calendar, endDate: Calendar) {
        val formattedFirstDay = dateFormatter.format(startDate.time)
        val formattedLastDay = dateFormatter.format(endDate.time)
        dateRangeTextView.text = getString(R.string.date_infos, formattedFirstDay, formattedLastDay)
    }
}

private class StaticActivityWeekViewAdapter(
    context: Context,
    private val rangeChangeHandler: (startDate: Calendar, endDate: Calendar) -> Unit,
    private val loadMoreHandler: (startDate: Calendar, endDate: Calendar) -> Unit
) : WeekView.PagingAdapter<Event>(context) {

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

    override fun onRangeChanged(firstVisibleDate: Calendar, lastVisibleDate: Calendar) {
        rangeChangeHandler(firstVisibleDate, lastVisibleDate)
    }
}

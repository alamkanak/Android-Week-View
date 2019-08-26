package com.alamkanak.weekview.sample

import android.app.ProgressDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.sample.apiclient.ApiEvent
import com.alamkanak.weekview.sample.data.EventsApi
import com.alamkanak.weekview.sample.data.FakeEventsApi
import java.text.SimpleDateFormat

private data class AsyncViewState(
    val events: List<ApiEvent> = emptyList(),
    val isLoading: Boolean = false
)

private class AsyncViewModel(
    private val eventsApi: EventsApi
) {
    val viewState = MutableLiveData<AsyncViewState>()

    init {
        viewState.value = AsyncViewState(isLoading = true)
        fetchEvents()
    }

    fun fetchEvents() = eventsApi.fetchEvents {
        viewState.value = AsyncViewState(it)
    }

    fun remove(event: ApiEvent) {
        val allEvents = viewState.value?.events ?: return
        viewState.value = AsyncViewState(events = allEvents.minus(event))
    }
}

class AsyncActivity : AppCompatActivity() {

    private var weekViewType = TYPE_THREE_DAY_VIEW

    private val weekView: WeekView<ApiEvent> by lazy {
        findViewById<WeekView<ApiEvent>>(R.id.weekView)
    }

    private val viewModel: AsyncViewModel by lazy {
        AsyncViewModel(FakeEventsApi(this))
    }

    @Suppress("DEPRECATION")
    private val progressDialog: ProgressDialog by lazy {
        ProgressDialog(this).apply {
            setCancelable(false)
            setMessage("Loading events ...")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        viewModel.viewState.observe(this, Observer { viewState ->
            if (viewState.isLoading) {
                progressDialog.show()
            } else {
                progressDialog.dismiss()
            }

            weekView.submit(viewState.events)
        })

        weekView.setOnEventClickListener { data, rect ->
            viewModel.remove(data)
            Toast.makeText(this, "Removed ${data.name}", Toast.LENGTH_SHORT).show()
        }

        weekView.setOnEventLongClickListener { data, rect ->
            Toast.makeText(this, "Long pressed event: ${data.name}", Toast.LENGTH_SHORT).show()
        }

        weekView.setOnEmptyViewLongClickListener { time ->
            val sdf = SimpleDateFormat.getDateTimeInstance()
            Toast.makeText(
                this,
                "Empty view long pressed: ${sdf.format(time.time)}",
                Toast.LENGTH_SHORT
            ).show()
        }
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

    companion object {
        private const val TYPE_DAY_VIEW = 1
        private const val TYPE_THREE_DAY_VIEW = 2
        private const val TYPE_WEEK_VIEW = 3
    }
}

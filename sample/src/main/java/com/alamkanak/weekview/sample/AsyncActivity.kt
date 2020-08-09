package com.alamkanak.weekview.sample

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.sample.data.EventsApi
import com.alamkanak.weekview.sample.data.model.ApiEvent
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlinx.android.synthetic.main.activity_basic.weekView
import kotlinx.android.synthetic.main.view_toolbar.toolbar

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

    private val viewModel: AsyncViewModel by lazy {
        AsyncViewModel(EventsApi(this))
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
        setContentView(R.layout.activity_basic)

        toolbar.setupWithWeekView(weekView)

        val adapter = AsyncActivityWeekViewAdapter(
            context = this,
            eventClickHandler = viewModel::remove
        )

        viewModel.viewState.observe(this, Observer { viewState ->
            if (viewState.isLoading) {
                progressDialog.show()
            } else {
                progressDialog.dismiss()
            }
            adapter.submit(viewState.events)
        })
    }
}

private class AsyncActivityWeekViewAdapter(
    context: Context,
    private val eventClickHandler: (ApiEvent) -> Unit
) : WeekView.SimpleAdapter<ApiEvent>(context) {

    private val formatter = SimpleDateFormat.getDateTimeInstance()

    override fun onEventClick(data: ApiEvent) {
        eventClickHandler(data)
        context.showToast("Removed ${data.title}")
    }

    override fun onEmptyViewClick(time: Calendar) {
        context.showToast("Empty view clicked at ${formatter.format(time.time)}")
    }

    override fun onEventLongClick(data: ApiEvent) {
        context.showToast("Long-clicked ${data.title}")
    }

    override fun onEmptyViewLongClick(time: Calendar) {
        context.showToast("Empty view long-clicked at ${formatter.format(time.time)}")
    }
}

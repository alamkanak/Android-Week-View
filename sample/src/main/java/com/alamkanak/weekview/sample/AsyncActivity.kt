package com.alamkanak.weekview.sample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.alamkanak.weekview.sample.data.EventsApi
import com.alamkanak.weekview.sample.data.model.ApiEvent
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import com.alamkanak.weekview.threetenabp.WeekViewSimpleAdapterThreeTenAbp
import kotlinx.android.synthetic.main.activity_basic.blockingProgressIndicator
import kotlinx.android.synthetic.main.activity_basic.weekView
import kotlinx.android.synthetic.main.view_toolbar.toolbar
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle.MEDIUM
import org.threeten.bp.format.FormatStyle.SHORT

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic)

        toolbar.setupWithWeekView(weekView)

        val adapter = AsyncActivityWeekViewAdapter(eventClickHandler = viewModel::remove)
        weekView.adapter = adapter

        viewModel.viewState.observe(this) { viewState ->
            blockingProgressIndicator.isVisible = viewState.isLoading
            adapter.submit(viewState.events)
        }
    }
}

private var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

private class AsyncActivityWeekViewAdapter(
    private val eventClickHandler: (ApiEvent) -> Unit
) : WeekViewSimpleAdapterThreeTenAbp<ApiEvent>() {

    private val formatter = DateTimeFormatter.ofLocalizedDateTime(MEDIUM, SHORT)

    override fun onEventClick(data: ApiEvent) {
        eventClickHandler(data)
        context.showToast("Removed ${data.title}")
    }

    override fun onEmptyViewClick(time: LocalDateTime) {
        context.showToast("Empty view clicked at ${formatter.format(time)}")
    }

    override fun onEventLongClick(data: ApiEvent) {
        context.showToast("Long-clicked ${data.title}")
    }

    override fun onEmptyViewLongClick(time: LocalDateTime) {
        context.showToast("Empty view long-clicked at ${formatter.format(time)}")
    }
}

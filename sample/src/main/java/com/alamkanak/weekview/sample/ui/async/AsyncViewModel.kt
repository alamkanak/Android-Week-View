package com.alamkanak.weekview.sample.ui.async

import androidx.lifecycle.MutableLiveData
import com.alamkanak.weekview.sample.data.EventsApi
import com.alamkanak.weekview.sample.data.model.ApiEvent

internal data class AsyncViewState(
    val events: List<ApiEvent> = emptyList(),
    val isLoading: Boolean = false
)

internal class AsyncViewModel(
    private val eventsApi: EventsApi
) {

    val viewState = MutableLiveData<AsyncViewState>()

    init {
        fetchEvents()
    }

    private fun fetchEvents() {
        viewState.value = AsyncViewState(isLoading = true)

        eventsApi.fetchEvents { events ->
            viewState.value = AsyncViewState(events = events, isLoading = false)
        }
    }

    fun remove(event: ApiEvent) {
        val allEvents = viewState.value?.events ?: return
        viewState.value = AsyncViewState(events = allEvents.minus(event))
    }
}

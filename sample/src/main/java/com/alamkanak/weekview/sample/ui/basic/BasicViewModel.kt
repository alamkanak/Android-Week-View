package com.alamkanak.weekview.sample.ui.basic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alamkanak.weekview.sample.data.EventsDatabase
import com.alamkanak.weekview.sample.data.model.CalendarEntity
import java.io.IOException
import kotlin.random.Random
import org.threeten.bp.LocalDate

internal data class LoadParams(val startDate: LocalDate, val endDate: LocalDate)

internal class RangeLoadingException(val loadParams: LoadParams) : IOException()

internal data class BasicViewState(
    val entities: List<CalendarEntity> = emptyList(),
    val error: RangeLoadingException? = null
)

internal class BasicViewModel(
    private val database: EventsDatabase
) : ViewModel() {

    private val _viewState = MutableLiveData<BasicViewState>()
    val viewState: LiveData<BasicViewState> = _viewState

    fun retry(loadParams: LoadParams) {
        fetchEvents(loadParams)
    }

    fun fetchEvents(loadParams: LoadParams) {
        val shouldFail = Random.nextDouble()
        val existingEvents = _viewState.value?.entities.orEmpty()

        if (shouldFail < 0.1) {
            _viewState.value = BasicViewState(
                entities = existingEvents,
                error = RangeLoadingException(loadParams = loadParams)
            )
        } else {
            val newEvents = database.getEntitiesInRange(loadParams.startDate, loadParams.endDate)
            _viewState.value = BasicViewState(
                entities = existingEvents + newEvents,
                error = null
            )
        }
    }
}

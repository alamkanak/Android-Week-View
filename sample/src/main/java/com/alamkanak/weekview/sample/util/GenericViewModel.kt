package com.alamkanak.weekview.sample.util

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import com.alamkanak.weekview.sample.data.EventsRepository
import com.alamkanak.weekview.sample.data.model.CalendarEntity
import java.time.YearMonth

data class GenericViewState(
    val entities: List<CalendarEntity> = emptyList()
)

class GenericViewModel(
    private val eventsRepository: EventsRepository
) : ViewModel() {

    private val _viewState = MutableLiveData<GenericViewState>()
    val viewState: LiveData<GenericViewState> = _viewState

    fun fetchEvents(yearMonths: List<YearMonth>) {
        eventsRepository.fetch(yearMonths = yearMonths) { entities ->
            val existingEntities = _viewState.value?.entities.orEmpty()
            _viewState.value = GenericViewState(entities = existingEntities + entities)
        }
    }

    fun remove(id: Long) {
        val entities = requireNotNull(viewState.value).entities.toMutableList()
        entities.removeIf { (it as? CalendarEntity.Event)?.id == id }
        _viewState.value = GenericViewState(entities.toList())
    }

    class Factory(private val eventsRepository: EventsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GenericViewModel::class.java)) {
                return GenericViewModel(eventsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class ${modelClass.simpleName}")
        }
    }
}

fun ComponentActivity.genericViewModel(): Lazy<GenericViewModel> {
    val factoryPromise = {
        GenericViewModel.Factory(eventsRepository = EventsRepository(context = this))
    }
    return ViewModelLazy(GenericViewModel::class, { viewModelStore }, factoryPromise)
}

fun Fragment.genericViewModel(): Lazy<GenericViewModel> {
    val factoryPromise = {
        GenericViewModel.Factory(eventsRepository = EventsRepository(context = requireContext()))
    }
    return ViewModelLazy(GenericViewModel::class, { viewModelStore }, factoryPromise)
}

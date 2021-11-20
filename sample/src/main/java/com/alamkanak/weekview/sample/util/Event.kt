package com.alamkanak.weekview.sample.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

data class Event<T>(private val payload: T) {

    private var handled: Boolean = false

    fun handle(block: (T) -> Unit) {
        handled = true
        block(payload)
    }
}

fun <T> MutableLiveData<Event<T>>.postEvent(element: T) {
    postValue(Event(element))
}

fun <T> LiveData<Event<T>>.subscribeToEvents(lifecycleOwner: LifecycleOwner, observe: (T) -> Unit) {
    observe(lifecycleOwner) { event ->
        event.handle(observe)
    }
}

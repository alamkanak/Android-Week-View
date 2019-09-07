package com.alamkanak.weekview

import java.util.concurrent.ConcurrentHashMap

internal fun <T> ConcurrentHashMap<Long, MutableList<EventChip<T>>>.addOrReplace(
    key: Long,
    eventChip: EventChip<T>
) {
    val results = getOrElse(key) { mutableListOf() }
    val indexOfExisting = results.indexOfFirst { it.event == eventChip.event }
    if (indexOfExisting != -1) {
        // If an event with the same ID already exists, replace it. The new event will likely be
        // more up-to-date.
        results.replace(indexOfExisting, eventChip)
    } else {
        results.add(eventChip)
    }

    this[key] = results
}

internal fun <T> MutableList<T>.replace(index: Int, element: T) {
    removeAt(index)
    add(index, element)
}

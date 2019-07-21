package com.alamkanak.weekview

import android.view.MotionEvent
import androidx.collection.ArrayMap
import java.util.Calendar

internal class EventChipCache<T> {

    private val allEventChips: List<EventChip<T>>
        get() = normalEventChipsByDate.values.flatten() + allDayEventChipsByDate.values.flatten()

    private val normalEventChipsByDate = ArrayMap<Calendar, MutableList<EventChip<T>>>()
    private val allDayEventChipsByDate = ArrayMap<Calendar, MutableList<EventChip<T>>>()

    fun findHits(e: MotionEvent) = allEventChips.filter { it.isHit(e) }

    fun groupedByDate(): Map<Calendar, List<EventChip<T>>> {
        return allEventChips.groupBy { it.event.startTime.atStartOfDay }
    }

    fun normalEventChipsByDate(date: Calendar): List<EventChip<T>> {
        return normalEventChipsByDate[date.atStartOfDay].orEmpty()
    }

    fun allDayEventChipsByDate(date: Calendar): List<EventChip<T>> {
        return allDayEventChipsByDate[date.atStartOfDay].orEmpty()
    }

    fun put(newChips: List<EventChip<T>>) {
        val (allDay, normal) = newChips.partition { it.event.isAllDay }

        normal.forEach {
            val key = it.event.startTime.atStartOfDay
            normalEventChipsByDate.add(key, it)
        }

        allDay.forEach {
            val key = it.event.startTime.atStartOfDay
            allDayEventChipsByDate.add(key, it)
        }
    }

    operator fun plusAssign(newChips: List<EventChip<T>>) = put(newChips)

    fun clearCache() {
        allEventChips.filter { it.originalEvent.isNotAllDay }.forEach(EventChip<T>::clearCache)
    }

    fun clear() {
        allDayEventChipsByDate.clear()
        normalEventChipsByDate.clear()
    }

    private fun ArrayMap<Calendar, MutableList<EventChip<T>>>.add(
        key: Calendar,
        eventChip: EventChip<T>
    ) {
        val results = getOrElse(key) { mutableListOf() }
        val indexOfExisting = results.indexOfFirst { it.event.id == eventChip.event.id }
        if (indexOfExisting != -1) {
            // If an event with the same ID already exists, replace it. The new event will likely be
            // more up-to-date.
            results.replace(indexOfExisting, eventChip)
        } else {
            results.add(eventChip)
        }
        this[key] = results
    }

    private fun <T> MutableList<T>.replace(index: Int, element: T) {
        removeAt(index)
        add(index, element)
    }
}

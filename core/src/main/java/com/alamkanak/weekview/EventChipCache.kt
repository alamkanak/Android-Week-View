package com.alamkanak.weekview

import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

internal class EventChipCache<T> {

    val allEventChips: List<EventChip<T>>
        get() = normalEventChipsByDate.values.flatten() + allDayEventChipsByDate.values.flatten()

    private val normalEventChipsByDate = ConcurrentHashMap<Long, MutableList<EventChip<T>>>()
    private val allDayEventChipsByDate = ConcurrentHashMap<Long, MutableList<EventChip<T>>>()

    fun groupedByDate(): Map<Calendar, List<EventChip<T>>> {
        return allEventChips.groupBy { it.event.startTime.atStartOfDay }
    }

    fun allEventChipsInDateRange(
        dateRange: List<Calendar>
    ): List<EventChip<T>> {
        val results = mutableListOf<EventChip<T>>()
        for (date in dateRange) {
            results += allDayEventChipsByDate[date.atStartOfDay.timeInMillis].orEmpty()
            results += normalEventChipsByDate[date.atStartOfDay.timeInMillis].orEmpty()
        }
        return results
    }

    fun normalEventChipsByDate(
        date: Calendar
    ): List<EventChip<T>> = normalEventChipsByDate[date.atStartOfDay.timeInMillis].orEmpty()

    fun allDayEventChipsByDate(
        date: Calendar
    ): List<EventChip<T>> = allDayEventChipsByDate[date.atStartOfDay.timeInMillis].orEmpty()

    fun allDayEventChipsInDateRange(
        dateRange: List<Calendar>
    ): List<EventChip<T>> {
        val results = mutableListOf<EventChip<T>>()
        for (date in dateRange) {
            results += allDayEventChipsByDate[date.atStartOfDay.timeInMillis].orEmpty()
        }
        return results
    }

    private fun put(newChips: List<EventChip<T>>) {
        for (eventChip in newChips) {
            val key = eventChip.event.startTime.atStartOfDay.timeInMillis
            if (eventChip.event.isAllDay) {
                allDayEventChipsByDate.addOrReplace(key, eventChip)
            } else {
                normalEventChipsByDate.addOrReplace(key, eventChip)
            }
        }
    }

    operator fun plusAssign(newChips: List<EventChip<T>>) = put(newChips)

    fun clearSingleEventsCache() {
        allEventChips.filter { it.originalEvent.isNotAllDay }.forEach(EventChip<T>::clearCache)
    }

    fun clear() {
        allDayEventChipsByDate.clear()
        normalEventChipsByDate.clear()
    }

    private fun <T> ConcurrentHashMap<Long, MutableList<EventChip<T>>>.addOrReplace(
        key: Long,
        eventChip: EventChip<T>
    ) {
        val results = getOrElse(key) { mutableListOf() }
        val indexOfExisting = results.indexOfFirst { it.event.id == eventChip.event.id }
        if (indexOfExisting != -1) {
            // If an event with the same ID already exists, replace it. The new event will likely be
            // more up-to-date.
            results.removeAt(indexOfExisting)
            results.add(indexOfExisting, eventChip)
        } else {
            results.add(eventChip)
        }

        this[key] = results
    }
}

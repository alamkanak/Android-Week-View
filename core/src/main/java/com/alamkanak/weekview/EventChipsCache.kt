package com.alamkanak.weekview

import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

internal class EventChipsCache {

    val allEventChips: List<EventChip>
        get() = normalEventChipsByDate.values.flatten() + allDayEventChipsByDate.values.flatten()

    private val normalEventChipsByDate = ConcurrentHashMap<Long, CopyOnWriteArrayList<EventChip>>()
    private val allDayEventChipsByDate = ConcurrentHashMap<Long, CopyOnWriteArrayList<EventChip>>()

    fun allEventChipsInDateRange(
        dateRange: List<Calendar>
    ): List<EventChip> {
        val results = mutableListOf<EventChip>()
        for (date in dateRange) {
            results += allDayEventChipsByDate[date.atStartOfDay.timeInMillis].orEmpty()
            results += normalEventChipsByDate[date.atStartOfDay.timeInMillis].orEmpty()
        }
        return results
    }

    fun normalEventChipsByDate(
        date: Calendar
    ): List<EventChip> = normalEventChipsByDate[date.atStartOfDay.timeInMillis].orEmpty()

    fun allDayEventChipsByDate(
        date: Calendar
    ): List<EventChip> = allDayEventChipsByDate[date.atStartOfDay.timeInMillis].orEmpty()

    fun allDayEventChipsInDateRange(
        dateRange: List<Calendar>
    ): List<EventChip> {
        val results = mutableListOf<EventChip>()
        for (date in dateRange) {
            results += allDayEventChipsByDate[date.atStartOfDay.timeInMillis].orEmpty()
        }
        return results
    }

    private fun put(newChips: List<EventChip>) {
        for (eventChip in newChips) {
            val key = eventChip.event.startTime.atStartOfDay.timeInMillis
            if (eventChip.event.isAllDay) {
                allDayEventChipsByDate.addOrReplace(key, eventChip)
            } else {
                normalEventChipsByDate.addOrReplace(key, eventChip)
            }
        }
    }

    operator fun plusAssign(newChips: List<EventChip>) = put(newChips)

    fun clearSingleEventsCache() {
        allEventChips.filter { it.originalEvent.isNotAllDay }.forEach(EventChip::clearCache)
    }

    fun clear() {
        allDayEventChipsByDate.clear()
        normalEventChipsByDate.clear()
    }

    private fun ConcurrentHashMap<Long, CopyOnWriteArrayList<EventChip>>.addOrReplace(
        key: Long,
        eventChip: EventChip
    ) {
        val results = getOrElse(key) { CopyOnWriteArrayList() }
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

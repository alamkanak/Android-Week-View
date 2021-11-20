package com.alamkanak.weekview

import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

internal typealias EventChipsCacheProvider = () -> EventChipsCache?

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

    fun replaceAll(eventChips: List<EventChip>) {
        clear()
        addAll(eventChips)
    }

    fun addAll(eventChips: List<EventChip>) {
        for (eventChip in eventChips) {
            val isExistingEvent = allEventChips.any { it.eventId == eventChip.eventId }
            if (isExistingEvent) {
                remove(eventId = eventChip.eventId)
            }
        }

        for (eventChip in eventChips) {
            val key = eventChip.startTime.atStartOfDay.timeInMillis

            if (eventChip.event.isAllDay) {
                allDayEventChipsByDate.addOrReplace(key, eventChip)
            } else {
                normalEventChipsByDate.addOrReplace(key, eventChip)
            }
        }
    }

    fun findHitEvent(x: Float, y: Float): EventChip? {
        val candidates = allEventChips.filter { it.isHit(x, y) }
        return when {
            candidates.isEmpty() -> null
            // Two events hit. This is most likely because an all-day event was clicked, but a
            // single event is rendered underneath it. We return the all-day event.
            candidates.size == 2 -> candidates.first { it.event.isAllDay }
            else -> candidates.first()
        }
    }

    fun remove(eventId: Long) {
        val eventChip = allEventChips.firstOrNull { it.eventId == eventId } ?: return
        remove(eventChip)
    }

    fun removeAll(events: List<ResolvedWeekViewEntity>) {
        val eventIds = events.map { it.id }
        val eventChips = allEventChips.filter { it.event.id in eventIds }
        eventChips.forEach(this::remove)
    }

    private fun remove(eventChip: EventChip) {
        val key = eventChip.startTime.atStartOfDay.timeInMillis
        val eventId = eventChip.eventId

        if (eventChip.event.isAllDay) {
            allDayEventChipsByDate[key]?.removeAll { it.event.id == eventId }
        } else {
            normalEventChipsByDate[key]?.removeAll { it.event.id == eventId }
        }
    }

    fun clearSingleEventsCache() {
        allEventChips.filter { it.event.isNotAllDay }.forEach(EventChip::setEmpty)
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

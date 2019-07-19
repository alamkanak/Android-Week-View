package com.alamkanak.weekview

import android.view.MotionEvent
import androidx.collection.ArrayMap
import java.util.Calendar

internal class EventCache<T>(
    private val eventSplitter: WeekViewEventSplitter<T>
) {

    private val allEventChips: List<EventChip<T>>
        get() = normalEventChipsByDate.values.flatten() + allDayEventChipsByDate.values.flatten()

    private val normalEventChipsByDate = ArrayMap<Calendar, MutableList<EventChip<T>>>()
    private val allDayEventChipsByDate = ArrayMap<Calendar, MutableList<EventChip<T>>>()

    var previousPeriodEvents: List<WeekViewEvent<T>>? = null
    var currentPeriodEvents: List<WeekViewEvent<T>>? = null
    var nextPeriodEvents: List<WeekViewEvent<T>>? = null

    var fetchedPeriods: FetchPeriods? = null

    val hasEvents: Boolean
        get() = previousPeriodEvents != null && currentPeriodEvents != null && nextPeriodEvents != null

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

    fun covers(fetchPeriods: FetchPeriods): Boolean {
        return fetchedPeriods?.let {
            it.previous == fetchPeriods.previous
                && it.current == fetchPeriods.current
                && it.next == fetchPeriods.next
        } ?: false
    }

    private fun getEventChipsInRange(
        eventChips: List<EventChip<T>>,
        dateRange: List<Calendar>
    ): List<WeekViewEvent<T>> {
        val results = mutableListOf<WeekViewEvent<T>>()
        for (date in dateRange) {
            results += eventChips
                .filter { it.event.isSameDay(date) }
                .map { it.event }
        }
        return results
    }

    fun getAllDayEventsInRange(dateRange: List<Calendar>): List<WeekViewEvent<T>> {
        return getEventChipsInRange(allEventChips, dateRange).filter { it.isAllDay }
    }

    fun clearEventChipsCache() {
        allEventChips.filter { it.originalEvent.isNotAllDay }.forEach(EventChip<T>::clearCache)
    }

    fun clear() {
        allDayEventChipsByDate.clear()
        normalEventChipsByDate.clear()

        previousPeriodEvents = null
        currentPeriodEvents = null
        nextPeriodEvents = null

        fetchedPeriods = null
    }

    fun update(
        previousPeriodEvents: List<WeekViewEvent<T>>,
        currentPeriodEvents: List<WeekViewEvent<T>>,
        nextPeriodEvents: List<WeekViewEvent<T>>,
        fetchedPeriods: FetchPeriods
    ) {
        sortAndCacheEvents(previousPeriodEvents)
        sortAndCacheEvents(currentPeriodEvents)
        sortAndCacheEvents(nextPeriodEvents)

        this.previousPeriodEvents = previousPeriodEvents
        this.currentPeriodEvents = currentPeriodEvents
        this.nextPeriodEvents = nextPeriodEvents
        this.fetchedPeriods = fetchedPeriods
    }

    /**
     * Sort and cache events.
     *
     * @param events The events to be sorted and cached.
     */
    private fun sortAndCacheEvents(events: List<WeekViewEvent<T>>) {
        events.sorted().forEach { cacheEvent(it) }
    }

    /**
     * Cache the event for smooth scrolling functionality.
     *
     * @param event The event to cache.
     */
    private fun cacheEvent(event: WeekViewEvent<T>) {
        if (event.startTime >= event.endTime) {
            return
        }

        val newChips = eventSplitter.split(event).map { EventChip(it, event, null) }
        put(newChips)
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

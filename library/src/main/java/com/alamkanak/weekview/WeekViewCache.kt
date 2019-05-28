package com.alamkanak.weekview

import android.support.v4.util.ArrayMap
import org.threeten.bp.LocalDate

internal class WeekViewCache<T>(
        private val eventSplitter: WeekViewEventSplitter<T>
) {

    var allEventChips = mutableListOf<EventChip<T>>()
    var allDayEventChips = mutableListOf<EventChip<T>>()

    var previousPeriodEvents: List<WeekViewEvent<T>>? = null
    var currentPeriodEvents: List<WeekViewEvent<T>>? = null
    var nextPeriodEvents: List<WeekViewEvent<T>>? = null

    var fetchedPeriods: FetchPeriods? = null

    val hasEvents: Boolean
        get() = previousPeriodEvents != null && currentPeriodEvents != null && nextPeriodEvents != null

    private val normalEventChipsByDate = ArrayMap<LocalDate, MutableList<EventChip<T>>>()
    private val allDayEventChipsByDate = ArrayMap<LocalDate, MutableList<EventChip<T>>>()

    fun normalEventChipsByDate(date: LocalDate): List<EventChip<T>> {
        return normalEventChipsByDate[date].orEmpty()
    }

    fun allDayEventChipsByDate(date: LocalDate): List<EventChip<T>> {
        return allDayEventChipsByDate[date].orEmpty()
    }

    fun put(newChips: List<EventChip<T>>) {
        allEventChips.clear()
        allEventChips.addAll(newChips)

        val (allDay, normal) = newChips.partition { it.event.isAllDay }

        allDayEventChips.clear()
        allDayEventChips.addAll(allDay)

        normal.forEach {
            val key = it.event.startDateTime.toLocalDate()
            normalEventChipsByDate.add(key, it)
        }

        allDay.forEach {
            val key = it.event.startDateTime.toLocalDate()
            allDayEventChipsByDate.add(key, it)
        }
    }

    fun covers(fetchPeriods: FetchPeriods): Boolean {
        return this.fetchedPeriods?.let {
            it.previous == fetchPeriods.previous && it.current == fetchPeriods.current
                    && it.next == fetchPeriods.next
        } ?: false
    }

    private fun getEventChipsInRange(
            eventChips: List<EventChip<T>>,
            dateRange: List<LocalDate>
    ): List<WeekViewEvent<T>> {
        val results = mutableListOf<WeekViewEvent<T>>()
        for (date in dateRange) {
            results += eventChips
                    .filter { it.event.isSameDay(date) }
                    .map { it.event }
        }
        return results
    }

    fun getAllDayEventsInRange(dateRange: List<LocalDate>): List<WeekViewEvent<T>> {
        return getEventChipsInRange(allDayEventChips, dateRange).filter { it.isAllDay }
    }

    fun clearEventChipsCache() {
        allEventChips.forEach { it.rect = null }
    }

    fun clear() {
        allEventChips.clear()
        previousPeriodEvents = null
        currentPeriodEvents = null
        nextPeriodEvents = null
        fetchedPeriods = null
    }

    /**
     * Sort and cache events.
     *
     * @param events The events to be sorted and cached.
     */
    fun sortAndCacheEvents(events: List<WeekViewEvent<T>>) {
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
        allEventChips.addAll(newChips)
    }

    private fun ArrayMap<LocalDate, MutableList<EventChip<T>>>.add(
            key: LocalDate,
            eventChip: EventChip<T>
    ) {
        val results = getOrElse(key) { mutableListOf() }
        results.add(eventChip)
        this[key] = results
    }

}

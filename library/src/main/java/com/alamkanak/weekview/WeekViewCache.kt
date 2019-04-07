package com.alamkanak.weekview

import org.threeten.bp.LocalDate

internal class WeekViewCache<T>(
        private val eventSplitter: WeekViewEventSplitter<T>
) {

    var allEventChips = mutableListOf<EventChip<T>>()
    var normalEventChips = mutableListOf<EventChip<T>>()
    var allDayEventChips = mutableListOf<EventChip<T>>()

    var previousPeriodEvents: List<WeekViewEvent<T>>? = null
    var currentPeriodEvents: List<WeekViewEvent<T>>? = null
    var nextPeriodEvents: List<WeekViewEvent<T>>? = null

    var fetchedPeriods: FetchPeriods? = null

    val hasEvents: Boolean
        get() = previousPeriodEvents != null && currentPeriodEvents != null && nextPeriodEvents != null

    fun put(newChips: List<EventChip<T>>) {
        allEventChips.clear()
        allEventChips.addAll(newChips)

        val (allDay, normal) = newChips.partition { it.event.isAllDay }

        normalEventChips.clear()
        normalEventChips.addAll(normal)

        allDayEventChips.clear()
        allDayEventChips.addAll(allDay)
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

}

package com.alamkanak.weekview

import java.util.*

internal class WeekViewCache<T> {

    var allEventChips = mutableListOf<EventChip<T>>()
    var normalEventChips = mutableListOf<EventChip<T>>()
    var allDayEventChips = mutableListOf<EventChip<T>>()

    var previousPeriodEvents: List<WeekViewEvent<T>>? = null
    var currentPeriodEvents: List<WeekViewEvent<T>>? = null
    var nextPeriodEvents: List<WeekViewEvent<T>>? = null

    var fetchedPeriods: FetchedPeriods? = null

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

    fun contains(period: Period): Boolean {
        return fetchedPeriods?.let {
            it.previous == period || it.current == period || it.next == period
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
    fun sortAndCacheEvents(config: WeekViewConfig, events: List<WeekViewEvent<T>>) {
        events.sorted().forEach { cacheEvent(config, it) }
    }

    /**
     * Cache the event for smooth scrolling functionality.
     *
     * @param event The event to cache.
     */
    private fun cacheEvent(config: WeekViewConfig, event: WeekViewEvent<T>) {
        if (event.startTime >= event.endTime) {
            return
        }

        val newChips = event.splitWeekViewEvents(config).map { EventChip(it, event, null) }
        allEventChips.addAll(newChips)
    }

}

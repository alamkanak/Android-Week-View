package com.alamkanak.weekview

import java.util.Calendar

internal class EventCache<T> {

    var previousPeriodEvents: List<WeekViewEvent<T>>? = null
    var currentPeriodEvents: List<WeekViewEvent<T>>? = null
    var nextPeriodEvents: List<WeekViewEvent<T>>? = null

    var fetchedRange: FetchRange? = null

    val hasEvents: Boolean
        get() = fetchedRange != null

    fun covers(fetchRange: FetchRange): Boolean {
        return fetchedRange?.let {
            it.previous == fetchRange.previous &&
                it.current == fetchRange.current &&
                it.next == fetchRange.next
        } ?: false
    }

    fun getAllDayEventsInRange(dateRange: List<Calendar>): List<WeekViewEvent<T>> {
        val events = previousPeriodEvents.orEmpty() +
            currentPeriodEvents.orEmpty() + nextPeriodEvents.orEmpty()
        val results = mutableListOf<WeekViewEvent<T>>()
        for (date in dateRange) {
            results += events.filter { it.isAllDay && it.isSameDay(date) }
        }
        return results
    }

    fun clear() {
        previousPeriodEvents = null
        currentPeriodEvents = null
        nextPeriodEvents = null
        fetchedRange = null
    }

    fun update(
        previousPeriodEvents: List<WeekViewEvent<T>>,
        currentPeriodEvents: List<WeekViewEvent<T>>,
        nextPeriodEvents: List<WeekViewEvent<T>>,
        fetchedRange: FetchRange
    ) {
        this.previousPeriodEvents = previousPeriodEvents
        this.currentPeriodEvents = currentPeriodEvents
        this.nextPeriodEvents = nextPeriodEvents
        this.fetchedRange = fetchedRange
    }
}

package com.alamkanak.weekview

import java.util.Calendar

internal class EventsCache<T> {

    var allEvents: MutableSet<WeekViewEvent<T>>? = null

    var previousPeriodEvents: List<WeekViewEvent<T>>? = null
    var currentPeriodEvents: List<WeekViewEvent<T>>? = null
    var nextPeriodEvents: List<WeekViewEvent<T>>? = null

    var fetchedRange: FetchRange? = null

    val isEmpty: Boolean
        get() = allEvents == null

    operator fun contains(fetchRange: FetchRange): Boolean {
        return fetchedRange?.let {
            it.previous == fetchRange.previous &&
                it.current == fetchRange.current &&
                it.next == fetchRange.next
        } ?: false
    }

    operator fun get(
        dateRange: List<Calendar>
    ) = allEvents.orEmpty().filter { dateRange.contains(it.startTime.atStartOfDay) }

    operator fun get(
        period: Period
    ): List<WeekViewEvent<T>>? {
        val range = checkNotNull(fetchedRange)
        return when (period) {
            range.previous -> previousPeriodEvents
            range.current -> currentPeriodEvents
            range.next -> nextPeriodEvents
            else -> throw IllegalStateException("Requesting events for invalid period $period")
        }
    }

    operator fun set(
        period: Period,
        events: List<WeekViewEvent<T>>
    ) {
        fetchedRange?.let { range ->
            when (period) {
                range.previous -> previousPeriodEvents = events
                range.current -> currentPeriodEvents = events
                range.next -> nextPeriodEvents = events
            }
        }

        if (allEvents == null) {
            allEvents = mutableSetOf()
        }
        allEvents?.addAll(events)
    }

    fun clear() {
        allEvents = null
        previousPeriodEvents = null
        currentPeriodEvents = null
        nextPeriodEvents = null
        fetchedRange = null
    }
}

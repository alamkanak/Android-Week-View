package com.alamkanak.weekview

import java.util.Calendar

internal class EventCache<T> {

    val allEvents: List<WeekViewEvent<T>>
        get() = previousPeriodEvents.orEmpty() +
            currentPeriodEvents.orEmpty() + nextPeriodEvents.orEmpty()

    var previousPeriodEvents: List<WeekViewEvent<T>>? = null
    var currentPeriodEvents: List<WeekViewEvent<T>>? = null
    var nextPeriodEvents: List<WeekViewEvent<T>>? = null

    var fetchedRange: FetchRange? = null

    val hasEvents: Boolean
        get() = fetchedRange != null

    operator fun contains(fetchRange: FetchRange): Boolean {
        return fetchedRange?.let {
            it.previous == fetchRange.previous &&
                it.current == fetchRange.current &&
                it.next == fetchRange.next
        } ?: false
    }

    operator fun get(
        dateRange: List<Calendar>
    ) = allEvents.filter { dateRange.contains(it.startTime.atStartOfDay) }

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
        val range = checkNotNull(fetchedRange)
        when (period) {
            range.previous -> previousPeriodEvents = events
            range.current -> currentPeriodEvents = events
            range.next -> nextPeriodEvents = events
        }
    }

    fun clear() {
        previousPeriodEvents = null
        currentPeriodEvents = null
        nextPeriodEvents = null
        fetchedRange = null
    }
}

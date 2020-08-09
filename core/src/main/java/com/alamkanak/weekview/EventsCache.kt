package com.alamkanak.weekview

import androidx.collection.ArrayMap
import java.util.Calendar

/**
 * An abstract class that provides functionality to cache [WeekViewEvent]s.
 */
internal abstract class EventsCache<T> {

    abstract val allEvents: List<ResolvedWeekViewEvent<T>>
    abstract fun update(events: List<ResolvedWeekViewEvent<T>>)
    abstract fun clear()

    operator fun get(id: Long): ResolvedWeekViewEvent<T>? = allEvents.firstOrNull { it.id == id }

    operator fun get(
        dateRange: List<Calendar>
    ): List<ResolvedWeekViewEvent<T>> {
        val startDate = checkNotNull(dateRange.min())
        val endDate = checkNotNull(dateRange.max())
        return allEvents.filter { it.endTime >= startDate || it.startTime <= endDate }
    }

    operator fun get(
        fetchRange: FetchRange
    ): List<ResolvedWeekViewEvent<T>> {
        val startTime = fetchRange.previous.startDate
        val endTime = fetchRange.next.endDate
        return allEvents.filter { it.endTime >= startTime && it.startTime <= endTime }
    }
}

/**
 * Represents an [EventsCache] that relies on a simple list of [WeekViewEvent]s. When updated with
 * new [WeekViewEvent]s, all existing ones are replaced.
 */
internal class SimpleEventsCache<T> : EventsCache<T>() {

    private var _allEvents: List<ResolvedWeekViewEvent<T>>? = null

    override val allEvents: List<ResolvedWeekViewEvent<T>>
        get() = _allEvents.orEmpty()

    override fun update(events: List<ResolvedWeekViewEvent<T>>) {
        _allEvents = events
    }

    override fun clear() {
        _allEvents = null
    }
}

/**
 * Represents an [EventsCache] that caches [ResolvedWeekViewEvent]s for their respective [Period]
 * and allows retrieval based on that [Period].
 */
internal class PagedEventsCache<T> : EventsCache<T>() {

    override val allEvents: List<ResolvedWeekViewEvent<T>>
        get() = eventsByPeriod.values.flatten()

    private val eventsByPeriod: ArrayMap<Period, List<ResolvedWeekViewEvent<T>>> = ArrayMap()

    override fun update(events: List<ResolvedWeekViewEvent<T>>) {
        val groupedEvents = events.groupBy { Period.fromDate(it.startTime) }
        for ((period, periodEvents) in groupedEvents) {
            eventsByPeriod[period] = periodEvents
        }
    }

    internal fun determinePeriodsToFetch(range: FetchRange) = range.periods.filter { it !in this }

    operator fun contains(period: Period) = eventsByPeriod.contains(period)

    operator fun contains(range: FetchRange) = eventsByPeriod.containsAll(range.periods)

    fun reserve(period: Period) {
        eventsByPeriod[period] = listOf()
    }

    override fun clear() {
        eventsByPeriod.clear()
    }
}

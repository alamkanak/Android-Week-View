package com.alamkanak.weekview

import androidx.collection.ArrayMap
import java.util.Calendar

/**
 * An abstract class that provides functionality to cache [ResolvedWeekViewEntity] elements.
 */
internal abstract class EventsCache {

    abstract val allEvents: List<ResolvedWeekViewEntity>
    abstract fun update(events: List<ResolvedWeekViewEntity>)
    abstract fun clear()

    operator fun get(id: Long): ResolvedWeekViewEntity? = allEvents.firstOrNull { it.id == id }

    operator fun get(
        dateRange: List<Calendar>
    ): List<ResolvedWeekViewEntity> {
        val startDate = checkNotNull(dateRange.minOrNull())
        val endDate = checkNotNull(dateRange.maxOrNull())
        return allEvents.filter { it.endTime >= startDate || it.startTime <= endDate }
    }

    operator fun get(
        fetchRange: FetchRange
    ): List<ResolvedWeekViewEntity> {
        val startTime = fetchRange.previous.startDate
        val endTime = fetchRange.next.endDate
        return allEvents.filter { it.endTime >= startTime && it.startTime <= endTime }
    }
}

/**
 * Represents an [EventsCache] that relies on a simple list of [ResolvedWeekViewEntity] objects.
 * When updated with new [ResolvedWeekViewEntity] objects, all existing ones are replaced.
 */
internal class SimpleEventsCache : EventsCache() {

    private var _allEvents: List<ResolvedWeekViewEntity>? = null

    override val allEvents: List<ResolvedWeekViewEntity>
        get() = _allEvents.orEmpty()

    override fun update(events: List<ResolvedWeekViewEntity>) {
        _allEvents = events
    }

    override fun clear() {
        _allEvents = null
    }
}

/**
 * Represents an [EventsCache] that caches [ResolvedWeekViewEntity]s for their respective [Period]
 * and allows retrieval based on that [Period].
 */
internal class PaginatedEventsCache : EventsCache() {

    override val allEvents: List<ResolvedWeekViewEntity>
        get() = eventsByPeriod.values.flatten()

    private val eventsByPeriod: ArrayMap<Period, List<ResolvedWeekViewEntity>> = ArrayMap()

    override fun update(events: List<ResolvedWeekViewEntity>) {
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

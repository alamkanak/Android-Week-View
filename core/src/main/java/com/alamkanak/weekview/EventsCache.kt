package com.alamkanak.weekview

import androidx.collection.ArrayMap
import java.util.Calendar

internal typealias EventsCacheProvider = () -> EventsCache?

/**
 * An abstract class that provides functionality to cache [ResolvedWeekViewEntity] elements.
 */
internal abstract class EventsCache {

    abstract val allEvents: List<ResolvedWeekViewEntity>
    abstract fun update(events: List<ResolvedWeekViewEntity>)
    abstract fun update(event: ResolvedWeekViewEntity)
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

    private var _allEvents: MutableList<ResolvedWeekViewEntity>? = null

    override val allEvents: List<ResolvedWeekViewEntity>
        get() = _allEvents.orEmpty()

    override fun update(events: List<ResolvedWeekViewEntity>) {
        _allEvents = events.toMutableList()
    }

    override fun update(event: ResolvedWeekViewEntity) {
        val index = _allEvents?.indexOfFirst { it.id == event.id }?.takeIf { it != -1 }

        if (index != null) {
            _allEvents?.removeAt(index)
            _allEvents?.add(index, event)
        }
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

    private val eventsByPeriod: ArrayMap<Period, MutableList<ResolvedWeekViewEntity>> = ArrayMap()

    override fun update(events: List<ResolvedWeekViewEntity>) {
        val groupedEvents = events.groupBy { it.period }
        for ((period, periodEvents) in groupedEvents) {
            eventsByPeriod[period] = periodEvents.toMutableList()
        }
    }

    override fun update(event: ResolvedWeekViewEntity) {
        val existingEvent = allEvents.firstOrNull { it.id == event.id } ?: return
        eventsByPeriod[existingEvent.period]?.removeAll { it.id == event.id }
        eventsByPeriod[event.period]?.add(event)
    }

    internal fun determinePeriodsToFetch(range: FetchRange) = range.periods.filter { it !in this }

    operator fun contains(period: Period) = eventsByPeriod.contains(period)

    operator fun contains(range: FetchRange) = eventsByPeriod.containsAll(range.periods)

    fun reserve(period: Period) {
        eventsByPeriod[period] = mutableListOf()
    }

    override fun clear() {
        eventsByPeriod.clear()
    }
}

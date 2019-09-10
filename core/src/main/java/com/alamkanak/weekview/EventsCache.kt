package com.alamkanak.weekview

import androidx.annotation.VisibleForTesting
import java.util.Calendar

/**
 * Wraps all available [EventsCache]s to allow for dynamic switching between them.
 */
internal class EventsCacheWrapper<T> {

    private val simpleEventsCache: EventsCache<T> = SimpleEventsCache()
    private val pagedEventsCache: EventsCache<T> = PagedEventsCache()

    private var currentEventsCache = simpleEventsCache

    /**
     * Returns the [EventsCache] that is currently in use.
     */
    fun get() = currentEventsCache

    /**
     * Switches the currently used [EventsCache] to a [PagedEventsCache] (if [listener] is not null)
     * or [SimpleEventsCache] (otherwise).
     */
    fun onListenerChanged(listener: OnMonthChangeListener<T>?) {
        currentEventsCache = if (listener != null) pagedEventsCache else simpleEventsCache
    }

    /**
     * Switches the currently used [EventsCache] to a [PagedEventsCache] (if [listener] is not null)
     * or [SimpleEventsCache] (otherwise).
     */
    fun onListenerChanged(listener: OnLoadMoreListener?) {
        currentEventsCache = if (listener != null) pagedEventsCache else simpleEventsCache
    }
}

/**
 * An abstract class that provides functionality to cache [WeekViewEvent]s.
 */
internal abstract class EventsCache<T> {

    abstract val allEvents: List<WeekViewEvent<T>>
    abstract fun clear()

    operator fun get(
        dateRange: List<Calendar>
    ) = allEvents.filter { dateRange.contains(it.startTime.atStartOfDay) }

    operator fun get(
        fetchRange: FetchRange
    ): List<WeekViewEvent<T>> {
        val startTime = fetchRange.previous.startDate
        val endTime = fetchRange.next.endDate
        return allEvents.filter { it.endTime >= startTime && it.startTime <= endTime }
    }

    open operator fun get(period: Period): List<WeekViewEvent<T>>? = null
}

/**
 * Represents an [EventsCache] that relies on a simple list of [WeekViewEvent]s. When updated with
 * new [WeekViewEvent]s, all existing ones are replaced.
 */
internal class SimpleEventsCache<T> : EventsCache<T>() {

    private var _allEvents: List<WeekViewEvent<T>>? = null

    override val allEvents: List<WeekViewEvent<T>>
        get() = _allEvents.orEmpty()

    fun update(events: List<WeekViewEvent<T>>) {
        _allEvents = events
    }

    override fun clear() {
        _allEvents = null
    }
}

/**
 * Represents an [EventsCache] that caches [WeekViewEvent]s for a particular [FetchRange]. It is
 * used in combination with [PagedEventsLoader] or [LegacyEventsLoader].
 */
internal class PagedEventsCache<T> : EventsCache<T>() {

    override val allEvents: List<WeekViewEvent<T>>
        get() = previousPeriodEvents.orEmpty() +
            currentPeriodEvents.orEmpty() +
            nextPeriodEvents.orEmpty()

    private var previousPeriodEvents: List<WeekViewEvent<T>>? = null
    private var currentPeriodEvents: List<WeekViewEvent<T>>? = null
    private var nextPeriodEvents: List<WeekViewEvent<T>>? = null

    @VisibleForTesting
    internal var fetchedRange: FetchRange? = null

    operator fun contains(period: Period) = fetchedRange?.periods?.contains(period) ?: false

    operator fun contains(fetchRange: FetchRange) = fetchedRange?.isEqual(fetchRange) ?: false

    override fun get(period: Period): List<WeekViewEvent<T>>? {
        val range = checkNotNull(fetchedRange)
        return when (period) {
            range.previous -> previousPeriodEvents
            range.current -> currentPeriodEvents
            range.next -> nextPeriodEvents
            else -> throw IllegalStateException("Requesting events for invalid period $period")
        }
    }

    /**
     * Adjusts the [WeekViewEvent]s of the fetched [Period]s to the new [FetchRange]. This means
     * that if the user scrolled to the next month, the following would happen:
     *  1. The events of the current month would be moved to [previousPeriodEvents].
     *  2. The events of the next month would be moved to [currentPeriodEvents].
     *  3. The events of the following month aren't loaded yet; [nextPeriodEvents] will be null.
     *  4. The [fetchedRange] will be set to the provided [FetchRange].
     *
     * When scrolling to the previous month, the behavior would be like so:
     *  1. The events of the current month would be moved to [nextPeriodEvents].
     *  2. The events of the previous month would be moved to [currentPeriodEvents].
     *  2. The events of the preceding month aren't loaded yet; [previousPeriodEvents] will be null.
     *  4. The [fetchedRange] will be set to the provided [FetchRange].
     */
    fun adjustToFetchRange(fetchRange: FetchRange) {
        val oldFetchRange = fetchedRange ?: fetchRange
        val newCurrentPeriod = fetchRange.current

        val previousEvents = when (newCurrentPeriod) {
            oldFetchRange.previous -> null
            oldFetchRange.next -> currentPeriodEvents
            else -> previousPeriodEvents
        }

        val currentEvents = when (newCurrentPeriod) {
            oldFetchRange.previous -> previousPeriodEvents
            oldFetchRange.next -> nextPeriodEvents
            else -> currentPeriodEvents
        }

        val nextEvents = when (newCurrentPeriod) {
            oldFetchRange.previous -> currentPeriodEvents
            oldFetchRange.next -> null
            else -> nextPeriodEvents
        }

        previousPeriodEvents = previousEvents
        currentPeriodEvents = currentEvents
        nextPeriodEvents = nextEvents
        fetchedRange = fetchRange
    }

    fun update(eventsByPeriod: Map<Period, List<WeekViewEvent<T>>>) {
        for ((period, events) in eventsByPeriod) {
            update(period, events)
        }
    }

    private fun update(period: Period, events: List<WeekViewEvent<T>>) {
        val range = checkNotNull(fetchedRange)
        when (period) {
            range.previous -> previousPeriodEvents = events
            range.current -> currentPeriodEvents = events
            range.next -> nextPeriodEvents = events
        }
    }

    operator fun set(
        period: Period,
        events: List<WeekViewEvent<T>>
    ) {
        update(period, events)
    }

    override fun clear() {
        previousPeriodEvents = null
        currentPeriodEvents = null
        nextPeriodEvents = null
        fetchedRange = null
    }
}

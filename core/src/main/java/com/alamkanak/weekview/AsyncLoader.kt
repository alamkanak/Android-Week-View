package com.alamkanak.weekview

import java.util.Calendar

/**
 * This class enables asynchronous loading of [WeekViewEvent]s. While
 * [OnMonthChangeListener.onMonthChange] requires a result, this class's [onLoadMoreListener]
 * does not. It only acts as a way to notify any potential data provider that [WeekView] is
 * requesting more events for a particular period. To update events in [WeekView], this class
 * provides a [submit] method.
 */
internal class AsyncLoader<T>(
    private val eventsCache: EventsCache<T>,
    private val eventChipsLoader: EventChipsLoader<T>
) : OnMonthChangeListener<T> {

    var onLoadMoreListener: OnLoadMoreListener? = null

    override fun onMonthChange(
        startDate: Calendar,
        endDate: Calendar
    ): List<WeekViewDisplayable<T>> {
        onLoadMoreListener?.onLoadMore(startDate, endDate)
        return emptyList()
    }

    /**
     * Caches the provided [WeekViewDisplayable]s and returns whether the submitted events fall
     * into the currently displayed date range. If so, [WeekView] can decide to invalidate the view
     * and thus accommodate the changes.
     *
     * @param items The list of new [WeekViewDisplayable]s
     * @param dateRange The list of currently visible dates
     */
    fun submit(
        items: List<WeekViewDisplayable<T>>,
        dateRange: List<Calendar>
    ): Boolean {
        val events = items.map { it.toWeekViewEvent() }
        val startDate = events.map { it.startTime.atStartOfDay }.min()
        val endDate = events.map { it.startTime.atEndOfDay }.max()

        if (startDate == null || endDate == null) {
            // If these are null, this would indicate that the submitted list of events is empty.
            // The new items are empty, but it's possible that WeekView is currently displaying
            // events.
            val currentEvents = eventsCache[dateRange]
            eventsCache.clear()
            return currentEvents.isNotEmpty()
        }

        val eventsByPeriod = mapEventsToPeriod(events)

        if (onLoadMoreListener == null) {
            // If no OnLoadMoreListener is set, the consumer of the library is not using paged event
            // loading. Therefore, we clear the event cache before adding the new events. Otherwise,
            // we would simply add the submitted events to the events already in the cache.
            eventsCache.clear()
        }

        updateEventsCache(eventsByPeriod)
        cacheEventChips(events)

        return dateRange.any { it.isBetween(startDate, endDate, inclusive = true) }
    }

    private fun mapEventsToPeriod(
        events: List<WeekViewEvent<T>>
    ) = events.groupBy { Period.fromDate(it.startTime) }

    private fun updateEventsCache(eventsByPeriod: Map<Period, List<WeekViewEvent<T>>>) {
        /*val periods = eventsByPeriod.keys.sorted()
        eventsCache.fetchedRange = when (periods.size) {
            3 -> FetchRange.fromList(periods)
            1 -> FetchRange.create(periods.single())
            else -> throw IllegalStateException("AsyncLoader attempted to cache " +
                "${periods.size} periods, which should not be possible.")
        }*/

        for ((period, events) in eventsByPeriod) {
            eventsCache[period] = events
        }
    }

    private fun cacheEventChips(events: List<WeekViewEvent<T>>) {
        eventChipsLoader.createAndCacheEventChips(events)
    }
}

package com.alamkanak.weekview

import java.util.Calendar
import java.util.concurrent.Executors

/**
 * A helper class that caches the submitted [WeekViewEvent]s and creates [EventChip]s on a
 * background thread.
 */
internal class EventsDiffer<T>(
    private val eventsCacheWrapper: EventsCacheWrapper<T>,
    private val eventChipsLoader: EventChipsLoader<T>
) {

    private val executor = Executors.newSingleThreadExecutor()

    /**
     * Updates the [EventsCache] with the provided [WeekViewDisplayable]s and creates [EventChip]s.
     *
     * @param items The list of new [WeekViewDisplayable]s
     * @param dateRange The list of currently visible dates
     * @param onFinished Callback to inform the caller whether [WeekView] should invalidate.
     */
    fun submit(
        items: List<WeekViewDisplayable<T>>,
        dateRange: List<Calendar>,
        onFinished: (Boolean) -> Unit
    ) {
        executor.execute {
            val result = submitItems(items, dateRange)
            onFinished(result)
        }
    }

    private fun submitItems(
        items: List<WeekViewDisplayable<T>>,
        dateRange: List<Calendar>
    ): Boolean {
        val events = items.map { it.toWeekViewEvent() }
        val startDate = events.map { it.startTime.atStartOfDay }.min()
        val endDate = events.map { it.startTime.atEndOfDay }.max()

        val eventsCache = eventsCacheWrapper.get()

        if (startDate == null || endDate == null) {
            // If these are null, this would indicate that the submitted list of events is empty.
            // The new items are empty, but it's possible that WeekView is currently displaying
            // events.
            val currentEvents = eventsCache[dateRange]
            eventsCache.clear()
            return currentEvents.isNotEmpty()
        }

        when (eventsCache) {
            is SimpleEventsCache -> eventsCache.update(events)
            is PagedEventsCache -> eventsCache.update(mapEventsToPeriod(events))
        }

        eventChipsLoader.createAndCacheEventChips(events)
        return dateRange.any { it.isBetween(startDate, endDate, inclusive = true) }
    }

    private fun mapEventsToPeriod(
        events: List<WeekViewEvent<T>>
    ) = events.groupBy { Period.fromDate(it.startTime) }
}

package com.alamkanak.weekview

import java.util.Calendar

private typealias EventsTriple<T> =
    Triple<List<WeekViewEvent<T>>?, List<WeekViewEvent<T>>?, List<WeekViewEvent<T>>?>
private fun <T> EventsTriple<T>.shiftLeft(): EventsTriple<T> = EventsTriple(second, third, null)
private fun <T> EventsTriple<T>.shiftRight(): EventsTriple<T> = EventsTriple(null, first, second)

/**
 * This class is responsible for loading [WeekViewEvent]s into [WeekView]. It uses
 * [OnMonthChangeListener.onMonthChange] to synchronously load events whenever the currently
 * displayed month changes. Asynchronous event loading can be performed via [AsyncLoader].
 */
internal class EventsLoader<T>(
    private val cache: EventsCache<T>
) {

    var shouldRefreshEvents: Boolean = false
    var onMonthChangeListener: OnMonthChangeListener<T>? = null

    /**
     * Returns a list of [WeekViewEvent]s of the [FetchRange] around the first visible date, or
     * null if:
     *
     * a) No [OnMonthChangeListener] is registered
     *
     * b) Events don't need to be refreshed due to caching
     */
    fun loadEventsIfNecessary(firstVisibleDate: Calendar?): List<WeekViewEvent<T>>? {
        if (onMonthChangeListener == null) {
            // No OnMonthChangeListener is set. This is possible if an OnLoadMoreListener
            // is used instead of an OnMonthChangeListener.
            return cache.allEvents.orEmpty().toList()
        }

        val hasNoEvents = cache.isEmpty
        val firstVisibleDay = checkNotNull(firstVisibleDate)
        val fetchPeriods = FetchRange.create(firstVisibleDay)

        return if (hasNoEvents || shouldRefreshEvents || fetchPeriods !in cache) {
            loadEvents(fetchPeriods)
        } else {
            cache.allEvents.orEmpty().toList()
        }
    }

    private fun loadEvents(fetchRange: FetchRange): List<WeekViewEvent<T>> {
        if (shouldRefreshEvents) {
            shouldRefreshEvents = false
            cache.clear()
        }

        prepareCache(fetchRange)

        val periodsToBeLoaded = fetchRange.periods
            .map { it to cache[it] }
            .filter { it.second == null }
            .map { it.first }

        val results = mutableListOf<WeekViewEvent<T>>()
        periodsToBeLoaded.forEach { period ->
            val periodEvents = loadEvents(period)
            cache[period] = periodEvents
            results += periodEvents
        }
        return results
    }

    private fun prepareCache(fetchRange: FetchRange) {
        val oldFetchRange = cache.fetchedRange ?: fetchRange
        val newCurrentPeriod = fetchRange.current

        val previousEvents = when (newCurrentPeriod) {
            oldFetchRange.previous -> null
            oldFetchRange.next -> cache.currentPeriodEvents
            else -> cache.previousPeriodEvents
        }

        val currentEvents = when (newCurrentPeriod) {
            oldFetchRange.previous -> cache.previousPeriodEvents
            oldFetchRange.next -> cache.nextPeriodEvents
            else -> cache.currentPeriodEvents
        }

        val nextEvents = when (newCurrentPeriod) {
            oldFetchRange.previous -> cache.currentPeriodEvents
            oldFetchRange.next -> null
            else -> cache.nextPeriodEvents
        }

        cache.previousPeriodEvents = previousEvents
        cache.currentPeriodEvents = currentEvents
        cache.nextPeriodEvents = nextEvents
        cache.fetchedRange = fetchRange
    }

    private fun loadEvents(period: Period): List<WeekViewEvent<T>> {
        val listener = checkNotNull(onMonthChangeListener)

        val startDate = today()
            .withYear(period.year)
            .withMonth(period.month)
            .withDayOfMonth(1)

        val maxDays = startDate.lengthOfMonth
        val endDate = startDate
            .withDayOfMonth(maxDays)
            .atEndOfDay

        return listener
            .onMonthChange(startDate, endDate)
            .map { it.toWeekViewEvent() }
    }
}

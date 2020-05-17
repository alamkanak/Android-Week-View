package com.alamkanak.weekview

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.Calendar
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class MainExecutor : Executor {
    private val handler = Handler(Looper.getMainLooper())
    override fun execute(runnable: Runnable) {
        handler.post(runnable)
    }
}

/**
 * A helper class that caches the submitted [WeekViewEvent]s and creates [EventChip]s on a
 * background thread.
 */
internal class EventsDiffer<T>(
    private val context: Context,
    private val eventsCacheWrapper: EventsCacheWrapper<T>,
    private val eventChipsLoader: EventChipsLoader<T>,
    private val eventChipsCache: EventChipsCache<T>,
    private val drawingContext: DrawingContext
) {

    private val backgroundExecutor = Executors.newSingleThreadExecutor()
    private val mainThreadExecutor = MainExecutor()

    /**
     * Updates the [EventsCache] with the provided [WeekViewDisplayable]s and creates [EventChip]s.
     *
     * @param items The list of new [WeekViewDisplayable]s
     * @param onFinished Callback to inform the caller whether [WeekView] should invalidate.
     */
    fun submit(
        items: List<WeekViewDisplayable<T>>,
        onFinished: (Boolean) -> Unit
    ) {
        backgroundExecutor.execute {
            val dateRange = drawingContext.dateRange
            // It's possible that weekView.submit() is called before the date range has been
            // initialized. Therefor, waiting until the date range is actually set may be required.
            while (dateRange.isEmpty()) {
                Thread.sleep(100L)
                continue
            }

            val result = submitItems(items, dateRange)
            mainThreadExecutor.execute {
                onFinished(result)
            }
        }
    }

    private fun submitItems(
        items: List<WeekViewDisplayable<T>>,
        dateRange: List<Calendar>
    ): Boolean {
        val events = items.map { it.toResolvedWeekViewEvent(context) }
        val startDate = events.map { it.startTime.atStartOfDay }.min()
        val endDate = events.map { it.endTime.atEndOfDay }.max()

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

        eventChipsCache += eventChipsLoader.createEventChips(events)
        return dateRange.any { it.isBetween(startDate, endDate, inclusive = true) }
    }

    private fun mapEventsToPeriod(
        events: List<ResolvedWeekViewEvent<T>>
    ) = events.groupBy { Period.fromDate(it.startTime) }
}

package com.alamkanak.weekview

import android.content.Context
import android.os.Handler
import android.os.Looper
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
    private val eventsCache: EventsCache<T>,
    private val eventChipsFactory: EventChipsFactory,
    private val eventChipsCache: EventChipsCache
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
        viewState: ViewState,
        onFinished: () -> Unit
    ) {
        backgroundExecutor.execute { submitItems(items, viewState)
            mainThreadExecutor.execute {
                onFinished()
            }
        }
    }

    private fun submitItems(
        items: List<WeekViewDisplayable<T>>,
        viewState: ViewState
    ) {
        val events = items.map { it.toResolvedWeekViewEvent(context) }
        val startDate = events.map { it.startTime.atStartOfDay }.min()
        val endDate = events.map { it.endTime.atEndOfDay }.max()

        if (startDate == null || endDate == null) {
            // If these are null, this would indicate that the submitted list of events is empty.
            // The new items are empty, but it's possible that WeekView is currently displaying
            // events.
            eventsCache.clear()
            return
        }

        when (eventsCache) {
            is SimpleEventsCache -> eventsCache.update(events)
            is PagedEventsCache -> eventsCache.update(mapEventsToPeriod(events))
        }

        eventChipsCache += eventChipsFactory.createEventChips(events, viewState)
    }

    private fun mapEventsToPeriod(
        events: List<ResolvedWeekViewEvent<T>>
    ) = events.groupBy { Period.fromDate(it.startTime) }
}

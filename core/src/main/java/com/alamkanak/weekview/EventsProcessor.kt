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
 * A helper class that processes the submitted [WeekViewEvent]s and creates [EventChip]s on a
 * background thread.
 */
internal class EventsProcessor<T>(
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
     * @param viewState The current [ViewState] of [WeekView]
     * @param onFinished Callback to inform the caller whether [WeekView] should invalidate.
     */
    fun submit(
        items: List<WeekViewDisplayable<T>>,
        viewState: ViewState,
        onFinished: () -> Unit
    ) {
        backgroundExecutor.execute {
            submitItems(items, viewState)
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
        val startDate = events.map { it.startTime.atStartOfDay }.minOrNull()
        val endDate = events.map { it.endTime.atEndOfDay }.maxOrNull()

        if (startDate == null || endDate == null) {
            // The new items are empty, but it's possible that WeekView is currently displaying
            // events.
            eventsCache.clear()
            return
        }

        eventsCache.update(events)

        if (eventsCache is SimpleEventsCache) {
            // When using SimpleEventsCache, we completely replace all event chips that are
            // currently cached.
            eventChipsCache.clear()
        }

        eventChipsCache += eventChipsFactory.createEventChips(events, viewState)
    }
}

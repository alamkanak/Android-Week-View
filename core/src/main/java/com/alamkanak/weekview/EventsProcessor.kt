package com.alamkanak.weekview

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.WorkerThread
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class MainExecutor : Executor {
    private val handler = Handler(Looper.getMainLooper())
    override fun execute(runnable: Runnable) {
        handler.post(runnable)
    }
}

/**
 * A helper class that processes the submitted [WeekViewEntity] objects and creates [EventChip]s
 * on a background thread.
 */
internal class EventsProcessor(
    private val context: Context,
    private val eventsCache: EventsCache,
    private val eventChipsFactory: EventChipsFactory,
    private val eventChipsCache: EventChipsCache
) {

    private val backgroundExecutor = Executors.newSingleThreadExecutor()
    private val mainThreadExecutor = MainExecutor()

    /**
     * Updates the [EventsCache] with the provided [WeekViewEntity] elements and creates
     * [EventChip]s.
     *
     * @param entities The list of new [WeekViewEntity] elements
     * @param viewState The current [ViewState] of [WeekView]
     * @param onFinished Callback to inform the caller whether [WeekView] should invalidate.
     */
    fun submit(
        entities: List<WeekViewEntity>,
        viewState: ViewState,
        onFinished: () -> Unit
    ) {
        backgroundExecutor.execute {
            submitItems(entities, viewState)
            mainThreadExecutor.execute {
                onFinished()
            }
        }
    }

    @WorkerThread
    private fun submitItems(
        items: List<WeekViewEntity>,
        viewState: ViewState
    ) {
        val resolvedItems = items.map { it.resolve(context) }
        eventsCache.update(resolvedItems)

        if (eventsCache is SimpleEventsCache) {
            val eventChips = eventChipsFactory.create(resolvedItems, viewState)
            eventChipsCache.replaceAll(eventChips)
        } else {
            val existingIds = eventChipsCache.eventIds
            val newResolvedItems = resolvedItems.filterNot { it.id in existingIds }
            val eventChips = eventChipsFactory.create(newResolvedItems, viewState)
            eventChipsCache.addAll(eventChips)
        }
    }
}

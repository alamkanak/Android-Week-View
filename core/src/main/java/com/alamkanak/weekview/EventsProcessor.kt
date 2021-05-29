package com.alamkanak.weekview

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * A helper class that processes the submitted [WeekViewEntity] objects and creates [EventChip]s
 * on a background thread.
 */
internal class EventsProcessor(
    private val context: Context,
    private val eventsCache: EventsCache,
    private val eventChipsFactory: EventChipsFactory,
    private val eventChipsCache: EventChipsCache,
    private val backgroundExecutor: Executor = Executors.newSingleThreadExecutor(),
    private val mainThreadExecutor: Executor = ContextCompat.getMainExecutor(context),
) {

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
            submitEntities(entities, viewState)
            mainThreadExecutor.execute {
                onFinished()
            }
        }
    }

    internal fun updateDraggedEntity(
        event: ResolvedWeekViewEntity,
        viewState: ViewState
    ) {
        eventsCache.update(event)
        eventChipsCache.remove(eventId = event.id)

        val eventChips = eventChipsFactory.create(listOf(event), viewState)
        eventChipsCache.addAll(eventChips)
    }

    @WorkerThread
    private fun submitEntities(
        entities: List<WeekViewEntity>,
        viewState: ViewState
    ) {
        val resolvedEntities = entities.map { it.resolve(context) }
        eventsCache.update(resolvedEntities)

        if (eventsCache is SimpleEventsCache) {
            submitEntitiesToSimpleCache(resolvedEntities, viewState)
        } else {
            submitEntitiesToPagedCache(resolvedEntities, viewState)
        }
    }

    private fun submitEntitiesToSimpleCache(
        entities: List<ResolvedWeekViewEntity>,
        viewState: ViewState,
    ) {
        val eventChips = eventChipsFactory.create(entities, viewState)
        eventChipsCache.replaceAll(eventChips)
    }

    private fun submitEntitiesToPagedCache(
        entities: List<ResolvedWeekViewEntity>,
        viewState: ViewState,
    ) {
        val diffResult = performDiff(entities)
        eventChipsCache.removeAll(diffResult.itemsToRemove)

        val eventChips = eventChipsFactory.create(diffResult.itemsToAddOrUpdate, viewState)
        eventChipsCache.addAll(eventChips)
    }

    private fun performDiff(newEntities: List<ResolvedWeekViewEntity>): DiffResult {
        val existingEventChips = eventChipsCache.allEventChips
        val existingEntities = existingEventChips.map { it.event }
        return DiffResult.calculateDiff(
            existingEntities = existingEntities,
            newEntities = newEntities,
        )
    }

    data class DiffResult(
        val itemsToAddOrUpdate: List<ResolvedWeekViewEntity>,
        val itemsToRemove: List<ResolvedWeekViewEntity>,
    ) {
        companion object {
            fun calculateDiff(
                existingEntities: List<ResolvedWeekViewEntity>,
                newEntities: List<ResolvedWeekViewEntity>,
            ): DiffResult {
                val existingEntityIds = existingEntities.map { it.id }

                val submittedEntityIds = newEntities.map { it.id }
                val addedEvents = newEntities.filter { it.id !in existingEntityIds }
                val deletedEvents = existingEntities.filter { it.id !in submittedEntityIds }

                val updatedEvents = newEntities.filter { it.id in existingEntityIds }
                val changed = updatedEvents.filter { it !in existingEntities }

                return DiffResult(
                    itemsToAddOrUpdate = addedEvents + changed,
                    itemsToRemove = deletedEvents,
                )
            }
        }
    }
}

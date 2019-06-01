package com.alamkanak.weekview

internal class EventChipsProvider<T>(
    private val config: WeekViewConfigWrapper,
    private val cache: WeekViewCache<T>,
    private val viewState: WeekViewViewState
) {

    var monthLoader: MonthLoader<T>? = null

    fun loadEventsIfNecessary() {
        val hasNoEvents = cache.allEventChips.isEmpty()
        val shouldRefresh = viewState.shouldRefreshEvents

        val firstVisibleDay = checkNotNull(viewState.firstVisibleDay)
        val fetchPeriods = FetchPeriods.create(firstVisibleDay)

        if (hasNoEvents || shouldRefresh || !cache.covers(fetchPeriods)) {
            loadEventsAndCalculateEventChipPositions(fetchPeriods)
            viewState.shouldRefreshEvents = false
        }
    }

    private fun loadEventsAndCalculateEventChipPositions(fetchPeriods: FetchPeriods) {
        if (viewState.shouldRefreshEvents) {
            cache.clear()
        }

        loadEvents(fetchPeriods)
        calculateEventChipPositions()
    }

    private fun loadEvents(fetchPeriods: FetchPeriods) {
        val oldFetchPeriods = cache.fetchedPeriods ?: fetchPeriods
        val newCurrentPeriod = fetchPeriods.current

        var previousPeriodEvents: List<WeekViewEvent<T>>? = null
        var currentPeriodEvents: List<WeekViewEvent<T>>? = null
        var nextPeriodEvents: List<WeekViewEvent<T>>? = null

        if (cache.hasEvents) {
            when (newCurrentPeriod) {
                oldFetchPeriods.previous -> {
                    currentPeriodEvents = cache.previousPeriodEvents
                    nextPeriodEvents = cache.currentPeriodEvents
                }
                oldFetchPeriods.current -> {
                    previousPeriodEvents = cache.previousPeriodEvents
                    currentPeriodEvents = cache.currentPeriodEvents
                    nextPeriodEvents = cache.nextPeriodEvents
                }
                oldFetchPeriods.next -> {
                    previousPeriodEvents = cache.currentPeriodEvents
                    currentPeriodEvents = cache.nextPeriodEvents
                }
            }
        }

        val loader = checkNotNull(monthLoader) { "No WeekViewLoader or MonthChangeListener provided." }

        if (previousPeriodEvents == null) {
            previousPeriodEvents = loader.load(fetchPeriods.previous)
        }

        if (currentPeriodEvents == null) {
            currentPeriodEvents = loader.load(fetchPeriods.current)
        }

        if (nextPeriodEvents == null) {
            nextPeriodEvents = loader.load(fetchPeriods.next)
        }

        cache.update(previousPeriodEvents, currentPeriodEvents, nextPeriodEvents, fetchPeriods)
    }

    private fun calculateEventChipPositions() {
        val results = mutableListOf<EventChip<T>>()
        val groups = cache.allEventChips.groupBy { it.event.startTime.atStartOfDay }

        for (eventChips in groups.values) {
            computePositionOfEvents(eventChips)
            results += eventChips
        }

        cache.put(results)
    }

    /**
     * Forms [CollisionGroup]s for all event chips and uses them to expand the [EventChip]s to their
     * maximum width.
     *
     * @param eventChips A list of [EventChip]s
     */
    private fun computePositionOfEvents(eventChips: List<EventChip<T>>) {
        val collisionGroups = mutableListOf<CollisionGroup<T>>()

        for (eventChip in eventChips) {
            val collidingGroup = collisionGroups.firstOrNull { it.collidesWith(eventChip) }

            if (collidingGroup != null) {
                collidingGroup.add(eventChip)
            } else {
                collisionGroups += CollisionGroup(eventChip)
            }
        }

        for (collisionGroup in collisionGroups) {
            expandEventsToMaxWidth(collisionGroup.eventChips)
        }
    }

    /**
     * Expand all [EventChip]s in a [CollisionGroup] to their maximum width.
     *
     * @param eventChips The [EventChip]s of a [CollisionGroup]
     */
    private fun expandEventsToMaxWidth(eventChips: List<EventChip<T>>) {
        val columns = mutableListOf<Column<T>>(Column())

        for (eventChip in eventChips) {
            val fittingColumn = columns.firstOrNull { it.fits(eventChip) }
            if (fittingColumn != null) {
                fittingColumn.add(eventChip)
            } else {
                columns += Column(eventChip)
            }
        }

        val rows = columns.map { it.size }.max() ?: 0

        for (row in 0 until rows) {
            columns.forEachIndexed { index, column ->
                val hasEventInRow = column.size > row

                if (hasEventInRow) {
                    val eventChip = column[row]

                    // Every column gets the same width. For instance, if there are four columns,
                    // then each column's width is 0.25.
                    eventChip.width = 1f / columns.size

                    // The start position is calculated based on the index of the column. For
                    // instance, if there are four columns, the start positions will be 0, 0.25, 0.5
                    // and 0.75.
                    eventChip.left = index.toFloat() / columns.size

                    eventChip.calculateTopAndBottom(config)
                }
            }
        }
    }

    /**
     * This class encapsulates [EventChip]s that collide with each other, meaning that they overlap
     * from a time perspective.
     *
     */
    private class CollisionGroup<T>(
        val eventChips: MutableList<EventChip<T>>
    ) {

        constructor(eventChip: EventChip<T>) : this(mutableListOf(eventChip))

        /**
         * Returns whether an [EventChip] collides with any [EventChip] already in the
         * [CollisionGroup].
         *
         * @param eventChip An [EventChip]
         * @return Whether a collision exists
         */
        fun collidesWith(eventChip: EventChip<T>): Boolean {
            return eventChips.any { it.event.collidesWith(eventChip.event) }
        }

        fun add(eventChip: EventChip<T>) {
            eventChips.add(eventChip)
        }

    }

    /**
     * This class encapsulates [EventChip]s that are displayed in the same column.
     */
    private class Column<T>(
        val eventChips: MutableList<EventChip<T>> = mutableListOf()
    ) {

        constructor(eventChip: EventChip<T>) : this(mutableListOf(eventChip))

        val isEmpty: Boolean
            get() = eventChips.isEmpty()

        val size: Int
            get() = eventChips.size

        fun add(eventChip: EventChip<T>) {
            eventChips.add(eventChip)
        }

        operator fun get(index: Int): EventChip<T> {
            return eventChips[index]
        }

        fun fits(eventChip: EventChip<T>): Boolean {
            return isEmpty || !eventChips.last().event.collidesWith(eventChip.event)
        }

    }

}

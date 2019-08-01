package com.alamkanak.weekview

internal class EventChipsExpander<T>(
    private val config: WeekViewConfigWrapper,
    private val chipCache: EventChipCache<T>
) {

    fun calculateEventChipPositions() {
        val groups = chipCache.groupedByDate()
        val groupEventChips = groups.values

        for (eventChips in groupEventChips) {
            computePositionOfEvents(eventChips)
            chipCache += eventChips
        }
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
            expandEventsToMaxWidth(collisionGroup)
        }
    }

    /**
     * Expands all [EventChip]s in a [CollisionGroup] to their maximum width.
     */
    private fun expandEventsToMaxWidth(collisionGroup: CollisionGroup<T>) {
        val columns = mutableListOf<Column<T>>()
        columns += Column(index = 0)

        for (eventChip in collisionGroup.eventChips) {
            val fittingColumns = columns.filter { it.fits(eventChip) }
            when (fittingColumns.size) {
                0 -> {
                    val index = columns.size
                    columns += Column(index, eventChip)
                }
                1 -> {
                    val fittingColumn = fittingColumns.single()
                    fittingColumn.add(eventChip)
                }
                else -> {
                    // This event chip can span multiple columns.
                    val areAdjacentColumns = fittingColumns.map { it.index }.isContinuous
                    if (areAdjacentColumns) {
                        for (column in fittingColumns) {
                            column.add(eventChip)
                        }
                    } else {
                        val leftMostColumn = checkNotNull(fittingColumns.minBy { it.index })
                        leftMostColumn.add(eventChip)
                    }
                }
            }
        }

        val rows = columns.map { it.size }.max() ?: 0
        val columnWidth = 1f / columns.size

        for (row in 0 until rows) {
            val zipped = columns.zipWithPrevious()
            for ((previous, current) in zipped) {
                val hasEventInRow = current.size > row
                if (hasEventInRow) {
                    expandColumnEventToMaxWidth(current, previous, row, columnWidth, columns.size)
                }
            }
        }

        for (eventChip in collisionGroup.eventChips) {
            calculateMinutesFromStart(eventChip)
        }
    }

    private fun calculateMinutesFromStart(eventChip: EventChip<T>) {
        val event = eventChip.event
        if (event.isAllDay) {
            return
        }

        val hoursFromStart = event.startTime.hour - config.minHour
        eventChip.minutesFromStartHour = hoursFromStart * 60 + event.startTime.minute
    }

    private fun expandColumnEventToMaxWidth(
        current: Column<T>,
        previous: Column<T>?,
        row: Int,
        columnWidth: Float,
        columns: Int
    ) {
        val index = current.index
        val eventChip = current[row]

        val duplicateInPreviousColumn = previous?.findDuplicate(eventChip)

        if (duplicateInPreviousColumn != null) {
            duplicateInPreviousColumn.relativeWidth += columnWidth
        } else {
            // Every column gets the same width. For instance, if there are four columns,
            // then each column's width is 0.25.
            eventChip.relativeWidth = columnWidth

            // The start position is calculated based on the index of the column. For
            // instance, if there are four columns, the start positions will be 0.0, 0.25, 0.5
            // and 0.75.
            eventChip.relativeStart = index.toFloat() / columns
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
        val index: Int,
        val eventChips: MutableList<EventChip<T>> = mutableListOf()
    ) {

        constructor(index: Int, eventChip: EventChip<T>) : this(index, mutableListOf(eventChip))

        val isEmpty: Boolean
            get() = eventChips.isEmpty()

        val size: Int
            get() = eventChips.size

        fun add(eventChip: EventChip<T>) {
            eventChips.add(eventChip)
        }

        fun findDuplicate(eventChip: EventChip<T>) = eventChips.firstOrNull { it == eventChip }

        operator fun get(index: Int): EventChip<T> = eventChips[index]

        fun fits(eventChip: EventChip<T>): Boolean {
            return isEmpty || !eventChips.last().event.collidesWith(eventChip.event)
        }
    }

    private val List<Int>.isContinuous: Boolean
        get() {
            val zipped = sorted().zipWithNext()
            return zipped.all { it.first + 1 == it.second }
        }

    private fun <T> List<T>.zipWithPrevious(): List<Pair<T?, T>> {
        val results = mutableListOf<Pair<T?, T>>()
        for (index in 0 until size) {
            val previous = getOrNull(index - 1)
            val current = get(index)
            results += Pair(previous, current)
        }
        return results
    }
}

package com.alamkanak.weekview

internal class EventChipsProvider<T>(
        private val config: WeekViewConfigWrapper,
        private val cache: WeekViewCache<T>,
        private val viewState: WeekViewViewState
) {

    var weekViewLoader: WeekViewLoader<T>? = null

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

        val loader = checkNotNull(weekViewLoader) { "No WeekViewLoader or MonthChangeListener provided." }

        if (previousPeriodEvents == null) {
            previousPeriodEvents = loader.onLoad(fetchPeriods.previous)
        }

        if (currentPeriodEvents == null) {
            currentPeriodEvents = loader.onLoad(fetchPeriods.current)
        }

        if (nextPeriodEvents == null) {
            nextPeriodEvents = loader.onLoad(fetchPeriods.next)
        }

        cache.allEventChips.clear()
        cache.sortAndCacheEvents(previousPeriodEvents)
        cache.sortAndCacheEvents(currentPeriodEvents)
        cache.sortAndCacheEvents(nextPeriodEvents)

        cache.previousPeriodEvents = previousPeriodEvents
        cache.currentPeriodEvents = currentPeriodEvents
        cache.nextPeriodEvents = nextPeriodEvents
        cache.fetchedPeriods = fetchPeriods
    }

    private fun calculateEventChipPositions() {
        val results = mutableListOf<EventChip<T>>()

        cache.allEventChips
                .groupBy { it.event.startDateTime.toLocalDate() }
                .values
                .forEach { eventChips ->
                    computePositionOfEvents(eventChips)
                    results += eventChips
                }

        cache.put(results)
    }

    private fun computePositionOfEvents(eventChips: List<EventChip<T>>) {
        // Make "collision groups" for all events that collide with others.
        val collisionGroups = mutableListOf<MutableList<EventChip<T>>>()
        for (eventChip in eventChips) {
            var isPlaced = false

            outerLoop@ for (collisionGroup in collisionGroups) {
                for (groupEvent in collisionGroup) {
                    if (groupEvent.event.collidesWith(eventChip.event)
                            && groupEvent.event.isAllDay == eventChip.event.isAllDay) {
                        collisionGroup.add(eventChip)
                        isPlaced = true
                        break@outerLoop
                    }
                }
            }

            if (!isPlaced) {
                collisionGroups += mutableListOf(eventChip)
            }
        }

        for (collisionGroup in collisionGroups) {
            expandEventsToMaxWidth(collisionGroup)
        }
    }

    private fun expandEventsToMaxWidth(collisionGroup: List<EventChip<T>>) {
        // Expand the events to maximum possible width.
        val columns = mutableListOf<MutableList<EventChip<T>>>()
        columns.add(mutableListOf())

        for (eventChip in collisionGroup) {
            var isPlaced = false

            for (column in columns) {
                if (column.isEmpty()) {
                    column.add(eventChip)
                    isPlaced = true
                } else if (!eventChip.event.collidesWith(column[column.size - 1].event)) {
                    column.add(eventChip)
                    isPlaced = true
                    break
                }
            }

            if (!isPlaced) {
                columns += mutableListOf(eventChip)
            }
        }

        // Calculate left and right position for all the events.
        // Get the maxRowCount by looking in all columns.
        var maxRowCount = 0
        for (column in columns) {
            maxRowCount = Math.max(maxRowCount, column.size)
        }

        for (i in 0 until maxRowCount) {
            // Set the left and right values of the event.
            var j = 0f
            for (column in columns) {
                if (column.size >= i + 1) {
                    val eventChip = column[i]
                    eventChip.width = 1f / columns.size
                    eventChip.left = j / columns.size

                    if (!eventChip.event.isAllDay) {
                        eventChip.top = eventChip.event.getEffectiveStartMinutes(config).toFloat()
                        eventChip.bottom = eventChip.event.getEffectiveEndMinutes(config).toFloat()
                    } else {
                        eventChip.top = 0f
                        eventChip.bottom = 100f
                    }
                }
                j++
            }
        }
    }

}

package com.alamkanak.weekview

internal class EventChipsProvider<T>(
    private val cache: EventCache<T>,
    private val viewState: WeekViewViewState,
    private val eventSplitter: WeekViewEventSplitter<T>,
    private val chipCache: EventChipCache<T>
) {

    var shouldRefreshEvents: Boolean = false
    var monthLoader: MonthLoader<T>? = null

    fun loadEventsIfNecessary() {
        val hasNoEvents = cache.hasEvents.not()

        val firstVisibleDay = checkNotNull(viewState.firstVisibleDate)
        val fetchPeriods = FetchRange.create(firstVisibleDay)

        if (hasNoEvents || shouldRefreshEvents || !cache.covers(fetchPeriods)) {
            loadEventsAndCalculateEventChipPositions(fetchPeriods)
            shouldRefreshEvents = false
        }
    }

    private fun loadEventsAndCalculateEventChipPositions(fetchRange: FetchRange) {
        if (shouldRefreshEvents) {
            cache.clear()
        }

        loadEvents(fetchRange)
    }

    private fun loadEvents(fetchRange: FetchRange) {
        val oldFetchPeriods = cache.fetchedRange ?: fetchRange
        val newCurrentPeriod = fetchRange.current

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

        val loader = checkNotNull(monthLoader) {
            "No OnMonthChangeListener found. Provide one via weekView.setOnMonthChangeListener()."
        }

        if (previousPeriodEvents == null) {
            previousPeriodEvents = loader.load(fetchRange.previous)
        }

        if (currentPeriodEvents == null) {
            currentPeriodEvents = loader.load(fetchRange.current)
        }

        if (nextPeriodEvents == null) {
            nextPeriodEvents = loader.load(fetchRange.next)
        }

        cache.update(previousPeriodEvents, currentPeriodEvents, nextPeriodEvents, fetchRange)
        createAndCacheEventChips(previousPeriodEvents, currentPeriodEvents, nextPeriodEvents)
    }

    private fun createAndCacheEventChips(
        previousPeriodEvents: List<WeekViewEvent<T>>,
        currentPeriodEvents: List<WeekViewEvent<T>>,
        nextPeriodEvents: List<WeekViewEvent<T>>
    ) {
        chipCache += convertEventsToEventChips(previousPeriodEvents)
        chipCache += convertEventsToEventChips(currentPeriodEvents)
        chipCache += convertEventsToEventChips(nextPeriodEvents)
    }

    private fun convertEventsToEventChips(
        events: List<WeekViewEvent<T>>
    ): List<EventChip<T>> = events.sorted().map(this::convertEventToEventChips).flatten()

    private fun convertEventToEventChips(
        event: WeekViewEvent<T>
    ): List<EventChip<T>> {
        if (event.startTime >= event.endTime) {
            return emptyList()
        }
        return eventSplitter.split(event).map { EventChip(it, event, null) }
    }
}

package com.alamkanak.weekview

internal class EventChipsProvider<T>(
    private val cache: EventCache<T>,
    private val viewState: WeekViewViewState
) {

    var monthLoader: MonthLoader<T>? = null

    fun loadEventsIfNecessary() {
        val hasNoEvents = cache.hasEvents.not()
        val shouldRefresh = viewState.shouldRefreshEvents

        val firstVisibleDay = checkNotNull(viewState.firstVisibleDate)
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

        val loader = checkNotNull(monthLoader) {
            "No OnMonthChangeListener found. Provide one via weekView.setOnMonthChangeListener()."
        }

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

}

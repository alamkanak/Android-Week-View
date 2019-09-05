package com.alamkanak.weekview

internal class HeaderRowHeightUpdater<T>(
    private val config: WeekViewConfigWrapper,
    private val eventsCacheWrapper: EventsCacheWrapper<T>
) : Updater {

    private var previousHorizontalOrigin: Float? = null
    private val previousAllDayEventIds = mutableSetOf<Long>()

    private val eventsCache: EventsCache<T>
        get() = eventsCacheWrapper.get()

    override fun isRequired(drawingContext: DrawingContext): Boolean {
        val didScrollHorizontally = previousHorizontalOrigin != config.currentOrigin.x
        val currentTimeColumnWidth = config.timeTextWidth + config.timeColumnPadding * 2
        val didTimeColumnChange = currentTimeColumnWidth != config.timeColumnWidth
        val allDayEvents = eventsCache[drawingContext.dateRange]
            .filter { it.isAllDay }
            .map { it.id }
            .toSet()
        val didEventsChange = allDayEvents.hashCode() != previousAllDayEventIds.hashCode()

        return (didScrollHorizontally || didTimeColumnChange || didEventsChange).also {
            previousAllDayEventIds.clear()
            previousAllDayEventIds += allDayEvents
        }
    }

    override fun update(drawingContext: DrawingContext) {
        previousHorizontalOrigin = config.currentOrigin.x
        config.timeColumnWidth = config.timeTextWidth + config.timeColumnPadding * 2
        refreshHeaderHeight(drawingContext)
    }

    private fun refreshHeaderHeight(drawingContext: DrawingContext) {
        val dateRange = drawingContext.dateRange
        val visibleEvents = eventsCache[dateRange].filter { it.isAllDay }
        config.hasEventInHeader = visibleEvents.isNotEmpty()
        config.refreshHeaderHeight()
    }
}

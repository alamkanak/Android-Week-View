package com.alamkanak.weekview

internal class HeaderRowCalculator<T>(
    private val config: WeekViewConfigWrapper,
    private val cache: WeekViewCache<T>
) {

    fun update(drawingContext: DrawingContext) {
        config.timeColumnWidth = config.timeTextWidth + config.timeColumnPadding * 2
        refreshHeaderHeight(drawingContext)
    }

    private fun refreshHeaderHeight(drawingContext: DrawingContext) {
        if (cache.allDayEventChips.isEmpty()) {
            config.hasEventInHeader = false
            config.refreshHeaderHeight()
        }

        val dateRange = drawingContext.dateRangeWithStartPixels.map { it.first }
        val visibleEvents = cache.getAllDayEventsInRange(dateRange)

        config.hasEventInHeader = visibleEvents.any { it.isAllDay }
        config.refreshHeaderHeight()
    }

}

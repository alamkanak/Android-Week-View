package com.alamkanak.weekview

internal class HeaderRowHeightUpdater<T>(
    private val config: WeekViewConfigWrapper,
    private val cache: EventCache<T>
) : Updater {

    private var previousHorizontalOrigin: Float? = null

    override fun isRequired(): Boolean {
        val currentTimeColumnWidth = config.timeTextWidth + config.timeColumnPadding * 2
        val didTimeColumnChange = currentTimeColumnWidth != config.timeColumnWidth
        val didScrollHorizontally = previousHorizontalOrigin != config.currentOrigin.x
        return didTimeColumnChange || didScrollHorizontally
    }

    override fun update(drawingContext: DrawingContext) {
        previousHorizontalOrigin = config.currentOrigin.x
        config.timeColumnWidth = config.timeTextWidth + config.timeColumnPadding * 2
        refreshHeaderHeight(drawingContext)
    }

    private fun refreshHeaderHeight(drawingContext: DrawingContext) {
        if (cache.allDayEventChips.isEmpty()) {
            config.hasEventInHeader = false
            config.refreshHeaderHeight()
            return
        }

        val dateRange = drawingContext.dateRangeWithStartPixels.map { it.first }
        val visibleEvents = cache.getAllDayEventsInRange(dateRange)

        config.hasEventInHeader = visibleEvents.isNotEmpty()
        config.refreshHeaderHeight()
    }

}

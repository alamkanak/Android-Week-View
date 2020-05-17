package com.alamkanak.weekview

import android.graphics.RectF
import java.util.Calendar

internal class SingleEventsUpdater<T : Any>(
    private val view: WeekView<T>,
    private val config: WeekViewConfigWrapper,
    private val chipsCache: EventChipsCache<T>
) : Updater {

    private val boundsCalculator = EventChipBoundsCalculator<T>(config)

    override fun isRequired(drawingContext: DrawingContext) = true

    override fun update(drawingContext: DrawingContext) {
        chipsCache.clearSingleEventsCache()

        drawingContext
            .dateRangeWithStartPixels
            .forEach { (date, startPixel) ->
                // If we use a horizontal margin in the day view, we need to offset the start pixel.
                val modifiedStartPixel = when {
                    config.isSingleDay -> startPixel + config.eventMarginHorizontal.toFloat()
                    else -> startPixel
                }
                calculateRectsForEventsOnDate(date, modifiedStartPixel)
            }
    }

    private fun calculateRectsForEventsOnDate(
        date: Calendar,
        startPixel: Float
    ) {
        chipsCache.normalEventChipsByDate(date)
            .filter { it.event.isNotAllDay && it.event.isWithin(config.minHour, config.maxHour) }
            .forEach {
                val chipRect = boundsCalculator.calculateSingleEvent(it, startPixel)
                if (chipRect.isValidSingleEventRect) {
                    it.bounds = chipRect
                } else {
                    it.bounds = null
                }
            }
    }

    private val RectF.isValidSingleEventRect: Boolean
        get() {
            val hasCorrectWidth = left < right && left < view.width
            val hasCorrectHeight = top < view.height
            val isNotHiddenByChrome = right > config.timeColumnWidth && bottom > config.headerHeight
            return hasCorrectWidth && hasCorrectHeight && isNotHiddenByChrome
        }
}

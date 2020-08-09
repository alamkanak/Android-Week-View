package com.alamkanak.weekview

import android.graphics.RectF
import java.util.Calendar

internal class SingleEventsUpdater(
    private val viewState: ViewState,
    private val chipsCache: EventChipsCache
) : Updater {

    private val boundsCalculator = EventChipBoundsCalculator(viewState)

    override fun isRequired() = true

    override fun update() {
        chipsCache.clearSingleEventsCache()

        viewState
            .dateRangeWithStartPixels
            .forEach { (date, startPixel) ->
                // If we use a horizontal margin in the day view, we need to offset the start pixel.
                val modifiedStartPixel = when {
                    viewState.isSingleDay -> startPixel + viewState.eventMarginHorizontal.toFloat()
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
            .filter { it.event.isNotAllDay && it.event.isWithin(viewState.minHour, viewState.maxHour) }
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
            val hasCorrectWidth = left < right && left < viewState.viewWidth
            val hasCorrectHeight = top < viewState.viewHeight
            val isNotHiddenByChrome = right > viewState.timeColumnWidth && bottom > viewState.headerHeight
            return hasCorrectWidth && hasCorrectHeight && isNotHiddenByChrome
        }
}

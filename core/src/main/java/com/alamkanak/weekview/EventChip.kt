package com.alamkanak.weekview

import android.graphics.RectF
import android.view.MotionEvent

/**
 * This class encapsulates a [WeekViewEvent] and its visual representation, a [RectF]
 * which is eventually drawn to the screen.
 *
 * There may be more than one [EventChip] for any even (think multi-day events). In that case,
 * multiple [EventChip]s will be used for a single [WeekViewEvent].
 *
 * The original [WeekViewEvent] is accessible via [originalEvent]. The [WeekViewEvent] that
 * corresponds to the drawn rectangle is accessible via [event].
 */
internal data class EventChip<T>(
    /**
     * The [WeekViewEvent] corresponding to the drawn rectangle. It might differ from
     * [originalEvent], which may be a multi-day event.
     */
    val event: WeekViewEvent<T>,
    /**
     * The original [WeekViewEvent], which may be a multi-day event.
     */
    val originalEvent: WeekViewEvent<T>,
    /**
     * The rectangle in which the [WeekViewEvent] will be drawn.
     */
    var rect: RectF?
) {

    var left = 0f
    var width = 0f
    var top = 0f
    var bottom = 0f

    private var availableWidthCache: Int = 0
    private var availableHeightCache: Int = 0

    fun didAvailableAreaChange(area: RectF, eventPadding: Int): Boolean {
        val availableWidth = (area.right - area.left - (eventPadding * 2f)).toInt()
        val availableHeight = (area.bottom - area.top - (eventPadding * 2f)).toInt()
        return availableWidth != availableWidthCache || availableHeight != availableHeightCache
    }

    fun updateAvailableArea(width: Int, height: Int) {
        availableWidthCache = width
        availableHeightCache = height
    }

    fun clearCache() {
        rect = null
        availableWidthCache = 0
        availableHeightCache = 0
    }

    fun calculateTopAndBottom(config: WeekViewConfigWrapper) {
        if (event.isNotAllDay) {
            top = event.getEffectiveStartMinutes(config).toFloat()
            bottom = event.getEffectiveEndMinutes(config).toFloat()
        } else {
            top = 0f
            bottom = 100f // TODO
        }
    }

    fun isHit(e: MotionEvent): Boolean {
        return rect?.let {
            e.x > it.left && e.x < it.right && e.y > it.top && e.y < it.bottom
        } ?: false
    }

}

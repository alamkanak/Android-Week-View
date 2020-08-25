package com.alamkanak.weekview

import android.graphics.RectF

/**
 * This class encapsulates a [ResolvedWeekViewEvent] and its visual representation, a [RectF]
 * which is eventually drawn to the screen.
 *
 * There may be more than one [EventChip] for any even (think multi-day events). In that case,
 * multiple [EventChip]s will be used for a single [ResolvedWeekViewEvent].
 *
 * The original [ResolvedWeekViewEvent] is accessible via [originalEvent]. The
 * [ResolvedWeekViewEvent] that corresponds to the drawn rectangle is accessible via [event].
 */
internal data class EventChip(
    /**
     * The [ResolvedWeekViewEvent] corresponding to the drawn rectangle. It might differ from
     * [originalEvent], which may be a multi-day event.
     */
    val event: ResolvedWeekViewEvent<*>,
    /**
     * The original [ResolvedWeekViewEvent], which may be a multi-day event.
     */
    val originalEvent: ResolvedWeekViewEvent<*>
) {

    /**
     * A unique ID of this [EventChip].
     */
    val id: String
        get() = "$eventId-${this.event.startTime.timeInMillis}"

    /**
     * The ID of the [ResolvedWeekViewEvent] that this [EventChip] represents.
     */
    val eventId: Long
        get() = originalEvent.id

    /**
     * The rectangle in which the [ResolvedWeekViewEvent] will be drawn.
     */
    var bounds: RectF = RectF()

    /**
     * The relative start position of the [EventChip].
     *
     * For instance, if there are four columns of events, possible values are 0.0, 0.25, 0.5 and
     * 0.75.
     */
    var relativeStart: Float = 0f

    /**
     * The relative width of the [EventChip].
     *
     * For instance, if there are four columns of events, possible values are:
     * - 0.25: spanning a single column
     * - 0.50: spanning two columns
     * - 0.75: spanning three columns
     * - 1.00: spanning all four columns
     */
    var relativeWidth: Float = 0f

    var minutesFromStartHour: Int = 0

    private var availableWidthCache: Int = 0
    private var availableHeightCache: Int = 0

    fun didAvailableAreaChange(
        area: RectF,
        horizontalPadding: Int,
        verticalPadding: Int
    ): Boolean {
        val availableWidth = (area.width() - horizontalPadding).toInt()
        val availableHeight = (area.height() - verticalPadding).toInt()
        return availableWidth != availableWidthCache || availableHeight != availableHeightCache
    }

    fun updateAvailableArea(width: Int, height: Int) {
        availableWidthCache = width
        availableHeightCache = height
    }

    fun clearCache() {
        bounds.setEmpty()
        availableWidthCache = 0
        availableHeightCache = 0
    }

    fun isHit(x: Float, y: Float): Boolean {
        return x > bounds.left && x < bounds.right && y > bounds.top && y < bounds.bottom
    }
}

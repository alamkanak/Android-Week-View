package com.alamkanak.weekview

import android.graphics.RectF

/**
 * This class encapsulates a [ResolvedWeekViewEntity] and its visual representation, a [RectF] which
 * is eventually drawn to the screen.
 *
 * There may be more than one [EventChip] for any even (think multi-day events). In that case,
 * multiple [EventChip]s will be used for a single [ResolvedWeekViewEntity].
 */
internal data class EventChip(
    /**
     * The [ResolvedWeekViewEntity] corresponding to the drawn rectangle. It might differ from
     * [originalEvent], which may be a multi-day event.
     */
    val event: ResolvedWeekViewEntity,
    /**
     * The original [ResolvedWeekViewEntity], which may be a multi-day event.
     */
    val originalEvent: ResolvedWeekViewEntity
) {

    /**
     * A unique ID of this [EventChip].
     */
    val id: String
        get() = "${event.id}-${this.event.startTime.timeInMillis}"

    /**
     * The bounds in which the [event] will be drawn.
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

    /**
     * Returns whether the [EventChip] of an all-day event is currently hidden, if all-day events
     * are arranged vertically.
     */
    var isHidden: Boolean = false

    var minutesFromStartHour: Int = 0

    fun setEmpty() {
        bounds.setEmpty()
        widthCache = 0f
        heightCache = 0f
    }

    fun isHit(x: Float, y: Float): Boolean {
        return x > bounds.left && x < bounds.right && y > bounds.top && y < bounds.bottom
    }

    private var widthCache: Float = 0f
    private var heightCache: Float = 0f

    fun didAvailableAreaChange(
        availableWidth: Float,
        availableHeight: Float
    ): Boolean = availableWidth != widthCache || availableHeight != heightCache

    fun updateAvailableArea(width: Float, height: Float) {
        widthCache = width
        heightCache = height
    }
}

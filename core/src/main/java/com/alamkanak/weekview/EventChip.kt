package com.alamkanak.weekview

import android.graphics.RectF
import java.util.Calendar

/**
 * This class encapsulates a [ResolvedWeekViewEntity] and its visual representation, a [RectF] which
 * is drawn to the screen. There may be more than one [EventChip] for any [ResolvedWeekViewEntity],
 * for instance in the case of multi-day events.
 */
internal data class EventChip(
    val event: ResolvedWeekViewEntity,
    val index: Int,
    val startTime: Calendar,
    val endTime: Calendar,
) {

    /**
     * A unique ID of this [EventChip].
     */
    val id: String = "${event.id}-$index"

    /**
     * The ID of this [EventChip]'s [ResolvedWeekViewEntity].
     */
    val eventId: Long = event.id

    /**
     * The bounds in which [EventChip] will be drawn.
     */
    var bounds: RectF = RectF()

    val durationInMinutes: Int by lazy {
        (endTime minutesUntil startTime).minutes
    }

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
     * Returns whether the [EventChip] of an all-day event is currently hidden. This can happen when
     * all-day events are arranged vertically.
     */
    var isHidden: Boolean = false

    var minutesFromStartHour: Int = 0

    val startsOnEarlierDay: Boolean
        get() = event.startTime < startTime

    val endsOnLaterDay: Boolean
        get() = event.endTime > endTime

    fun setEmpty() {
        bounds.setEmpty()
        widthCache = 0
        heightCache = 0
    }

    fun isHit(x: Float, y: Float): Boolean {
        return x > bounds.left && x < bounds.right && y > bounds.top && y < bounds.bottom
    }

    private var widthCache: Int = 0
    private var heightCache: Int = 0

    fun didAvailableAreaChange(
        availableWidth: Int,
        availableHeight: Int
    ): Boolean = availableWidth != widthCache || availableHeight != heightCache

    fun updateAvailableArea(width: Int, height: Int) {
        widthCache = width
        heightCache = height
    }
}

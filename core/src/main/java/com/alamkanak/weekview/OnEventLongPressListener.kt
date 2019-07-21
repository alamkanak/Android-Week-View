package com.alamkanak.weekview

import android.graphics.RectF

@Deprecated(
    "Use OnEventLongPressListener",
    ReplaceWith("OnEventLongPressListener")
)
typealias EventLongPressListener<T> = OnEventLongPressListener<T>

@FunctionalInterface
interface OnEventLongPressListener<T> {

    /**
     * Called when an [EventChip] is long-clicked.
     *
     * @param data The [T] object associated with the [EventChip]'s [WeekViewEvent]
     * @param eventRect The [RectF] of the [EventChip]
     */
    fun onEventLongPress(data: T, eventRect: RectF)
}

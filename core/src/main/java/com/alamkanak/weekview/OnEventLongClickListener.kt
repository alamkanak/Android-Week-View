package com.alamkanak.weekview

import android.graphics.RectF

@Deprecated(
    "Use OnEventLongClickListener",
    ReplaceWith("OnEventLongClickListener")
)
typealias EventLongPressListener<T> = OnEventLongClickListener<T>

@Deprecated(
    "Use OnEventLongClickListener",
    ReplaceWith("OnEventLongClickListener")
)
typealias OnEventLongPressListener<T> = OnEventLongClickListener<T>

@FunctionalInterface
interface OnEventLongClickListener<T> {

    /**
     * Called when an [EventChip] is long-clicked.
     *
     * @param data The [T] object associated with the [EventChip]'s [WeekViewEvent]
     * @param eventRect The [RectF] of the [EventChip]
     */
    fun onEventLongClick(data: T, eventRect: RectF)
}

@Deprecated(
    "Use onEventLongClick",
    ReplaceWith("onEventLongClick(data, eventRect)")
)
fun <T> OnEventLongClickListener<T>.onEventLongPress(
    data: T,
    eventRect: RectF
) = onEventLongClick(data, eventRect)

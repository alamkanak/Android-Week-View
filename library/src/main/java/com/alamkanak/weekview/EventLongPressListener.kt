package com.alamkanak.weekview

import android.graphics.RectF

@FunctionalInterface
interface EventLongPressListener<T> {

    /**
     * Called when an [EventChip] is long-clicked.
     *
     * @param data The [T] object associated with the [EventChip]'s [WeekViewEvent]
     * @param eventRect The [RectF] of the [EventChip]
     */
    fun onEventLongPress(data: T, eventRect: RectF)

}

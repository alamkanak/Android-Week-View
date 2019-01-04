package com.alamkanak.weekview

import android.graphics.RectF

@FunctionalInterface
interface EventClickListener<T> {

    /**
     * Called when an [EventChip] is clicked.
     *
     * @param data The [T] object associated with the [EventChip]'s [WeekViewEvent]
     * @param eventRect The [RectF] of the [EventChip]
     */
    fun onEventClick(data: T, eventRect: RectF)

}

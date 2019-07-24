package com.alamkanak.weekview

import java.util.Calendar

@Deprecated(
    "Use OnEmptyViewLongClickListener",
    ReplaceWith("OnEmptyViewLongClickListener")
)
typealias EmptyViewLongPressListener = OnEmptyViewLongClickListener

@Deprecated(
    "Use OnEmptyViewLongClickListener",
    ReplaceWith("OnEmptyViewLongClickListener")
)
typealias OnEmptyViewLongPressListener = OnEmptyViewLongClickListener

@FunctionalInterface
interface OnEmptyViewLongClickListener {

    /**
     * Called when an empty area of [WeekView] is long-clicked.
     *
     * @param time A [Calendar] with the date and time of the clicked position
     */
    fun onEmptyViewLongClick(time: Calendar)
}

@Deprecated(
    "Use onEmptyViewLongClick",
    ReplaceWith("onEmptyViewLongClick(time)")
)
fun OnEmptyViewLongClickListener.onEmptyViewLongPress(
    time: Calendar
) = onEmptyViewLongClick(time)

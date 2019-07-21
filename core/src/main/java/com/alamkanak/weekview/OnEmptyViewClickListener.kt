package com.alamkanak.weekview

import java.util.Calendar

@Deprecated(
    "Use OnEmptyViewClickListener",
    ReplaceWith("OnEmptyViewClickListener")
)
typealias EmptyViewClickListener = OnEmptyViewClickListener

@FunctionalInterface
interface OnEmptyViewClickListener {

    /**
     * Called when an empty area of [WeekView] is clicked.
     *
     * @param time A [Calendar] with the date and time of the clicked position
     */
    fun onEmptyViewClicked(time: Calendar)
}

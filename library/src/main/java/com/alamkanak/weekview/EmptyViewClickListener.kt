package com.alamkanak.weekview

import java.util.*

@FunctionalInterface
interface EmptyViewClickListener {

    /**
     * Called when an empty area of [WeekView] is clicked.
     *
     * @param time A [Calendar] with the date and time of the clicked position
     */
    fun onEmptyViewClicked(time: Calendar)

}

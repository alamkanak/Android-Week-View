package com.alamkanak.weekview

import java.util.Calendar

@FunctionalInterface
interface OnRangeChangeListener {

    /**
     * Called when the range of visible days changes due to scrolling.
     *
     * @param firstVisibleDate The first visible day
     * @param lastVisibleDate The last visible day
     */
    fun onRangeChanged(firstVisibleDate: Calendar, lastVisibleDate: Calendar)

}

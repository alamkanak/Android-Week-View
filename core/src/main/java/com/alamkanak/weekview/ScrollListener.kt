package com.alamkanak.weekview

import java.util.Calendar

@FunctionalInterface
interface ScrollListener {

    /**
     * Called when the first visible day has changed.
     *
     * @param newFirstVisibleDay The new first visible day
     * @param oldFirstVisibleDay The old first visible day
     */
    fun onFirstVisibleDayChanged(newFirstVisibleDay: Calendar?, oldFirstVisibleDay: Calendar?)

}

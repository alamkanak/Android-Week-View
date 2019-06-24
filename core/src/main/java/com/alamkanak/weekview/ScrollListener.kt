package com.alamkanak.weekview

import java.util.*

@FunctionalInterface
interface ScrollListener {

    /**
     * Called when the first visible day has changed.
     *
     * @param newFirstVisibleDay The new first visible day
     * @param oldFirstVisibleDay The old first visible day (is null on the first call).
     */
    fun onFirstVisibleDayChanged(newFirstVisibleDay: Calendar, oldFirstVisibleDay: Calendar?)

}

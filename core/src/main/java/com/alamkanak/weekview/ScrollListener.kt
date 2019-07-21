package com.alamkanak.weekview

import java.util.Calendar

@FunctionalInterface
interface ScrollListener {

    /**
     * Called when the first visible date has changed.
     *
     * @param date The new first visible date
     */
    fun onFirstVisibleDateChanged(date: Calendar)
}

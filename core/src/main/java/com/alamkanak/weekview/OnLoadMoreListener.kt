package com.alamkanak.weekview

import java.util.Calendar

@FunctionalInterface
interface OnLoadMoreListener {

    /**
     * Called when the month displayed in [WeekView] changes.
     * @param startDate A [Calendar] representing the start date of the month
     * @param endDate A [Calendar] representing the end date of the month
     */
    fun onLoadMore(startDate: Calendar, endDate: Calendar)
}
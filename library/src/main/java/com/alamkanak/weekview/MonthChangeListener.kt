package com.alamkanak.weekview

import java.util.*

@FunctionalInterface
interface MonthChangeListener<T> {

    /**
     * Called when the month displayed in the [WeekView] changes.
     * @param startDate A [Calendar] representing the start date of the month
     * @param endDate A [Calendar] representing the end date of the month
     * @return The list of [WeekViewDisplayable] of the provided month
     */
    fun onMonthChange(startDate: Calendar, endDate: Calendar): List<WeekViewDisplayable<T>>

}

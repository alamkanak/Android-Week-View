package com.alamkanak.weekview

import java.util.*

interface WeekViewLoader<T> {

    /**
     * Convert a date into a double that will be used to reference when you're loading data.
     *
     * All periods that have the same integer part, define one period. Dates that are later in time
     * should have a greater return value.
     *
     * @param instance the date
     * @return The period index in which the date falls (floating point number).
     */
    @Deprecated("Use WeekViewLoader.toPeriod(Calendar)")
    fun toWeekViewPeriodIndex(instance: Calendar): Double {
        return 0.0
    }

    /**
     * Converts a date into a [Period] that will be used to reference when you're loading data.
     *
     *
     * @param instance The date
     * @return The [Period] in which the date falls
     */
    fun toPeriod(instance: Calendar): Period

    /**
     * Load the events within the period
     * @param periodIndex the period to load
     * @return A list with the events of this period
     */
    @Deprecated("Use WeekViewLoader.onLoad(Period): List<WeekViewEvent<T>>")
    fun onLoad(periodIndex: Int): List<WeekViewEvent<T>> {
        return emptyList()
    }

    /**
     * Loads the events within the specified period
     * @param period The [Period] to load
     * @return A list with the [WeekViewEvent]s of this [Period]
     */
    fun onLoad(period: Period): List<WeekViewEvent<T>>

}

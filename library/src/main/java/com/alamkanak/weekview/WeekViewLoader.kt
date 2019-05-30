package com.alamkanak.weekview

import java.util.*

interface WeekViewLoader<T> {

    /**
     * Converts a date into a [Period] that will be used for reference when you're loading data.
     *
     * @param instance The date
     * @return The [Period] in which the date falls
     */
    fun toPeriod(instance: Calendar): Period

    /**
     * Loads the events within the specified period
     *
     * @param period The [Period] to load
     * @return A list with the [WeekViewEvent]s of this [Period]
     */
    fun onLoad(period: Period): List<WeekViewEvent<T>>

}

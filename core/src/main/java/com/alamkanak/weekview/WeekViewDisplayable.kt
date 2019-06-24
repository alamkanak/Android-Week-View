package com.alamkanak.weekview

/**
 * This interface can be implemented by classes that should be displayed in [WeekView].
 * Instead of having to provide a list of [WeekViewEvent]s, you can provide a list of elements
 * of your class. The conversion to [WeekViewEvent] will happen in the background.
 */
interface WeekViewDisplayable<T> {

    /**
     * Returns a [WeekViewEvent] for use in [WeekView].
     *
     * @return A [WeekViewEvent]
     */
    fun toWeekViewEvent(): WeekViewEvent<T>

}

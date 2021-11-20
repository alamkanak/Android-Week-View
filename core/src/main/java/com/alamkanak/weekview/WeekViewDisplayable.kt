package com.alamkanak.weekview

/**
 * This interface must be implemented by classes that should be displayed in [WeekView].
 */
@Deprecated(
    message = "This interface has been deprecated. Instead, construct WeekViewEntity objects in the onCreateEntity() method of WeekView's adapter."
)
interface WeekViewDisplayable<T> {

    /**
     * Returns a [WeekViewEvent] for use in [WeekView].
     */
    @Deprecated(
        message = "This method is deprecated. Instead, construct a WeekViewEntity in the onCreateEntity() method of WeekView's adapter."
    )
    fun toWeekViewEvent(): WeekViewEvent<T>
}

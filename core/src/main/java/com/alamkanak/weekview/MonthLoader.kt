package com.alamkanak.weekview

/**
 * This class is responsible for loading [WeekViewEvent]s into [WeekView]. It can handle
 * both concrete [WeekViewEvent] objects and [WeekViewDisplayable] objects. The latter is
 * an interface that can be implemented in one's actual data class and handles the conversion to a
 * [WeekViewEvent].
 */
internal class MonthLoader<T>(
    var listener: OnMonthChangeListener<T>?
) {

    fun load(period: Period): List<WeekViewEvent<T>> {
        val listener = checkNotNull(listener) { "No OnMonthChangeListener found. " +
            "Provide one via weekView.setOnMonthChangeListener()." }

        val startDate = today()
            .withYear(period.year)
            .withMonth(period.month)
            .withDayOfMonth(1)

        val maxDays = startDate.lengthOfMonth
        val endDate = startDate
            .withDayOfMonth(maxDays)
            .atEndOfDay

        return listener
            .onMonthChange(startDate, endDate)
            .map { it.toWeekViewEvent() }
    }
}

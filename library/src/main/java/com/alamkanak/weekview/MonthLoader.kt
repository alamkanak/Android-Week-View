package com.alamkanak.weekview

import java.util.*

/**
 * This class is responsible for loading [WeekViewEvent]s into [WeekView]. It can handle
 * both concrete [WeekViewEvent] objects and [WeekViewDisplayable] objects. The latter is
 * an interface that can be implemented in one's actual data class and handles the conversion to a
 * [WeekViewEvent].
 */
internal class MonthLoader<T>(
        var onMonthChangeListener: MonthChangeListener<T>?
) : WeekViewLoader<T> {

    override fun toPeriod(instance: Calendar): Period {
        return Period.fromDate(instance.toLocalDate())
    }

    override fun onLoad(period: Period): List<WeekViewEvent<T>> {
        val (month, year) = period

        val startDate = today()
                .withYear(year)
                .withMonth(month)
                .withDayOfMonth(1)
        val maxDays = startDate.lengthOfMonth()
        val endDate = startDate.withDayOfMonth(maxDays)

        val listener = onMonthChangeListener ?: return emptyList()
        return listener
                .onMonthChange(startDate.toCalendar(), endDate.toCalendar())
                .map { it.toWeekViewEvent() }
    }

}

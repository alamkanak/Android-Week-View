package com.alamkanak.weekview

import com.alamkanak.weekview.DateUtils.today
import java.util.*

/**
 * This class is responsible for loading [WeekViewEvent]s into [WeekView]. It can handle
 * both concrete [WeekViewEvent] objects and [WeekViewDisplayable] objects. The latter is
 * an interface that can be implemented in one's actual data class and handles the conversion to a
 * [WeekViewEvent].
 */
private class MonthLoader<T>(
        var onMonthChangeListener: MonthChangeListener<T>?
) : WeekViewLoader<T> {

    override fun toPeriod(instance: Calendar): Period {
        val month = instance.get(Calendar.MONTH)
        val year = instance.get(Calendar.YEAR)
        return Period(month, year)
    }

    override fun onLoad(period: Period): List<WeekViewEvent<T>> {
        val (month, year) = period

        val startDate = today().withTimeAtStartOfDay().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val maxDays = startDate.getActualMaximum(Calendar.DAY_OF_MONTH)

        val endDate = today().withTimeAtEndOfDay().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, maxDays)
        }

        val listener = onMonthChangeListener ?: return emptyList()
        return listener.onMonthChange(startDate, endDate).map { it.toWeekViewEvent() }
    }

}

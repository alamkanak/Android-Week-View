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

    override fun toWeekViewPeriodIndex(instance: Calendar): Double {
        return ((instance.get(Calendar.YEAR) * 12).toDouble()
                + instance.get(Calendar.MONTH).toDouble()
                + (instance.get(Calendar.DAY_OF_MONTH) - 1) / 30.0)
    }

    override fun onLoad(periodIndex: Int): List<WeekViewEvent<T>> {
        val year = periodIndex / 12
        val month = periodIndex % 12

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

        return onMonthChangeListener?.onMonthChange(startDate, endDate).orEmpty()
                .map { it.toWeekViewEvent() }
    }

}

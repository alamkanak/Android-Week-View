package com.alamkanak.weekview

import java.util.Calendar

internal data class FetchRange(
    val previous: Period,
    val current: Period,
    val next: Period
) {

    val periods: List<Period> = listOf(previous, current, next)

    fun isEqual(other: FetchRange) = this == other

    internal companion object {

        fun create(firstVisibleDay: Calendar): FetchRange {
            val current = Period.fromDate(firstVisibleDay)
            return FetchRange(current.previous, current, current.next)
        }
    }
}

internal data class Period(
    val month: Int,
    val year: Int
) : Comparable<Period> {

    val previous: Period
        get() {
            val year = if (month == Calendar.JANUARY) year - 1 else year
            val month = if (month == Calendar.JANUARY) Calendar.DECEMBER else month - 1
            return Period(month, year)
        }

    val next: Period
        get() {
            val year = if (month == Calendar.DECEMBER) year + 1 else year
            val month = if (month == Calendar.DECEMBER) Calendar.JANUARY else month + 1
            return Period(month, year)
        }

    val startDate: Calendar = newDate(year, month, dayOfMonth = 1)
    val endDate: Calendar = startDate.withDayOfMonth(startDate.lengthOfMonth).atEndOfDay

    override fun compareTo(other: Period): Int {
        return when {
            year < other.year -> -1
            year > other.year -> 1
            else -> month.compareTo(other.month)
        }
    }

    internal companion object {
        fun fromDate(date: Calendar): Period = Period(month = date.month, year = date.year)
    }
}

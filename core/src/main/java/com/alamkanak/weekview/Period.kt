package com.alamkanak.weekview

import java.util.Calendar

internal data class FetchRange(
    val previous: Period,
    val current: Period,
    val next: Period
) {

    internal companion object {

        fun create(firstVisibleDay: Calendar): FetchRange {
            val current = Period.fromDate(firstVisibleDay)
            return FetchRange(current.previous, current, current.next)
        }

    }

}

internal data class Period(val month: Int, val year: Int) {

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

    internal companion object {

        fun fromDate(date: Calendar): Period {
            val month = date.month
            val year = date.year
            return Period(month, year)
        }

    }

}

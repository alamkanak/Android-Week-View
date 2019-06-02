package com.alamkanak.weekview

import java.util.Calendar

internal data class FetchPeriods(
    val previous: Period,
    val current: Period,
    val next: Period
) {

    internal companion object {

        fun create(firstVisibleDay: Calendar): FetchPeriods {
            val current = Period.fromDate(firstVisibleDay)
            return FetchPeriods(current.previous, current, current.next)
        }

    }

}

internal data class Period(val month: Int, val year: Int) {

    val previous: Period
        get() {
            val year = if (month == Month.JANUARY) year - 1 else year
            val month = if (month == Month.JANUARY) Month.DECEMBER else month - 1
            return Period(month, year)
        }

    val next: Period
        get() {
            val year = if (month == Month.DECEMBER) year + 1 else year
            val month = if (month == Month.DECEMBER) Month.JANUARY else month + 1
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

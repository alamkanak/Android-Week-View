package com.alamkanak.weekview

import java.util.*

internal data class FetchPeriods(
        val previous: Period,
        val current: Period,
        val next: Period
) {

    companion object {

        @JvmStatic
        fun create(firstVisibleDay: Calendar): FetchPeriods {
            val current = Period.fromDate(firstVisibleDay)
            return FetchPeriods(current.previous(), current, current.next())
        }

    }

}

data class Period(val month: Int, val year: Int) {

    fun previous(): Period {
        val year = if (month == Month.JANUARY) year - 1 else year
        val month = if (month == Month.JANUARY) Month.DECEMBER else month - 1
        return Period(month, year)
    }

    fun next(): Period {
        val year = if (month == Month.DECEMBER) year + 1 else year
        val month = if (month == Month.DECEMBER) Month.JANUARY else month + 1
        return Period(month, year)
    }

    companion object {

        fun fromDate(date: Calendar): Period {
            val month = date.month
            val year = date.year
            return Period(month, year)
        }

    }

}

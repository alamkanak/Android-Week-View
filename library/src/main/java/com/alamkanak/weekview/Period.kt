package com.alamkanak.weekview

import org.threeten.bp.LocalDate
import org.threeten.bp.Month

internal data class FetchPeriods(
        val previous: Period,
        val current: Period,
        val next: Period
) {

    companion object {

        @JvmStatic
        fun create(firstVisibleDay: LocalDate): FetchPeriods {
            val current = Period.fromDate(firstVisibleDay)
            return FetchPeriods(current.previous(), current, current.next())
        }

    }

}

data class Period(val month: Int, val year: Int) {

    fun previous(): Period {
        val year = if (month == Month.JANUARY.value) year - 1 else year
        val month = if (month == Month.JANUARY.value) Month.DECEMBER.value else month - 1
        return Period(month, year)
    }

    fun next(): Period {
        val year = if (month == Month.DECEMBER.value) year + 1 else year
        val month = if (month == Month.DECEMBER.value) Month.JANUARY.value else month + 1
        return Period(month, year)
    }

    companion object {

        fun fromDate(date: LocalDate): Period {
            val month = date.monthValue
            val year = date.year
            return Period(month, year)
        }

    }

}

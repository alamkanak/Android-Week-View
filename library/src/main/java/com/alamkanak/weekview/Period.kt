package com.alamkanak.weekview

import java.util.Calendar.DECEMBER
import java.util.Calendar.JANUARY

data class FetchedPeriods(
        val previous: Period,
        val current: Period,
        val next: Period
) {

    companion object {

        @JvmStatic
        fun create(period: Period): FetchedPeriods {
            return FetchedPeriods(period.previous(), period, period.next())
        }

    }

}

data class Period(val month: Int, val year: Int) {

    fun previous(): Period {
        val year = if (month - 1 < 0) year - 1 else year
        val month = if (month - 1 < 0) DECEMBER else month - 1
        return Period(month, year)
    }

    fun next(): Period {
        val month = (month + 1) % 12
        val year = if (month == JANUARY) year + 1 else year
        return Period(month, year)
    }

}

package com.alamkanak.weekview.sample.util

import android.content.Context
import android.widget.Toast
import java.time.LocalDate
import java.time.YearMonth

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun yearMonthsBetween(startDate: LocalDate, endDate: LocalDate): List<YearMonth> {
    val yearMonths = mutableListOf<YearMonth>()
    val maxYearMonth = endDate.yearMonth
    var currentYearMonth = startDate.yearMonth

    while (currentYearMonth <= maxYearMonth) {
        yearMonths += currentYearMonth
        currentYearMonth = currentYearMonth.plusMonths(1)
    }

    return yearMonths
}

private val LocalDate.yearMonth: YearMonth
    get() = YearMonth.of(year, month)

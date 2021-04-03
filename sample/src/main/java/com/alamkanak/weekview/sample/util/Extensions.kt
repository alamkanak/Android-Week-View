package com.alamkanak.weekview.sample.util

import android.content.Context
import android.widget.Toast
import java.time.LocalDate
import java.time.YearMonth

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun yearMonthsBetween(startDate: LocalDate, endDate: LocalDate): List<YearMonth> {
    val startDates = mutableListOf(startDate)
    var currentStartDate = startDate
    while (currentStartDate <= endDate) {
        startDates += currentStartDate
        currentStartDate = currentStartDate.plusMonths(1)
    }

    return startDates.map { YearMonth.of(it.year, it.month) }
}

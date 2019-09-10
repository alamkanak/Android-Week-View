package com.alamkanak.weekview.util

import java.util.Calendar

internal fun createDate(
    year: Int,
    month: Int,
    dayOfMonth: Int
) = Calendar.getInstance().apply {
    set(Calendar.YEAR, year)
    set(Calendar.MONTH, month)
    set(Calendar.DAY_OF_MONTH, dayOfMonth)
}

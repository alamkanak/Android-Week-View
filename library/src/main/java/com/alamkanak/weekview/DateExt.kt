package com.alamkanak.weekview

import java.util.*
import java.util.Calendar.*

fun Calendar.withTimeAtStartOfDay(): Calendar = DateUtils.withTimeAtStartOfDay(this)

fun Calendar.withTimeAtEndOfDay(): Calendar = DateUtils.withTimeAtEndOfDay(this)

val Calendar.isToday: Boolean
    get() = DateUtils.isSameDay(this, DateUtils.today())

val Calendar.isWeekend: Boolean
    get() = get(DAY_OF_WEEK) == SATURDAY || get(DAY_OF_WEEK) == SUNDAY

val Calendar.isBeforeToday: Boolean
    get() = before(DateUtils.today())

fun Calendar.copy(): Calendar = clone() as Calendar

fun Calendar.withTimeAtStartOfPeriod(hour: Int): Calendar {
    return DateUtils.withTimeAtStartOfPeriod(this, hour)
}

fun Calendar.withTimeAtEndOfPeriod(hour: Int): Calendar {
    return DateUtils.withTimeAtEndOfPeriod(this, hour)
}

fun Calendar.plusDays(days: Int): Calendar {
    return copy().apply {
        add(DATE, days)
    }
}

fun Calendar.addDays(days: Int) {
    add(DATE, days)
}

fun Calendar.isSameDate(other: Calendar): Boolean {
    return DateUtils.isSameDay(this, other)
}

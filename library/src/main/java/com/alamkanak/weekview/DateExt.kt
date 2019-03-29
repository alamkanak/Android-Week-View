package com.alamkanak.weekview

import java.util.*
import java.util.Calendar.*

internal fun Calendar.withTimeAtStartOfDay(): Calendar = DateUtils.withTimeAtStartOfDay(this)

internal fun Calendar.withTimeAtEndOfDay(): Calendar = DateUtils.withTimeAtEndOfDay(this)

internal val Calendar.isToday: Boolean
    get() = DateUtils.isSameDay(this, DateUtils.today())

internal val Calendar.isWeekend: Boolean
    get() = get(DAY_OF_WEEK) == SATURDAY || get(DAY_OF_WEEK) == SUNDAY

internal val Calendar.isBeforeToday: Boolean
    get() = before(DateUtils.today())

internal fun Calendar.copy(): Calendar = clone() as Calendar

internal fun Calendar.withTimeAtStartOfPeriod(hour: Int): Calendar {
    return DateUtils.withTimeAtStartOfPeriod(this, hour)
}

internal fun Calendar.withTimeAtEndOfPeriod(hour: Int): Calendar {
    return DateUtils.withTimeAtEndOfPeriod(this, hour)
}

internal fun Calendar.plusDays(days: Int): Calendar {
    return copy().apply {
        add(DATE, days)
    }
}

internal fun Calendar.addDays(days: Int) {
    add(DATE, days)
}

internal fun Calendar.isSameDate(other: Calendar): Boolean {
    return DateUtils.isSameDay(this, other)
}

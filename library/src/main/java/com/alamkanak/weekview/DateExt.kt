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

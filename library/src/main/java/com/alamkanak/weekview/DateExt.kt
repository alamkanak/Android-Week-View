package com.alamkanak.weekview

import java.util.*

fun Calendar.withTimeAtStartOfDay() = DateUtils.withTimeAtStartOfDay(this)

fun Calendar.withTimeAtEndOfDay() = DateUtils.withTimeAtEndOfDay(this)

val Calendar.isToday: Boolean
    get() = DateUtils.isSameDay(this, DateUtils.today())

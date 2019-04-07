@file:JvmName("DateUtils")
package com.alamkanak.weekview

import android.content.Context
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.*

fun getDateRange(daysSinceToday: Int, size: Int): List<Calendar> {
    val days = ArrayList<Calendar>()
    var day: Calendar

    for (dayNumber in daysSinceToday..size) {
        day = today()
        day.add(DATE, dayNumber - 1)
        days.add(day)
    }

    return days
}

fun today(): Calendar = Calendar.getInstance().withTimeAtStartOfDay()

fun firstDayOfYear(): Calendar {
    return Calendar.getInstance().withTimeAtStartOfDay().apply {
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 1)
    }
}

fun getDefaultDateFormat(numberOfDays: Int): SimpleDateFormat {
    return when (numberOfDays) {
        7 -> SimpleDateFormat("EEEEE M/dd", Locale.getDefault()) // display the first character
        1 -> SimpleDateFormat("EEEE M/dd", Locale.getDefault()) // display full weekday
        else -> SimpleDateFormat("EEE M/dd", Locale.getDefault()) // display first three characters
    }
}

fun getDefaultTimeFormat(context: Context): SimpleDateFormat {
    val format = if (DateFormat.is24HourFormat(context)) "HH:mm" else "hh a"
    return SimpleDateFormat(format, Locale.getDefault())
}

internal fun Calendar.withTimeAtStartOfDay(): Calendar = withTimeAtStartOfPeriod(0)

internal fun Calendar.withTimeAtEndOfDay(): Calendar = withTimeAtEndOfPeriod(24)

internal val Calendar.isToday: Boolean
    get() = isSameDate(today())

internal val Calendar.isWeekend: Boolean
    get() = get(DAY_OF_WEEK) == SATURDAY || get(DAY_OF_WEEK) == SUNDAY

internal val Calendar.isBeforeToday: Boolean
    get() = before(today())

internal fun Calendar.copy(): Calendar = clone() as Calendar

internal fun Calendar.withTimeAtStartOfPeriod(hour: Int): Calendar {
    return copy().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}

internal fun Calendar.withTimeAtEndOfPeriod(hour: Int): Calendar {
    return copy().apply {
        set(Calendar.HOUR_OF_DAY, hour - 1)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
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
    return get(Calendar.YEAR) == other.get(Calendar.YEAR)
            && get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
}

/**
 * Checks if this date is at the start of the next day after startTime.
 * For example, if the start date was January the 1st and startDate was January the 2nd at 00:00,
 * this method would return true.
 * @param startDate The start date of the event
 * @return Whether or not this date is at the start of the day after startDate
 */
internal fun Calendar.isAtStartOfNextDay(startDate: Calendar): Boolean {
    if (this === this.withTimeAtStartOfDay()) {
        val thisCalendar = this.copy()
        thisCalendar.add(Calendar.MILLISECOND, -1)
        return thisCalendar.isSameDate(startDate)
    }
    return false
}

val Calendar.daysFromToday: Int
    get() {
        val dateCal = withTimeAtStartOfDay().apply { timeZone = TimeZone.getTimeZone("UTC") }
        val todayCal = today().apply { timeZone = TimeZone.getTimeZone("UTC") }
        val diff = dateCal.timeInMillis - todayCal.timeInMillis
        return (diff / Constants.DAY_IN_MILLIS).toInt()
    }

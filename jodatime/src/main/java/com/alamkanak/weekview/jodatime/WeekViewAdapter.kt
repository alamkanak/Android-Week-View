package com.alamkanak.weekview.jodatime

import com.alamkanak.weekview.WeekView
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate
import java.util.Calendar

class WeekViewAdapter<T>(
    private val weekView: WeekView<T>
) {

    val firstDayOfWeek: Int
        get() = when (val day = weekView.firstDayOfWeek) {
            Calendar.MONDAY -> DateTimeConstants.MONDAY
            Calendar.TUESDAY -> DateTimeConstants.TUESDAY
            Calendar.WEDNESDAY -> DateTimeConstants.WEDNESDAY
            Calendar.THURSDAY -> DateTimeConstants.THURSDAY
            Calendar.FRIDAY -> DateTimeConstants.FRIDAY
            Calendar.SATURDAY -> DateTimeConstants.SATURDAY
            Calendar.SUNDAY -> DateTimeConstants.SUNDAY
            else -> throw IllegalArgumentException("Unknown day of week: $day")
        }

    var minDate: LocalDate?
        get() = weekView.minDate?.toLocalDate()
        set(value) {
            weekView.minDate = value?.toCalendar()
        }

    var maxDate: LocalDate?
        get() = weekView.maxDate?.toLocalDate()
        set(value) {
            weekView.maxDate = value?.toCalendar()
        }

    val firstVisibleDate: LocalDate?
        get() = weekView.firstVisibleDate?.toLocalDate()

    val lastVisibleDate: LocalDate?
        get() = weekView.lastVisibleDate?.toLocalDate()
}

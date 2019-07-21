package com.alamkanak.weekview.threetenabp

import com.alamkanak.weekview.WeekView
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import java.util.Calendar

class WeekViewAdapter<T>(
    private val weekView: WeekView<T>
) {

    val firstDayOfWeek: DayOfWeek
        get() = when (val day = weekView.firstDayOfWeek) {
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            Calendar.FRIDAY -> DayOfWeek.FRIDAY
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            Calendar.SUNDAY -> DayOfWeek.SUNDAY
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

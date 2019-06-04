package com.hellmund.weekview_androidthreethen

import com.alamkanak.weekview.OnMonthChangeListener
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import java.util.Calendar

fun <T> WeekView<T>.setOnMonthChangeListener(
    block: (startDate: LocalDate, endDate: LocalDate) -> List<WeekViewDisplayable<T>>
) {
    onMonthChangeListener = object : OnMonthChangeListener<T> {
        override fun onMonthChange(
            startDate: Calendar,
            endDate: Calendar
        ): List<WeekViewDisplayable<T>> {
            return block(startDate.toLocalDate(), endDate.toLocalDate())
        }
    }
}

internal fun Calendar.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate()
}

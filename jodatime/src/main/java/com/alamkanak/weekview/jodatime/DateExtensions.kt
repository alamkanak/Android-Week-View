package com.alamkanak.weekview.jodatime

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.util.Calendar

internal fun Calendar.toLocalDate(): LocalDate {
    val dateTimeZone = DateTimeZone.forID(timeZone.id)
    val dateTime = DateTime(timeInMillis, dateTimeZone)
    return dateTime.toLocalDate()
}

internal fun Calendar.toLocalDateTime() = LocalDateTime(timeInMillis)

internal fun LocalDate.toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = toDate()
    return calendar
}

internal fun LocalDateTime.toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = toDate()
    return calendar
}

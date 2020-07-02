package com.alamkanak.weekview.jodatime

import java.util.Calendar
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.junit.Assert
import org.junit.Test

class DateExtensionsTest {

    @Test
    fun `toLocalDate() returns correct date`() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 1995)
            set(Calendar.MONTH, Calendar.AUGUST)
            set(Calendar.DAY_OF_MONTH, 24)
        }
        val localDate = LocalDate(1995, 8, 24)
        Assert.assertEquals(localDate, calendar.toLocalDate())
    }

    @Test
    fun `toLocalDateTime() returns correct date and time`() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 1995)
            set(Calendar.MONTH, Calendar.AUGUST)
            set(Calendar.DAY_OF_MONTH, 24)
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val localDateTime = LocalDateTime(1995, 8, 24, 6, 30, 0, 0)
        Assert.assertEquals(localDateTime, calendar.toLocalDateTime())
    }

    @Test
    fun `LocalDate toCalendar() returns correct date and time`() {
        val localDateTime = LocalDateTime(1995, 8, 24, 6, 30, 0, 0)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 1995)
            set(Calendar.MONTH, Calendar.AUGUST)
            set(Calendar.DAY_OF_MONTH, 24)
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        Assert.assertEquals(calendar, localDateTime.toCalendar())
    }

    @Test
    fun `LocalDateTime toCalendar() returns correct date and time`() {
        val localDateTime = LocalDateTime(1995, 8, 24, 6, 30, 0, 0)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 1995)
            set(Calendar.MONTH, Calendar.AUGUST)
            set(Calendar.DAY_OF_MONTH, 24)
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        Assert.assertEquals(calendar, localDateTime.toCalendar())
    }
}

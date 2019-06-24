package com.alamkanak.weekview

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class DateExtensionsTest {

    @Test
    fun `returns correct day of week`() {
        val date = firstDayOfYear().withYear(2019)
        val expected = DayOfWeek.TUESDAY
        val result = date.dayOfWeek
        assertEquals(expected, result)
    }

    @Test
    fun `does correct equality check`() {
        val first = firstDayOfYear().withYear(2019)
        val second = firstDayOfYear().withYear(2019)
        assert(first.isEqual(second))

        val newSecond = second.plusMillis(1)
        assert(first.isNotEqual(newSecond))
    }

    @Test
    fun `adds days correctly`() {
        val date = firstDayOfYear().withYear(2019)
        val result = date.plusDays(2)
        assertEquals(3, result.dayOfMonth)

        val secondResult = date.plusDays(31)
        assertEquals(1, secondResult.dayOfMonth)
        assertEquals(Calendar.FEBRUARY, secondResult.month)
    }

}

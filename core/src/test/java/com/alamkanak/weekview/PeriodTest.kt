package com.alamkanak.weekview

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Test

class PeriodTest {

    @Test
    fun `returns correct previous period for January`() {
        val period = Period.fromDate(firstDayOfYear())
        val previous = period.previous

        assertEquals(previous.month, Calendar.DECEMBER)
        assertEquals(previous.year, period.year - 1)
    }

    @Test
    fun `returns correct previous period for August`() {
        val date = firstDayOfYear().withMonth(Calendar.AUGUST)
        val period = Period.fromDate(date)
        val previous = period.previous

        assertEquals(previous.month, Calendar.JULY)
        assertEquals(previous.year, period.year)
    }

    @Test
    fun `returns correct next period for August`() {
        val date = firstDayOfYear().withMonth(Calendar.AUGUST)
        val period = Period.fromDate(date)
        val next = period.next

        assertEquals(next.month, Calendar.SEPTEMBER)
        assertEquals(next.year, period.year)
    }

    @Test
    fun `returns correct next period for December`() {
        val date = firstDayOfYear().withMonth(Calendar.DECEMBER)
        val period = Period.fromDate(date)
        val next = period.next

        assertEquals(next.month, Calendar.JANUARY)
        assertEquals(next.year, period.year + 1)
    }
}

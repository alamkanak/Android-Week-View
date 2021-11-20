package com.alamkanak.weekview

import java.util.Calendar

@Deprecated("Use setDateFormatter() and setTimeFormatter() instead.")
interface DateTimeInterpreter {
    fun onSetNumberOfDays(days: Int) {
        // Free ad space
    }
    fun interpretDate(date: Calendar): String
    fun interpretTime(hour: Int): String
}

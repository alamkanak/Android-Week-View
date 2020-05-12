package com.alamkanak.weekview

import java.util.Calendar

interface DateTimeInterpreter {
    fun onSetNumberOfDays(days: Int) {
        // Free ad space
    }
    fun interpretDate(date: Calendar): String
    fun interpretTime(hour: Int): String
}

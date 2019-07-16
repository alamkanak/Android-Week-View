package com.alamkanak.weekview

import android.content.Context
import android.text.format.DateFormat
import java.util.Calendar
import java.util.Locale

interface DateTimeInterpreter {
    fun onSetNumberOfDays(days: Int) {
        // Free ad space
    }
    fun interpretDate(date: Calendar): String
    fun interpretTime(hour: Int): String
}

internal class DefaultDateTimeInterpreter(
    dateFormatProvider: DateFormatProvider,
    numberOfDays: Int
) : DateTimeInterpreter {

    private var sdfDate = getDefaultDateFormat(numberOfDays)
    private val sdfTime = getDefaultTimeFormat(dateFormatProvider.is24HourFormat)

    // This calendar is only used for interpreting the time. To avoid issues with time changes,
    // we always use the first day of the year
    private val calendar = firstDayOfYear()

    override fun onSetNumberOfDays(days: Int) {
        sdfDate = getDefaultDateFormat(days)
    }

    override fun interpretDate(date: Calendar): String {
        return sdfDate.format(date.time).toUpperCase(Locale.getDefault())
    }

    override fun interpretTime(hour: Int): String {
        val time = calendar.withTime(hour, minutes = 0)
        return sdfTime.format(time.time)
    }

}

internal interface DateFormatProvider {
    val is24HourFormat: Boolean
}

internal class RealDateFormatProvider (
    private val context: Context
) : DateFormatProvider {

    override val is24HourFormat: Boolean
        get() = DateFormat.is24HourFormat(context)

}



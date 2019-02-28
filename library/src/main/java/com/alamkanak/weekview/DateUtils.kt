package com.alamkanak.weekview

import android.content.Context
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DATE

internal object DateUtils {

    @JvmStatic
    fun getDateRange(daysSinceToday: Int, size: Int): List<Calendar> {
        val days = ArrayList<Calendar>()
        var day: Calendar

        for (dayNumber in daysSinceToday..size) {
            day = today()
            day.add(DATE, dayNumber - 1)
            days.add(day)
        }

        return days
    }

    @JvmStatic
    fun withTimeAtStartOfDay(date: Calendar): Calendar {
        return withTimeAtStartOfPeriod(date, 0)
    }

    @JvmStatic
    fun withTimeAtStartOfPeriod(date: Calendar, hour: Int): Calendar {
        val newDate = date.clone() as Calendar
        newDate.set(Calendar.HOUR_OF_DAY, hour)
        newDate.set(Calendar.MINUTE, 0)
        newDate.set(Calendar.SECOND, 0)
        newDate.set(Calendar.MILLISECOND, 0)
        return newDate
    }

    @JvmStatic
    fun withTimeAtEndOfDay(date: Calendar): Calendar {
        return withTimeAtEndOfPeriod(date, 24)
    }

    @JvmStatic
    fun withTimeAtEndOfPeriod(date: Calendar, hour: Int): Calendar {
        val newDate = date.clone() as Calendar
        newDate.set(Calendar.HOUR_OF_DAY, hour - 1)
        newDate.set(Calendar.MINUTE, 59)
        newDate.set(Calendar.SECOND, 59)
        newDate.set(Calendar.MILLISECOND, 999)
        return newDate
    }

    @JvmStatic
    fun getDaysUntilDate(date: Calendar): Int {
        val dateCal = date.withTimeAtStartOfDay()
        dateCal.timeZone = TimeZone.getTimeZone("UTC")
        val todayCal = today()
        todayCal.timeZone = TimeZone.getTimeZone("UTC")
        val dateInMillis = dateCal.timeInMillis
        val todayInMillis = todayCal.timeInMillis
        val diff = dateInMillis - todayInMillis
        return (diff / Constants.DAY_IN_MILLIS).toInt()
    }

    /**
     * Checks if two times are on the same day.
     * @param dayOne The first day.
     * @param dayTwo The second day.
     * @return Whether the times are on the same day.
     */
    @JvmStatic
    fun isSameDay(dayOne: Calendar, dayTwo: Calendar): Boolean {
        return dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)
                && dayOne.get(Calendar.DAY_OF_YEAR) == dayTwo.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Checks if date2 is at the start of the next day after date1.
     * For example, if date1 was January the 1st and date2 was January the 2nd at 00:00,
     * this method would return true.
     * @param date1 The first date
     * @param date2 The second date
     * @return Whether or not date2 is at the start of the day after date1
     */
    @JvmStatic
    fun isAtStartOfNewDay(date1: Calendar, date2: Calendar): Boolean {
        if (date2 === withTimeAtStartOfDay(date2)) {
            date2.add(Calendar.MILLISECOND, -1)
            return isSameDay(date1, date2)
        }
        return false
    }

    /**
     * Returns a calendar instance at the start of this day
     * @return the calendar instance
     */
    @JvmStatic
    fun today(): Calendar {
        return Calendar.getInstance().withTimeAtStartOfDay()
    }

    fun getDefaultDateFormat(numberOfDays: Int): SimpleDateFormat {
        return when (numberOfDays) {
            7 -> SimpleDateFormat("EEEEE M/dd", Locale.getDefault()) // display the first character
            1 -> SimpleDateFormat("EEEE M/dd", Locale.getDefault()) // display full weekday
            else -> SimpleDateFormat("EEE M/dd", Locale.getDefault()) // display first three characters
        }
    }

    @JvmStatic
    fun getDefaultTimeFormat(context: Context): SimpleDateFormat {
        return if (DateFormat.is24HourFormat(context)) {
            SimpleDateFormat("HH:mm", Locale.getDefault())
        } else {
            SimpleDateFormat("hh a", Locale.getDefault())
        }
    }

}

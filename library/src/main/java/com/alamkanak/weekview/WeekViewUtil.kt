package com.alamkanak.weekview

import java.util.*

/**
 * Created by jesse on 6/02/2016.
 */
object WeekViewUtil {


    /////////////////////////////////////////////////////////////////
    //
    //      Helper methods.
    //
    /////////////////////////////////////////////////////////////////

    /**
     * Checks if two times are on the same day.
     * @param dayOne The first day.
     * @param dayTwo The second day.
     * @return Whether the times are on the same day.
     */
    @JvmStatic
    fun isSameDay(dayOne: Calendar, dayTwo: Calendar): Boolean {
        return dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR) && dayOne.get(Calendar.DAY_OF_YEAR) == dayTwo.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Returns a calendar instance at the start of this day
     * @return the calendar instance
     */
    @JvmStatic
    fun today(): Calendar {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        return today
    }
}

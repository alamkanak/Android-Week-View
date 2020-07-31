package com.alamkanak.weekview;

import java.util.Calendar;

/**
 * Created by jesse on 6/02/2016.
 */
public class WeekViewUtil {


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
    public static boolean isSameDay(Calendar dayOne, Calendar dayTwo) {
        return dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR) && dayOne.get(Calendar.DAY_OF_YEAR) == dayTwo.get(Calendar.DAY_OF_YEAR);
    }

    private static Calendar sDayOne = Calendar.getInstance();
    private static Calendar sDayTwo = Calendar.getInstance();
    public static boolean isSameDay(long dayOne, long dayTwo) {
        sDayOne.setTimeInMillis(dayOne);
        sDayTwo.setTimeInMillis(dayTwo);
        return isSameDay(sDayOne, sDayTwo);
    }


    private static final Calendar sToday = Calendar.getInstance();
    /**
     * Returns a calendar instance at the start of this day
     * @return the calendar instance
     */
    public static Calendar today(){
        sToday.setTimeInMillis(System.currentTimeMillis());
        sToday.set(Calendar.HOUR_OF_DAY, 0);
        sToday.set(Calendar.MINUTE, 0);
        sToday.set(Calendar.SECOND, 0);
        sToday.set(Calendar.MILLISECOND, 0);
        return sToday;
    }
}

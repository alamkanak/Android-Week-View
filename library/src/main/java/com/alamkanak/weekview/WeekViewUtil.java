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
     * Checks if two dates are on the same day.
     *
     * @param dateOne The first date.
     * @param dateTwo The second date.     *
     * @return Whether the dates are on the same day.
     */
    public static boolean isSameDay(Calendar dateOne, Calendar dateTwo) {
        return dateOne.get(Calendar.YEAR) == dateTwo.get(Calendar.YEAR) && dateOne.get(Calendar.DAY_OF_YEAR) == dateTwo.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Returns a calendar instance at the start of today
     *
     * @return the calendar instance
     */
    public static Calendar today(){
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today;
    }

    /**
     * Checks if two dates are on the same day and hour.
     *
     * @param dateOne The first day.
     * @param dateTwo The second day.
     * @return Whether the dates are on the same day and hour.
     */
    public static boolean isSameDayAndHour(Calendar dateOne, Calendar dateTwo) {

        if (dateTwo != null) {
            return isSameDay(dateOne, dateTwo) && dateOne.get(Calendar.HOUR_OF_DAY) == dateTwo.get(Calendar.HOUR_OF_DAY);
        }
        return false;
    }

    /**
     * Returns the amount of days between the second date and the first date
     *
     * @param dateOne the first date
     * @param dateTwo the second date
     * @return the amount of days between dateTwo and dateOne
     */
    public static int daysBetween(Calendar dateOne, Calendar dateTwo){
        return (int) (
                (dateTwo.getTimeInMillis() + dateTwo.getTimeZone().getOffset(dateTwo.getTimeInMillis()) -
                (dateOne.getTimeInMillis() + dateOne.getTimeZone().getOffset(dateOne.getTimeInMillis())))
                        / (1000 * 60 * 60 * 24f));
    }
}

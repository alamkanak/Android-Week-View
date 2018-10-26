package com.alamkanak.weekview.utils;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by jesse on 6/02/2016.
 */
public class DateUtils {

    /**
     * Checks if two times are on the same day.
     * @param dayOne The first day.
     * @param dayTwo The second day.
     * @return Whether the times are on the same day.
     */
    public static boolean isSameDay(Calendar dayOne, Calendar dayTwo) {
        return dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)
                && dayOne.get(Calendar.DAY_OF_YEAR) == dayTwo.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Returns a calendar instance at the start of this day
     * @return the calendar instance
     */
    public static Calendar today() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today;
    }

    public static SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("EEEEE M/dd", Locale.getDefault());
    }

    public static SimpleDateFormat getTimeFormat(Context context) {
        return DateFormat.is24HourFormat(context)
                ? new SimpleDateFormat("HH:mm", Locale.getDefault())
                : new SimpleDateFormat("hh a", Locale.getDefault());
    }

}

package com.alamkanak.weekview;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static java.util.Calendar.DATE;

/**
 * Created by jesse on 6/02/2016.
 */
class DateUtils {

    static List<Calendar> getDateRange(int daysSinceToday, int size) {
        final List<Calendar> days = new ArrayList<>();
        Calendar day;

        for (int dayNumber = daysSinceToday; dayNumber <= size; dayNumber++) {
            day = today();
            day.add(DATE, dayNumber - 1);
            days.add(day);
        }

        return days;
    }

    static Calendar withTimeAtStartOfDay(Calendar date) {
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date;
    }

    static Calendar withTimeAtEndOfDay(Calendar date) {
        date.set(Calendar.HOUR_OF_DAY, 23);
        date.set(Calendar.MINUTE, 59);
        date.set(Calendar.SECOND, 59);
        date.set(Calendar.MILLISECOND, 999);
        return date;
    }

    static int getDaysUntilDate(Calendar date) {
        final long dateInMillis = date.getTimeInMillis();
        final long todayInMillis = today().getTimeInMillis();
        final long diff = dateInMillis - todayInMillis;
        return (int) (diff / Constants.DAY_IN_MILLIS);
    }

    /**
     * Checks if two times are on the same day.
     * @param dayOne The first day.
     * @param dayTwo The second day.
     * @return Whether the times are on the same day.
     */
    static boolean isSameDay(Calendar dayOne, Calendar dayTwo) {
        return dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)
                && dayOne.get(Calendar.DAY_OF_YEAR) == dayTwo.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Checks if date2 is at the start of the next day after date1.
     * For example, if date1 was January the 1st and date2 was January the 2nd at 00:00,
     * this method would return true.
     * @param date1 The first date
     * @param date2 The second date
     * @return Whether or not date2 is at the start of the day after date1
     */
    public static boolean isAtStartOfNewDay(Calendar date1, Calendar date2){
        if(date2.get(Calendar.HOUR) == 0 &&
                date2.get(Calendar.MINUTE) == 0 &&
                date2.get(Calendar.SECOND) == 0 &&
                date2.get(Calendar.MILLISECOND) == 0){
            date2.add(Calendar.MILLISECOND, -1);
            return isSameDay(date1, date2);
        }
        return false;
    }

    /**
     * Returns a calendar instance at the start of this day
     * @return the calendar instance
     */
    static Calendar today() {
        final Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today;
    }

    static SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("EEEEE M/dd", Locale.getDefault());
    }

    static SimpleDateFormat getTimeFormat(Context context) {
        return DateFormat.is24HourFormat(context)
                ? new SimpleDateFormat("HH:mm", Locale.getDefault())
                : new SimpleDateFormat("hh a", Locale.getDefault());
    }

}

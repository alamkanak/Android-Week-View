package com.alamkanak.weekview;

import java.util.Calendar;

/**
 * Created by Raquib on 1/6/2015.
 */
public abstract class DateTimeInterpreter {
    public abstract String interpretDate(Calendar date);
    public abstract String interpretTime(int hour, int minutes);
}

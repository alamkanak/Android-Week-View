package com.alamkanak.weekview;

import android.support.annotation.NonNull;

import java.util.Calendar;

/**
 * Created by Raquib on 1/6/2015.
 */
public interface DateTimeInterpreter {

    String interpretDate(@NonNull Calendar date);
    String interpretTime(int hour);

}

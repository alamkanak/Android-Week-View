package com.alamkanak.weekview.model;

import java.util.Calendar;

public class WeekViewViewState {

    public Calendar scrollToDay = null;
    public double scrollToHour = -1;

    public boolean isFirstDraw = true;
    public boolean areDimensionsInvalid = true;

    public Calendar firstVisibleDay;
    public Calendar lastVisibleDay;

    public boolean shouldRefreshEvents;

}

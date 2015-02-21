package com.alamkanak.weekview;

import java.util.Calendar;
import java.util.List;

public class MonthLoader implements WeekViewLoader {

    private WeekView.MonthChangeListener mOnMonthChangeListener;
    public MonthLoader(WeekView.MonthChangeListener listener){
        this.mOnMonthChangeListener = listener;
    }

    @Override
    public double toWeekViewPeriodIndex(Calendar instance){
        return instance.get(Calendar.YEAR)*12 + instance.get(Calendar.MONTH) + (instance.get(Calendar.DAY_OF_MONTH)-1)/30.0;
    }

    @Override
    public List<WeekViewEvent> onLoad(int periodIndex){
        return mOnMonthChangeListener.onMonthChange(periodIndex/12,periodIndex%12+1);
    }


    public WeekView.MonthChangeListener getOnMonthChangeListener() {
        return mOnMonthChangeListener;
    }

    public void setOnMonthChangeListener(WeekView.MonthChangeListener onMonthChangeListener) {
        this.mOnMonthChangeListener = onMonthChangeListener;
    }
}

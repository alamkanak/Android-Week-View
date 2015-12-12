package com.alamkanak.weekview;

import java.util.Calendar;
import java.util.List;

public class MonthLoader implements WeekViewLoader {

    private MonthChangeListener mOnMonthChangeListener;

    public MonthLoader(MonthChangeListener listener){
        this.mOnMonthChangeListener = listener;
    }

    @Override
    public double toWeekViewPeriodIndex(Calendar instance){
        return instance.get(Calendar.YEAR) * 12 + instance.get(Calendar.MONTH) + (instance.get(Calendar.DAY_OF_MONTH) - 1) / 30.0;
    }

    @Override
    public List<WeekViewEvent> onLoad(int periodIndex){
        return mOnMonthChangeListener.onMonthChange(periodIndex / 12, periodIndex % 12 + 1);
    }

    public MonthChangeListener getOnMonthChangeListener() {
        return mOnMonthChangeListener;
    }

    public void setOnMonthChangeListener(MonthChangeListener onMonthChangeListener) {
        this.mOnMonthChangeListener = onMonthChangeListener;
    }

    public interface MonthChangeListener {
        /**
         * Very important interface, it's the base to load events in the calendar.
         * This method is called three times: once to load the previous month, once to load the next month and once to load the current month.<br/>
         * <strong>That's why you can have three times the same event at the same place if you mess up with the configuration</strong>
         * @param newYear: year of the events required by the view.
         * @param newMonth: month of the events required by the view <br/><strong>1 based (not like JAVA API) --> January = 1 and December = 12</strong>.
         * @return a list of the events happening <strong>during the specified month</strong>.
         */
        List<WeekViewEvent> onMonthChange(int newYear, int newMonth);
    }
}

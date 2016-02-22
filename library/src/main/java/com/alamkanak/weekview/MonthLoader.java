package com.alamkanak.weekview;

import com.alamkanak.weekview.interfaces.WeekViewLoader;

import java.util.Calendar;
import java.util.List;

public class MonthLoader implements WeekViewLoader {

    private com.alamkanak.weekview.interfaces.MonthChangeListener mOnMonthChangeListener;

    public MonthLoader(com.alamkanak.weekview.interfaces.MonthChangeListener listener){
        this.mOnMonthChangeListener = listener;
    }

    @Override
    public double toWeekViewPeriodIndex(Calendar instance){
        return instance.get(Calendar.YEAR) * 12 + instance.get(Calendar.MONTH) + (instance.get(Calendar.DAY_OF_MONTH) - 1) / 30.0;
    }

    @Override
    public List<? extends WeekViewEvent> onLoad(int periodIndex){
        return mOnMonthChangeListener.onMonthChange(periodIndex / 12, periodIndex % 12 + 1);
    }

    public com.alamkanak.weekview.interfaces.MonthChangeListener getOnMonthChangeListener() {
        return mOnMonthChangeListener;
    }

    public void setOnMonthChangeListener(com.alamkanak.weekview.interfaces.MonthChangeListener onMonthChangeListener) {
        this.mOnMonthChangeListener = onMonthChangeListener;
    }

    /**
     * @deprecated  code refractoring
     *              {will be removed in next version} </br>
     *              use {@link com.alamkanak.weekview.interfaces.MonthChangeListener}  instead.
     */
    @Deprecated
    public interface MonthChangeListener extends com.alamkanak.weekview.interfaces.MonthChangeListener{

    }
}

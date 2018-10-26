package com.alamkanak.weekview.data;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.model.WeekViewDisplayable;
import com.alamkanak.weekview.model.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * This class is responsible for loading {@link WeekViewEvent}s into {@link WeekView}. It can handle
 * both concrete {@link WeekViewEvent} objects and {@link WeekViewDisplayable} objects. The latter is
 * an interface that can be implemented in one's actual data class and handles the conversion to a
 * {@link WeekViewEvent}.
 */
public class MonthLoader implements WeekViewLoader {

    private MonthChangeListener mOnMonthChangeListener;

    public MonthLoader(MonthChangeListener listener){
        this.mOnMonthChangeListener = listener;
    }

    @Override
    public double toWeekViewPeriodIndex(Calendar instance){
        return instance.get(Calendar.YEAR) * 12
                + instance.get(Calendar.MONTH)
                + (instance.get(Calendar.DAY_OF_MONTH) - 1) / 30.0;
    }

    @Override
    public List<? extends WeekViewEvent> onLoad(int periodIndex) {
        int newYear = periodIndex / 12;
        int newMonth = periodIndex % 12 + 1;

        List<WeekViewDisplayable> displayableItems =
                mOnMonthChangeListener.onMonthChange(newYear, newMonth);

        List<WeekViewEvent> events = new ArrayList<>();
        for (WeekViewDisplayable displayableItem : displayableItems) {
            events.add(displayableItem.toWeekViewEvent());
        }

        return events;
    }

    public MonthChangeListener getOnMonthChangeListener() {
        return mOnMonthChangeListener;
    }

    public void setOnMonthChangeListener(MonthChangeListener onMonthChangeListener) {
        this.mOnMonthChangeListener = onMonthChangeListener;
    }

    public interface MonthChangeListener {

        /**
         * Called when the month displayed in the {@link WeekView} changes.
         * @param newYear The year that is now being displayed
         * @param newMonth The month that is now being displayed
         * @return The list of {@link WeekViewDisplayable} of the provided month
         */
        List<WeekViewDisplayable> onMonthChange(int newYear, int newMonth);

    }
}

package com.alamkanak.weekview;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.alamkanak.weekview.DateUtils.today;

/**
 * This class is responsible for loading {@link WeekViewEvent}s into {@link WeekView}. It can handle
 * both concrete {@link WeekViewEvent} objects and {@link WeekViewDisplayable} objects. The latter is
 * an interface that can be implemented in one's actual data class and handles the conversion to a
 * {@link WeekViewEvent}.
 */
public class MonthLoader<T> implements WeekViewLoader<T> {

    private MonthChangeListener<T> onMonthChangeListener;

    MonthLoader(MonthChangeListener<T> listener){
        this.onMonthChangeListener = listener;
    }

    @Override
    public double toWeekViewPeriodIndex(@NonNull Calendar instance) {
        return instance.get(Calendar.YEAR) * 12
                + instance.get(Calendar.MONTH)
                + (instance.get(Calendar.DAY_OF_MONTH) - 1) / 30.0;
    }

    @NonNull
    @Override
    public List<WeekViewEvent<T>> onLoad(int periodIndex) {
        final int year = periodIndex / 12;
        final int month = periodIndex % 12;

        final Calendar startDate = DateUtils.withTimeAtStartOfDay(today());
        startDate.set(Calendar.YEAR, year);
        startDate.set(Calendar.MONTH, month);
        startDate.set(Calendar.DAY_OF_MONTH, 1);

        final int maxDays = startDate.getActualMaximum(Calendar.DAY_OF_MONTH);

        final Calendar endDate = DateUtils.withTimeAtEndOfDay(today());
        endDate.set(Calendar.YEAR, year);
        endDate.set(Calendar.MONTH, month);
        endDate.set(Calendar.DAY_OF_MONTH, maxDays);

        final List<WeekViewDisplayable<T>> displayableItems =
                onMonthChangeListener.onMonthChange(startDate, endDate);

        final List<WeekViewEvent<T>> events = new ArrayList<>();
        for (WeekViewDisplayable<T> displayableItem : displayableItems) {
            events.add(displayableItem.toWeekViewEvent());
        }

        return events;
    }

    MonthChangeListener getOnMonthChangeListener() {
        return onMonthChangeListener;
    }

    void setOnMonthChangeListener(MonthChangeListener<T> onMonthChangeListener) {
        this.onMonthChangeListener = onMonthChangeListener;
    }

}

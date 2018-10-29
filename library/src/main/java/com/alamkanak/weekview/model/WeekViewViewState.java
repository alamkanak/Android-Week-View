package com.alamkanak.weekview.model;

import com.alamkanak.weekview.ui.WeekView;

import java.util.Calendar;

import static com.alamkanak.weekview.utils.Constants.HOURS_PER_DAY;
import static java.lang.Math.max;

public class WeekViewViewState {

    public Calendar scrollToDay = null;
    public int scrollToHour = -1;

    public boolean isFirstDraw = true;
    public boolean areDimensionsInvalid = true;

    public Calendar firstVisibleDay;
    public Calendar lastVisibleDay;

    public boolean shouldRefreshEvents;

    public void update(WeekViewConfig config, UpdateListener listener) {
        if (!areDimensionsInvalid) {
            return;
        }

        final float totalHeaderHeight = config.drawingConfig.headerHeight
                + config.headerRowPadding * 2
                + config.drawingConfig.headerMarginBottom;

        final int height = WeekView.getViewHeight();

        config.effectiveMinHourHeight = max(
                config.minHourHeight,
                (int) ((height - totalHeaderHeight) / HOURS_PER_DAY)
        );

        areDimensionsInvalid = false;
        if (scrollToDay != null) {
            listener.goToDate(scrollToDay);
        }

        areDimensionsInvalid = false;
        if (scrollToHour >= 0) {
            listener.goToHour(scrollToHour);
        }

        scrollToDay = null;
        scrollToHour = -1;
        areDimensionsInvalid = false;
    }

    public void invalidate() {
        areDimensionsInvalid = false;
    }

    public interface UpdateListener {
        void goToDate(Calendar date);
        void goToHour(int hour);
    }

}

package com.alamkanak.weekview;

import java.util.Calendar;

import static com.alamkanak.weekview.Constants.HOURS_PER_DAY;
import static java.lang.Math.max;

final class WeekViewViewState {

    Calendar scrollToDay = null;
    int scrollToHour = -1;

    boolean isFirstDraw = true;
    boolean areDimensionsInvalid = true;

    Calendar firstVisibleDay;
    Calendar lastVisibleDay;

    boolean shouldRefreshEvents;

    void update(WeekViewConfig config, UpdateListener listener) {
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

    void invalidate() {
        areDimensionsInvalid = false;
    }

    interface UpdateListener {
        void goToDate(Calendar date);
        void goToHour(int hour);
    }

}

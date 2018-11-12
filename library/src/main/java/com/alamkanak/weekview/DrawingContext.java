package com.alamkanak.weekview;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class DrawingContext {

    final List<Calendar> dayRange;
    final float startPixel;

    private DrawingContext(List<Calendar> dayRange, float startPixel) {
        this.dayRange = dayRange;
        this.startPixel = startPixel;
    }

    static DrawingContext create(WeekViewConfig config, WeekViewViewState viewState) {
        final WeekViewDrawingConfig drawConfig = config.drawingConfig;
        final float totalDayWidth = config.getTotalDayWidth();
        final int leftDaysWithGaps = (int) (Math.ceil(drawConfig.currentOrigin.x / totalDayWidth) * -1);
        final float startPixel = drawConfig.currentOrigin.x
                + totalDayWidth * leftDaysWithGaps
                + drawConfig.headerColumnWidth;

        List<Calendar> dayRange = new ArrayList<>();
        if (config.isSingleDay()) {
            final Calendar day = (Calendar) viewState.firstVisibleDay.clone();
            dayRange.add(day);
        } else {
            final int start = leftDaysWithGaps + 1;
            final int end = start + config.numberOfVisibleDays + 1;
            dayRange.addAll(DateUtils.getDateRange(start, end));
        }

        return new DrawingContext(dayRange, startPixel);
    }

}

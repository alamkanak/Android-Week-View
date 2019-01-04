package com.alamkanak.weekview;

import java.util.Calendar;
import java.util.List;

import static java.lang.Math.ceil;

class DrawingContext {

    final List<Calendar> dayRange;
    final float startPixel;

    private DrawingContext(List<Calendar> dayRange, float startPixel) {
        this.dayRange = dayRange;
        this.startPixel = startPixel;
    }

    static DrawingContext create(WeekViewConfig config) {
        final WeekViewDrawingConfig drawConfig = config.drawingConfig;
        final float totalDayWidth = config.getTotalDayWidth();
        final int leftDaysWithGaps = (int) (ceil(drawConfig.currentOrigin.x / totalDayWidth) * -1);
        final float startPixel = drawConfig.currentOrigin.x
                + totalDayWidth * leftDaysWithGaps
                + drawConfig.timeColumnWidth;

        final int start = leftDaysWithGaps + 1;
        final int end = start + config.numberOfVisibleDays + 1;
        final List<Calendar> dayRange = DateUtils.getDateRange(start, end);

        return new DrawingContext(dayRange, startPixel);
    }

}

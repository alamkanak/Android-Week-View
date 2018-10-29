package com.alamkanak.weekview.drawing;

import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.utils.DateUtils;

import java.util.Calendar;
import java.util.List;

public class DrawingContext {

    public final List<Calendar> dayRange;

    //public final int leftDaysWithGaps;
    //public final float totalDayWidth;
    public final float startPixel;

    private DrawingContext(List<Calendar> dayRange, /*int leftDaysWithGaps,
                           float totalDayWidth,*/ float startPixel) {
        this.dayRange = dayRange;
        //this.leftDaysWithGaps = leftDaysWithGaps;
        //this.totalDayWidth = totalDayWidth;
        this.startPixel = startPixel;
    }

    public static DrawingContext create(WeekViewConfig config) {
        final WeekViewDrawingConfig drawConfig = config.drawingConfig;
        final float totalDayWidth = drawConfig.widthPerDay + config.columnGap;
        final int leftDaysWithGaps = (int) (Math.ceil(drawConfig.currentOrigin.x / totalDayWidth) * -1);
        final float startPixel = drawConfig.currentOrigin.x
                + totalDayWidth * leftDaysWithGaps
                + drawConfig.headerColumnWidth;

        final int start = leftDaysWithGaps + 1;
        final int end = start + config.numberOfVisibleDays + 1;
        final List<Calendar> dayRange = DateUtils.getDateRange(start, end);

        return new DrawingContext(dayRange, /*leftDaysWithGaps, totalDayWidth,*/ startPixel);
    }

}

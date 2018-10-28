package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;

import com.alamkanak.weekview.model.WeekViewConfig;

import java.util.Calendar;

import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;

public class NowLineDrawer {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawConfig;

    public NowLineDrawer(WeekViewConfig config) {
        this.config = config;
        this.drawConfig = config.drawingConfig;
    }

    public void drawLine(float startX, float startPixel, Canvas canvas) {
        final float headerHeight = drawConfig.headerHeight
                + config.headerRowPadding * 2
                + drawConfig.headerMarginBottom;
        final float startY = headerHeight + drawConfig.timeTextHeight / 2 + drawConfig.currentOrigin.y;
        final Calendar now = Calendar.getInstance();

        // Draw line
        final float portionOfDay = now.get(HOUR_OF_DAY) + now.get(MINUTE) / 60.0f;
        final float beforeNow = portionOfDay * config.hourHeight;
        final float lineStartY = startY + beforeNow;
        canvas.drawLine(startX, lineStartY, startPixel + drawConfig.widthPerDay, lineStartY, drawConfig.nowLinePaint);

        // Draw dot at the beginning of the line
        final float dotRadius = drawConfig.nowDotPaint.getStrokeWidth();
        final float dotMargin = 32;
        canvas.drawCircle(startX + dotMargin, lineStartY, dotRadius, drawConfig.nowDotPaint);
    }

}

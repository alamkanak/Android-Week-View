package com.alamkanak.weekview;

import android.graphics.Canvas;

import java.util.Calendar;

import static com.alamkanak.weekview.DateUtils.isSameDay;
import static com.alamkanak.weekview.DateUtils.today;
import static java.lang.Math.max;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;

class NowLineDrawer {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawConfig;

    NowLineDrawer(WeekViewConfig config) {
        this.config = config;
        this.drawConfig = config.drawingConfig;
    }

    void draw(DrawingContext drawingContext, Canvas canvas) {
        final Calendar today = today();

        float startPixel = drawingContext.startPixel;

        for (Calendar day : drawingContext.dayRange) {
            final boolean isSameDay = isSameDay(day, today);
            final float startX = max(startPixel, drawConfig.headerColumnWidth);

            if (config.isSingleDay()) {
                // Add a margin at the start if we're in day view. Otherwise, screen space is too
                // precious and we refrain from doing so.
                startPixel = startPixel + config.eventMarginHorizontal;
            }

            // Draw the line at the current time.
            if (config.showNowLine && isSameDay) {
                drawLine(startX, startPixel, canvas);
            }

            // In the next iteration, start from the next day.
            startPixel += config.getTotalDayWidth();
        }
    }

    private void drawLine(float startX, float startPixel, Canvas canvas) {
        final float headerHeight = drawConfig.headerHeight
                + config.headerRowPadding * 2
                + drawConfig.headerMarginBottom;
        final float startY = headerHeight + drawConfig.currentOrigin.y;
        final Calendar now = Calendar.getInstance();

        // Draw line
        final float portionOfDay = now.get(HOUR_OF_DAY) + now.get(MINUTE) / 60.0f;
        final float beforeNow = portionOfDay * config.hourHeight;
        final float lineStartY = startY + beforeNow;
        canvas.drawLine(startX, lineStartY, startPixel + drawConfig.widthPerDay, lineStartY, drawConfig.nowLinePaint);

        if (config.showNowLineDot) {
            // Draw dot at the beginning of the line
            final float dotRadius = drawConfig.nowDotPaint.getStrokeWidth();
            final float dotMargin = 32;

            // We use startPixel to prevent the dot from sticking on the left side of the screen
            canvas.drawCircle(startPixel + dotMargin, lineStartY, dotRadius, drawConfig.nowDotPaint);
        }

    }

}

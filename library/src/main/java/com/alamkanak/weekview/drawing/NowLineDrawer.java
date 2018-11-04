package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;

import com.alamkanak.weekview.model.WeekViewConfig;

import java.util.Calendar;

import static com.alamkanak.weekview.utils.DateUtils.isSameDay;
import static com.alamkanak.weekview.utils.DateUtils.today;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;

public class NowLineDrawer {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawConfig;

    public NowLineDrawer(WeekViewConfig config) {
        this.config = config;
        this.drawConfig = config.drawingConfig;
    }

    public void draw(DrawingContext drawingContext, Canvas canvas) {
        Calendar today = today();

        float startPixel = drawingContext.startPixel;

        for (Calendar day : drawingContext.dayRange) {
            boolean isSameDay = isSameDay(day, today);
            float startX = (startPixel < drawConfig.headerColumnWidth ? drawConfig.headerColumnWidth : startPixel);

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
            startPixel += drawConfig.widthPerDay + config.columnGap;
        }
    }

    private void drawLine(float startX, float startPixel, Canvas canvas) {
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

        // We use startPixel to prevent the dot from sticking on the left side of the screen
        canvas.drawCircle(startPixel + dotMargin, lineStartY, dotRadius, drawConfig.nowDotPaint);
    }

}

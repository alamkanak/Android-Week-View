package com.alamkanak.weekview;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Calendar;

import static com.alamkanak.weekview.DateUtils.isSameDay;
import static java.lang.Math.max;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.SATURDAY;
import static java.util.Calendar.SUNDAY;

class DayBackgroundDrawer {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawConfig;

    DayBackgroundDrawer(WeekViewConfig config) {
        this.config = config;
        this.drawConfig = config.drawingConfig;
    }

    void draw(DrawingContext drawingContext, Canvas canvas) {
        float startPixel = drawingContext.startPixel;

        for (Calendar day : drawingContext.dayRange) {
            float startX = max(startPixel, drawConfig.headerColumnWidth);
            drawDayBackground(day, startX, startPixel, canvas);

            if (config.isSingleDay()) {
                // Add a margin at the start if we're in day view. Otherwise, screen space is too
                // precious and we refrain from doing so.
                startPixel += config.eventMarginHorizontal;
            }

            // In the next iteration, start from the next day.
            startPixel += config.getTotalDayWidth();
        }
    }

    private void drawDayBackground(Calendar day, float startX, float startPixel, Canvas canvas) {
        final Calendar today = DateUtils.today();
        final boolean isToday = isSameDay(day, today);

        if (drawConfig.widthPerDay + startPixel - startX <= 0) {
            return;
        }

        final float headerHeight = drawConfig.headerHeight
                + config.headerRowPadding * 2
                + drawConfig.headerMarginBottom;

        final int height = RealWeekView.getViewHeight();

        if (config.showDistinctPastFutureColor) {
            final boolean isWeekend = day.get(DAY_OF_WEEK) == SATURDAY || day.get(DAY_OF_WEEK) == SUNDAY;
            final boolean useWeekendColor = isWeekend && config.showDistinctWeekendColor;

            final Paint pastPaint = drawConfig.getPastBackgroundPaint(useWeekendColor);
            final Paint futurePaint = drawConfig.getFutureBackgroundPaint(useWeekendColor);

            final float startY = headerHeight + drawConfig.currentOrigin.y;
            final float endX = startPixel + drawConfig.widthPerDay;

            if (isToday) {
                final Calendar now = Calendar.getInstance();
                final float beforeNow = (now.get(HOUR_OF_DAY) + now.get(MINUTE) / 60.0f) * config.hourHeight;
                canvas.drawRect(startX, startY, endX, startY + beforeNow, pastPaint);
                canvas.drawRect(startX, startY + beforeNow, endX, height, futurePaint);
            } else if (day.before(today)) {
                canvas.drawRect(startX, startY, endX, height, pastPaint);
            } else {
                canvas.drawRect(startX, startY, endX, height, futurePaint);
            }
        } else {
            final Paint todayPaint = drawConfig.getTodayBackgroundPaint(isToday);
            final float right = startPixel + drawConfig.widthPerDay;
            canvas.drawRect(startX, headerHeight, right, height, todayPaint);
        }
    }

}

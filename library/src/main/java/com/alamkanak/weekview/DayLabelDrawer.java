package com.alamkanak.weekview;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Calendar;

import static com.alamkanak.weekview.DateUtils.isSameDay;
import static com.alamkanak.weekview.DateUtils.today;

class DayLabelDrawer {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawingConfig;

    DayLabelDrawer(WeekViewConfig config) {
        this.config = config;
        this.drawingConfig = config.drawingConfig;
    }

    void draw(DrawingContext drawingContext, Canvas canvas) {
        float startPixel = drawingContext.startPixel;

        for (Calendar day : drawingContext.dayRange) {
            drawLabel(day, startPixel, canvas);

            if (config.isSingleDay()) {
                startPixel += config.eventMarginHorizontal;
            }

            startPixel += config.getTotalDayWidth();
        }
    }

    private void drawLabel(Calendar day, float startPixel, Canvas canvas) {
        final Calendar today = today();
        final boolean isSameDay = isSameDay(day, today);

        // Draw the day labels.
        final String dayLabel = drawingConfig.dateTimeInterpreter.interpretDate(day);

        final float x = startPixel + drawingConfig.widthPerDay / 2;
        final float y = drawingConfig.headerTextHeight + config.headerRowPadding;
        final Paint textPaint = isSameDay ? drawingConfig.todayHeaderTextPaint : drawingConfig.headerTextPaint;
        canvas.drawText(dayLabel, x, y, textPaint);
    }

}

package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.alamkanak.weekview.model.WeekViewConfig;

import java.util.Calendar;
import java.util.List;

import static com.alamkanak.weekview.utils.DateUtils.isSameDay;
import static com.alamkanak.weekview.utils.DateUtils.today;

public class DayLabelDrawer {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawingConfig;

    public DayLabelDrawer(WeekViewConfig config) {
        this.config = config;
        this.drawingConfig = config.drawingConfig;
    }

    public void draw(List<Calendar> dayRange, float startPixel, Canvas canvas) {
        for (Calendar day : dayRange) {
            draw(day, startPixel, canvas);

            if (config.isSingleDay()) {
                startPixel = startPixel + config.eventMarginHorizontal;
            }

            startPixel += drawingConfig.widthPerDay + config.columnGap;
        }
    }

    public void draw(Calendar day, float startPixel, Canvas canvas) {
        drawLabel(day, startPixel, canvas);
    }

    private void drawLabel(Calendar day, float startPixel, Canvas canvas) {
        Calendar today = today();
        boolean isSameDay = isSameDay(day, today);

        // Draw the day labels.
        String dayLabel = drawingConfig.dateTimeInterpreter.interpretDate(day);
        if (dayLabel == null) {
            throw new IllegalStateException("A DateTimeInterpreter must not return null date");
        }

        float x = startPixel + drawingConfig.widthPerDay / 2;
        float y = drawingConfig.headerTextHeight + config.headerRowPadding;
        Paint textPaint = isSameDay ? drawingConfig.todayHeaderTextPaint : drawingConfig.headerTextPaint;
        canvas.drawText(dayLabel, x, y, textPaint);
    }

}

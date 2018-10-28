package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.utils.DateUtils;

import java.util.Calendar;

import static com.alamkanak.weekview.utils.DateUtils.isSameDay;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.SATURDAY;
import static java.util.Calendar.SUNDAY;

class DayBackgroundDrawer {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawConfig;

    DayBackgroundDrawer(WeekViewConfig config, WeekViewDrawingConfig drawConfig) {
        this.config = config;
        this.drawConfig = drawConfig;
    }

    void drawDayBackground(Calendar day, int height,
                           float startX, float startPixel, Canvas canvas) {
        Calendar today = DateUtils.today();
        boolean isToday = isSameDay(day, today);

        if (drawConfig.widthPerDay + startPixel - startX <= 0) {
            return;
        }

        float headerHeight = drawConfig.headerHeight
                + config.headerRowPadding * 2
                + drawConfig.headerMarginBottom;

        if (config.showDistinctPastFutureColor) {
            boolean isWeekend = day.get(DAY_OF_WEEK) == SATURDAY || day.get(DAY_OF_WEEK) == SUNDAY;
            boolean useWeekendColor = isWeekend && config.showDistinctWeekendColor;

            Paint pastPaint = drawConfig.getPastBackgroundPaint(useWeekendColor);
            Paint futurePaint = drawConfig.getFutureBackgroundPaint(useWeekendColor);

            float startY = headerHeight + drawConfig.timeTextHeight / 2 + drawConfig.currentOrigin.y;
            float endX = startPixel + drawConfig.widthPerDay;

            if (isToday) {
                Calendar now = Calendar.getInstance();
                float beforeNow = (now.get(HOUR_OF_DAY) + now.get(MINUTE) / 60.0f) * config.hourHeight;
                canvas.drawRect(startX, startY, endX, startY + beforeNow, pastPaint);
                canvas.drawRect(startX, startY + beforeNow, endX, height, futurePaint);
            } else if (day.before(today)) {
                canvas.drawRect(startX, startY, endX, height, pastPaint);
            } else {
                canvas.drawRect(startX, startY, endX, height, futurePaint);
            }
        } else {
            Paint todayPaint = drawConfig.getTodayBackgroundPaint(isToday);
            float top = headerHeight + drawConfig.timeTextHeight / 2;
            float right = startPixel + drawConfig.widthPerDay;
            canvas.drawRect(startX, top, right, height, todayPaint);
        }
    }

}

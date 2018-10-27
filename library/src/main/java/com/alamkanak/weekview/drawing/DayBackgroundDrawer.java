package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.utils.DateUtils;

import java.util.Calendar;

import static com.alamkanak.weekview.utils.DateUtils.isSameDay;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;

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

        if (config.showDistinctPastFutureColor) {
            boolean isWeekend = day.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || day.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
            Paint pastPaint = isWeekend && config.showDistinctWeekendColor ? drawConfig.pastWeekendBackgroundPaint : drawConfig.pastBackgroundPaint;
            Paint futurePaint = isWeekend && config.showDistinctWeekendColor ? drawConfig.futureWeekendBackgroundPaint : drawConfig.futureBackgroundPaint;
            float startY = drawConfig.headerHeight + config.headerRowPadding * 2 + drawConfig.timeTextHeight / 2 + drawConfig.headerMarginBottom + drawConfig.currentOrigin.y;

            if (isToday) {
                Calendar now = Calendar.getInstance();
                float beforeNow = (now.get(HOUR_OF_DAY) + now.get(MINUTE) / 60.0f) * config.hourHeight;
                canvas.drawRect(startX, startY, startPixel + drawConfig.widthPerDay, startY + beforeNow, pastPaint);
                canvas.drawRect(startX, startY + beforeNow, startPixel + drawConfig.widthPerDay, height, futurePaint);
            } else if (day.before(today)) {
                canvas.drawRect(startX, startY, startPixel + drawConfig.widthPerDay, height, pastPaint);
            } else {
                canvas.drawRect(startX, startY, startPixel + drawConfig.widthPerDay, height, futurePaint);
            }
        } else {
            canvas.drawRect(startX, drawConfig.headerHeight + config.headerRowPadding * 2 + drawConfig.timeTextHeight / 2 + drawConfig.headerMarginBottom, startPixel + drawConfig.widthPerDay, height, isToday ? drawConfig.todayBackgroundPaint : drawConfig.dayBackgroundPaint);
        }
    }

}

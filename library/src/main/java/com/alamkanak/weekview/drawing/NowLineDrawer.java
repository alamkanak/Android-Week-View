package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;

import com.alamkanak.weekview.model.WeekViewConfig;

import java.util.Calendar;

import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;

class NowLineDrawer {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawConfig;

    NowLineDrawer(WeekViewConfig config, WeekViewDrawingConfig drawConfig) {
        this.config = config;
        this.drawConfig = drawConfig;
    }

    void drawLine(float startX, float startPixel, Canvas canvas) {
        float startY = drawConfig.headerHeight + config.headerRowPadding * 2 + drawConfig.timeTextHeight / 2 + drawConfig.headerMarginBottom + drawConfig.currentOrigin.y;
        Calendar now = Calendar.getInstance();

        // TODO: Draw dot at the beginning of the line
        // Draw the line
        float portionOfDay = now.get(HOUR_OF_DAY) + now.get(MINUTE) / 60.0f;
        float beforeNow = portionOfDay * config.hourHeight;
        float lineStartY = startY + beforeNow;
        canvas.drawLine(startX, lineStartY, startPixel + drawConfig.widthPerDay, lineStartY, drawConfig.nowLinePaint);

        // Draw a dot at the beginning of the line
        float dotRadius = drawConfig.nowDotPaint.getStrokeWidth();
        float dotMargin = 32;
        canvas.drawCircle(startX + dotMargin, lineStartY, dotRadius, drawConfig.nowDotPaint);
    }

}

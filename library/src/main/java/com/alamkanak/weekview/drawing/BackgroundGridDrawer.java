package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;

import com.alamkanak.weekview.model.WeekViewConfig;

class BackgroundGridDrawer {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawConfig;

    BackgroundGridDrawer(WeekViewConfig config, WeekViewDrawingConfig drawConfig) {
        this.config = config;
        this.drawConfig = drawConfig;
    }

    void drawGrid(float[] hourLines, int height,
                  float startX, float startPixel, Canvas canvas) {
        int i = 0;
        for (int hour = 0; hour < 24; hour++) {
            float top = drawConfig.headerHeight + config.headerRowPadding * 2 + drawConfig.currentOrigin.y + config.hourHeight * hour + drawConfig.timeTextHeight / 2 + drawConfig.headerMarginBottom;
            if (top > drawConfig.headerHeight + config.headerRowPadding * 2 + drawConfig.timeTextHeight / 2 + drawConfig.headerMarginBottom - config.hourSeparatorStrokeWidth && top < height && startPixel + drawConfig.widthPerDay - startX > 0) {
                hourLines[i * 4] = startX;
                hourLines[i * 4 + 1] = top;
                hourLines[i * 4 + 2] = startPixel + drawConfig.widthPerDay;
                hourLines[i * 4 + 3] = top;
                i++;
            }
        }

        // Draw the lines for hours.
        canvas.drawLines(hourLines, drawConfig.hourSeparatorPaint);
    }

}

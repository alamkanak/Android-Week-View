package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;

import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.ui.WeekView;

import static com.alamkanak.weekview.utils.Constants.HOURS_PER_DAY;

public class BackgroundGridDrawer {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawConfig;

    public BackgroundGridDrawer(WeekViewConfig config) {
        this.config = config;
        this.drawConfig = config.drawingConfig;
    }

    public void drawGrid(float[] hourLines, float startX, float startPixel, Canvas canvas) {
        int height = WeekView.getViewHeight();

        float headerHeight = drawConfig.headerHeight
                + config.headerRowPadding * 2
                + drawConfig.headerMarginBottom;

        int i = 0;
        for (int hour = 0; hour < HOURS_PER_DAY; hour++) {
            float heightOfHour = config.hourHeight * hour;
            float halfTextHeight = drawConfig.timeTextHeight / 2;
            float top = headerHeight + drawConfig.currentOrigin.y + heightOfHour + halfTextHeight;

            float widthPerDay = drawConfig.widthPerDay;
            float separatorWidth = config.hourSeparatorStrokeWidth;

            // TODO: Proper names
            boolean a = top > headerHeight + halfTextHeight - separatorWidth;
            boolean b = top < height;
            boolean c = startPixel + widthPerDay - startX > 0;

            if (a && b && c) {
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

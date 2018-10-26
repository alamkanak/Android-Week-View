package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;

import com.alamkanak.weekview.model.WeekViewConfig;

public class TimeColumnDrawer {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawingConfig;

    public TimeColumnDrawer(WeekViewConfig config, WeekViewDrawingConfig drawingConfig) {
        this.config = config;
        this.drawingConfig = drawingConfig;
    }

    public void draw(Canvas canvas, int bottom) {
        float top = drawingConfig.headerHeight + config.headerRowPadding * 2;

        // Draw the background color for the header column.
        canvas.drawRect(0, top, drawingConfig.headerColumnWidth, bottom, drawingConfig.headerColumnBackgroundPaint);

        canvas.restore();
        canvas.save();

        canvas.clipRect(0, drawingConfig.headerHeight + config.headerRowPadding * 2, drawingConfig.headerColumnWidth, bottom);

        for (int i = 0; i < 24; i++) {
            top = drawingConfig.headerHeight + config.headerRowPadding * 2 + drawingConfig.currentOrigin.y + config.hourHeight * i + drawingConfig.headerMarginBottom;

            // Draw the text if its y position is not outside of the visible area. The pivot point
            // of the text is the point at the bottom-right corner.
            String time = drawingConfig.dateTimeInterpreter.interpretTime(i);
            if (time == null)
                throw new IllegalStateException("A DateTimeInterpreter must not return null time");
            if (top < bottom) {
                float x = drawingConfig.timeTextWidth + config.headerColumnPadding;
                float y = top + drawingConfig.timeTextHeight;
                canvas.drawText(time, x, y, drawingConfig.timeTextPaint);
            }
        }

        canvas.restore();
    }

}

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
        float top = drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2;

        // Draw the background color for the header column.
        canvas.drawRect(0, top, drawingConfig.mHeaderColumnWidth, bottom, drawingConfig.mHeaderColumnBackgroundPaint);

        canvas.restore();
        canvas.save();

        canvas.clipRect(0, drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2, drawingConfig.mHeaderColumnWidth, bottom);

        for (int i = 0; i < 24; i++) {
            top = drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mCurrentOrigin.y + config.mHourHeight * i + drawingConfig.mHeaderMarginBottom;

            // Draw the text if its y position is not outside of the visible area. The pivot point
            // of the text is the point at the bottom-right corner.
            String time = drawingConfig.mDateTimeInterpreter.interpretTime(i);
            if (time == null)
                throw new IllegalStateException("A DateTimeInterpreter must not return null time");
            if (top < bottom) {
                float x = drawingConfig.mTimeTextWidth + config.mHeaderColumnPadding;
                float y = top + drawingConfig.mTimeTextHeight;
                canvas.drawText(time, x, y, drawingConfig.mTimeTextPaint);
            }
        }

        canvas.restore();
    }

}

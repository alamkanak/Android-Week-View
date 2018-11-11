package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;

import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.ui.WeekView;

import static com.alamkanak.weekview.utils.Constants.HOURS_PER_DAY;

public class TimeColumnDrawer {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawingConfig;

    public TimeColumnDrawer(WeekViewConfig config) {
        this.config = config;
        this.drawingConfig = config.drawingConfig;
    }

    public void drawTimeColumn(Canvas canvas) {
        float top = drawingConfig.headerHeight
                + config.headerRowPadding * 2
                + config.headerRowBottomLineWidth;
        int bottom = WeekView.getViewHeight();

        // Draw the background color for the header column.
        canvas.drawRect(0, top, drawingConfig.headerColumnWidth, bottom, drawingConfig.headerColumnBackgroundPaint);

        canvas.restore();
        canvas.save();

        canvas.clipRect(0, top, drawingConfig.headerColumnWidth, bottom);

        // The original header height
        float headerHeight = top;

        for (int i = 1; i < HOURS_PER_DAY; i++) {
            float headerBottomMargin = drawingConfig.headerMarginBottom;
            float heightOfHour = config.hourHeight * i;
            top = headerHeight + drawingConfig.currentOrigin.y + heightOfHour + headerBottomMargin;

            // Draw the text if its y position is not outside of the visible area. The pivot point
            // of the text is the point at the bottom-right corner.
            String time = drawingConfig.dateTimeInterpreter.interpretTime(i);
            if (time == null) {
                throw new IllegalStateException("A DateTimeInterpreter must not return null time");
            }

            if (top < bottom) {
                float x = drawingConfig.timeTextWidth + config.timeColumnPadding;
                float y = top + drawingConfig.timeTextHeight / 2;
                canvas.drawText(time, x, y, drawingConfig.timeTextPaint);
            }
        }

        // Draw the vertical time column separator
        if (config.showTimeColumnSeparator) {
            final float lineX = drawingConfig.headerColumnWidth - config.timeColumnSeparatorStrokeWidth;
            canvas.drawLine(lineX, headerHeight, lineX, bottom, drawingConfig.timeColumnSeparatorPaint);
        }

        canvas.restore();
    }

}

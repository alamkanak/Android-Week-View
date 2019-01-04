package com.alamkanak.weekview;

import android.graphics.Canvas;

class TimeColumnDrawer {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawingConfig;

    TimeColumnDrawer(WeekViewConfig config) {
        this.config = config;
        this.drawingConfig = config.drawingConfig;
    }

    void drawTimeColumn(Canvas canvas) {
        float top = drawingConfig.headerHeight
                + config.headerRowPadding * 2
                + config.headerRowBottomLineWidth;
        final int bottom = WeekView.getViewHeight();

        // Draw the background color for the time column.
        canvas.drawRect(0, top, drawingConfig.timeColumnWidth, bottom, drawingConfig.timeColumnBackgroundPaint);

        canvas.restore();
        canvas.save();

        canvas.clipRect(0, top, drawingConfig.timeColumnWidth, bottom);

        // The original header height
        final float headerHeight = top;

        for (int i = 1; i < Constants.HOURS_PER_DAY; i++) {
            final float headerBottomMargin = drawingConfig.headerMarginBottom;
            final float heightOfHour = config.hourHeight * i;
            top = headerHeight + drawingConfig.currentOrigin.y + heightOfHour + headerBottomMargin;

            // Draw the text if its y position is not outside of the visible area. The pivot point
            // of the text is the point at the bottom-right corner.
            final String time = drawingConfig.dateTimeInterpreter.interpretTime(i);

            if (top < bottom) {
                final float x = drawingConfig.timeTextWidth + config.timeColumnPadding;
                final float y = top + drawingConfig.timeTextHeight / 2;
                canvas.drawText(time, x, y, drawingConfig.timeTextPaint);
            }
        }

        // Draw the vertical time column separator
        if (config.showTimeColumnSeparator) {
            final float lineX = drawingConfig.timeColumnWidth - config.timeColumnSeparatorStrokeWidth;
            canvas.drawLine(lineX, headerHeight, lineX, bottom, drawingConfig.timeColumnSeparatorPaint);
        }

        canvas.restore();
    }

}

package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;

import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.ui.WeekView;

import static com.alamkanak.weekview.utils.Constants.HOURS_PER_DAY;
import static java.lang.Math.max;

public class BackgroundGridDrawer {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawConfig;

    public BackgroundGridDrawer(WeekViewConfig config) {
        this.config = config;
        this.drawConfig = config.drawingConfig;
    }

    public void draw(DrawingContext drawingContext, Canvas canvas) {
        final int size = drawingContext.dayRange.size();

        float startPixel = drawingContext.startPixel;
        float[] hourLines;

        for (int i = 0; i < size; i++) {
            float startX = max(startPixel, drawConfig.headerColumnWidth);
            hourLines = getHourLines();
            drawGrid(hourLines, startX, startPixel, canvas);

            if (config.isSingleDay()) {
                // Add a margin at the start if we're in day view. Otherwise, screen space is too
                // precious and we refrain from doing so.
                startPixel += config.eventMarginHorizontal;
            }

            // In the next iteration, start from the next day.
            startPixel += config.getTotalDayWidth();
        }
    }

    private float[] getHourLines() {
        final WeekViewDrawingConfig drawConfig = config.drawingConfig;
        final int height = WeekView.getViewHeight();
        final float headerHeight = drawConfig.headerHeight
                + config.headerRowPadding * 2
                + drawConfig.headerMarginBottom;
        int lineCount = (int) ((height - headerHeight) / config.hourHeight) + 1;
        lineCount = (lineCount) * (config.numberOfVisibleDays + 1);
        return new float[lineCount * 4];
    }

    private void drawGrid(float[] hourLines, float startX, float startPixel, Canvas canvas) {
        drawHourLines(hourLines, startX, startPixel, canvas);
        drawDaySeparators(startPixel, canvas);
    }

    private void drawDaySeparators(float startPixel, Canvas canvas) {
        final int days = config.numberOfVisibleDays;
        final float widthPerDay = config.getTotalDayWidth();

        final float top = drawConfig.headerHeight
                + config.headerRowPadding * 2
                + drawConfig.headerMarginBottom;
        final int height = WeekView.getViewHeight();

        for (int i = 0; i < days; i++) {
            final float start = startPixel + widthPerDay * (i + 1);
            canvas.drawLine(start, top, start, top + height, drawConfig.hourSeparatorPaint);
        }
    }

    private void drawHourLines(float[] hourLines, float startX, float startPixel, Canvas canvas) {
        final int height = WeekView.getViewHeight();

        final float headerHeight = drawConfig.headerHeight
                + config.headerRowPadding * 2;

        int i = 0;
        for (int hour = 1; hour < HOURS_PER_DAY; hour++) {
            final float heightOfHour = config.hourHeight * hour;
            final float top = headerHeight + drawConfig.currentOrigin.y + heightOfHour;

            final float widthPerDay = config.getTotalDayWidth();
            final float separatorWidth = config.hourSeparatorStrokeWidth;

            final boolean isNotHiddenByHeader = top > headerHeight - separatorWidth;
            final boolean isWithinVisibleRange = top < height;
            final boolean isVisibleHorizontally = startPixel + widthPerDay - startX > 0;

            if (isNotHiddenByHeader && isWithinVisibleRange && isVisibleHorizontally) {
                hourLines[i * 4] = startX;
                hourLines[i * 4 + 1] = top;
                hourLines[i * 4 + 2] = startPixel + widthPerDay;
                hourLines[i * 4 + 3] = top;
                i++;
            }
        }

        canvas.drawLines(hourLines, drawConfig.hourSeparatorPaint);
    }

}

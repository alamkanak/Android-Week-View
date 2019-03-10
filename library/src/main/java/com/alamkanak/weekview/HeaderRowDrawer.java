package com.alamkanak.weekview;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Calendar;
import java.util.List;

class HeaderRowDrawer<T> {

    private final WeekViewConfig config;
    private final WeekViewDrawingConfig drawConfig;

    private final WeekViewCache<T> cache;
    private final WeekViewViewState viewState;

    HeaderRowDrawer(WeekViewConfig config, WeekViewCache<T> cache, WeekViewViewState viewState) {
        this.config = config;
        this.drawConfig = config.drawingConfig;
        this.cache = cache;
        this.viewState = viewState;
    }

    void draw(DrawingContext drawingContext, Canvas canvas) {
        calculateAvailableSpaceForHeader(drawingContext);
        drawHeaderRow(canvas);
    }

    private void calculateAvailableSpaceForHeader(DrawingContext drawingContext) {
        drawConfig.timeColumnWidth = drawConfig.timeTextWidth + config.timeColumnPadding * 2;
        refreshHeaderHeight(drawingContext);
    }

    private void refreshHeaderHeight(DrawingContext drawingContext) {
        final List<EventChip<T>> eventChips = cache.getAllDayEventChips();
        if (eventChips.isEmpty()) {
            drawConfig.hasEventInHeader = false;
            drawConfig.refreshHeaderHeight(config);
        }

        Calendar firstVisibleDay = viewState.getFirstVisibleDay();
        if (firstVisibleDay == null) {
            return;
        }

        List<Calendar> dateRange = drawingContext.getDateRange();
        List<WeekViewEvent<T>> visibleEvents = cache.getAllDayEventsInRange(dateRange);

        boolean containsAllDayEvent = false;
        for (WeekViewEvent<T> event : visibleEvents) {
            if (event.isAllDay()) {
                containsAllDayEvent = true;
                break;
            }
        }

        drawConfig.hasEventInHeader = containsAllDayEvent;
        drawConfig.refreshHeaderHeight(config);
    }

    private void drawHeaderRow(Canvas canvas) {
        final int width = WeekView.getViewWidth();

        canvas.restore();
        canvas.save();

        final Paint headerBackground = drawConfig.headerBackgroundPaint;

        // Hide everything in the top left corner
        final float topLeftCornerWidth = drawConfig.timeTextWidth + config.timeColumnPadding * 2;
        canvas.clipRect(0, 0, topLeftCornerWidth, drawConfig.headerHeight);
        canvas.drawRect(0, 0, topLeftCornerWidth, drawConfig.headerHeight, headerBackground);

        canvas.restore();
        canvas.save();

        // Clip to paint header row only.
        canvas.clipRect(drawConfig.timeColumnWidth, 0, width, drawConfig.headerHeight);
        canvas.drawRect(0, 0, width, drawConfig.headerHeight, headerBackground);

        canvas.restore();
        canvas.save();

        if (config.showHeaderRowBottomLine) {
            drawHeaderBottomLine(width, canvas);
        }
    }

    private void drawHeaderBottomLine(int width, Canvas canvas) {
        final int headerRowBottomLineWidth = config.headerRowBottomLineWidth;
        final float topMargin = drawConfig.headerHeight - headerRowBottomLineWidth;

        final Paint paint = new Paint();
        paint.setStrokeWidth(headerRowBottomLineWidth);
        paint.setColor(config.headerRowBottomLineColor);

        canvas.drawLine(0, topMargin, width, topMargin, paint);
    }

}

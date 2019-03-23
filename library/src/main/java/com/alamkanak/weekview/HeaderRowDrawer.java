package com.alamkanak.weekview;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Calendar;
import java.util.List;

class HeaderRowDrawer<T> {

    private final WeekViewConfigWrapper config;

    private final WeekViewCache<T> cache;
    private final WeekViewViewState viewState;

    HeaderRowDrawer(WeekViewConfigWrapper config, WeekViewCache<T> cache, WeekViewViewState viewState) {
        this.config = config;
        this.cache = cache;
        this.viewState = viewState;
    }

    void draw(DrawingContext drawingContext, Canvas canvas) {
        calculateAvailableSpaceForHeader(drawingContext);
        drawHeaderRow(canvas);
    }

    private void calculateAvailableSpaceForHeader(DrawingContext drawingContext) {
        config.setTimeColumnWidth(config.getTimeTextWidth() + config.getTimeColumnPadding() * 2);
        refreshHeaderHeight(drawingContext);
    }

    private void refreshHeaderHeight(DrawingContext drawingContext) {
        final List<EventChip<T>> eventChips = cache.getAllDayEventChips();
        if (eventChips.isEmpty()) {
            config.setHasEventInHeader(false);
            config.refreshHeaderHeight();
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

        config.setHasEventInHeader(containsAllDayEvent);
        config.refreshHeaderHeight();
    }

    private void drawHeaderRow(Canvas canvas) {
        final int width = WeekView.getViewWidth();

        canvas.restore();
        canvas.save();

        final Paint headerBackground = config.getHeaderBackgroundPaint();

        // Hide everything in the top left corner
        final float topLeftCornerWidth = config.getTimeTextWidth() + config.getTimeColumnPadding() * 2;
        canvas.clipRect(0, 0, topLeftCornerWidth, config.getHeaderHeight());
        canvas.drawRect(0, 0, topLeftCornerWidth, config.getHeaderHeight(), headerBackground);

        canvas.restore();
        canvas.save();

        // Clip to paint header row only.
        canvas.clipRect(config.getTimeColumnWidth(), 0, width, config.getHeaderHeight());
        canvas.drawRect(0, 0, width, config.getHeaderHeight(), headerBackground);

        canvas.restore();
        canvas.save();

        if (config.getShowHeaderRowBottomLine()) {
            drawHeaderBottomLine(width, canvas);
        }
    }

    private void drawHeaderBottomLine(int width, Canvas canvas) {
        final float headerRowBottomLineWidth = config.getHeaderRowBottomLinePaint().getStrokeWidth();
        final float topMargin = config.getHeaderHeight() - headerRowBottomLineWidth;

        final Paint paint = new Paint();
        paint.setStrokeWidth(headerRowBottomLineWidth);
        paint.setColor(config.getHeaderRowBottomLinePaint().getColor());

        canvas.drawLine(0, topMargin, width, topMargin, paint);
    }

}

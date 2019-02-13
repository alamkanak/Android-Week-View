package com.alamkanak.weekview;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Calendar;
import java.util.List;

import static java.util.Calendar.DATE;

class HeaderRowDrawer<T> {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawConfig;

    private WeekViewData<T> data;
        private WeekViewViewState viewState;

    HeaderRowDrawer(WeekViewConfig config, WeekViewData<T> data, WeekViewViewState viewState) {
        this.config = config;
        this.drawConfig = config.drawingConfig;
        this.data = data;
        this.viewState = viewState;
    }

    void draw(Canvas canvas) {
        calculateAvailableSpaceForHeader();
        drawHeaderRow(canvas);
    }

    private void calculateAvailableSpaceForHeader() {
        drawConfig.timeColumnWidth = drawConfig.timeTextWidth + config.timeColumnPadding * 2;
        refreshHeaderHeight();
    }

    private void refreshHeaderHeight() {
        final List<EventChip<T>> eventChips = data.getAllDayEventChips();
        if (eventChips.isEmpty()) {
            drawConfig.hasEventInHeader = false;
            drawConfig.refreshHeaderHeight(config);
        }

        // Make sure the header is the right size (depends on AllDay events)
        boolean containsAllDayEvent = false;
        for (int i = 0; i < config.numberOfVisibleDays; i++) {
            final Calendar day = (Calendar) viewState.getFirstVisibleDay().clone();
            day.add(DATE, i);

            for (int j = 0; j < eventChips.size(); j++) {
                final WeekViewEvent event = eventChips.get(j).event;
                if (event.isSameDay(day) && event.isAllDay()) {
                    containsAllDayEvent = true;
                    break;
                }
            }

            if (containsAllDayEvent) {
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

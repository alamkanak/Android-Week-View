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
        drawConfig.headerColumnWidth = drawConfig.timeTextWidth + config.timeColumnPadding * 2;
        drawConfig.headerHeight = calculateHeaderHeight();
    }

    private float calculateHeaderHeight() {
        List<EventChip<T>> eventChips = data.getAllDayEventChips();
        if (eventChips == null || eventChips.isEmpty()) {
            return drawConfig.headerTextHeight;
        }

        // Make sure the header is the right size (depends on AllDay events)
        boolean containsAllDayEvent = false;
        for (int i = 0; i < config.numberOfVisibleDays; i++) {
            Calendar day = (Calendar) viewState.firstVisibleDay.clone();
            day.add(DATE, i);

            for (int j = 0; j < eventChips.size(); j++) {
                WeekViewEvent event = eventChips.get(j).event;
                if (event.isSameDay(day) && event.isAllDay()) {
                    containsAllDayEvent = true;
                    break;
                }
            }

            if (containsAllDayEvent) {
                break;
            }
        }

        int headerRowBottomLine = 0;
        if (config.showHeaderRowBottomLine) {
            headerRowBottomLine = config.headerRowBottomLineWidth;
        }

        if (containsAllDayEvent) {
            float headerTextSize = drawConfig.eventTextPaint.getTextSize();
            float totalEventPadding = config.eventPadding * 2;

            float eventChipBottomPadding = config.timeColumnTextSize / 4;

            return drawConfig.headerTextHeight + (headerTextSize
                    + totalEventPadding + eventChipBottomPadding
                    + headerRowBottomLine + drawConfig.headerMarginBottom);
        } else {
            return drawConfig.headerTextHeight + headerRowBottomLine;
        }
    }

    private void drawHeaderRow(Canvas canvas) {
        final int width = RealWeekView.getViewWidth();

        canvas.restore();
        canvas.save();

        final Paint headerBackground = drawConfig.headerBackgroundPaint;
        final float headerHeight = drawConfig.headerHeight
                + config.headerRowPadding * 2
                + config.headerRowBottomLineWidth;

        // Hide everything in the top left corner
        final float topLeftCornerWidth = drawConfig.timeTextWidth + config.timeColumnPadding * 2;
        canvas.clipRect(0, 0, topLeftCornerWidth, headerHeight);
        canvas.drawRect(0, 0, topLeftCornerWidth, headerHeight, headerBackground);

        canvas.restore();
        canvas.save();

        // Clip to paint header row only.
        canvas.clipRect(drawConfig.headerColumnWidth, 0, width, headerHeight);
        canvas.drawRect(0, 0, width, headerHeight, headerBackground);

        canvas.restore();
        canvas.save();

        if (config.showHeaderRowBottomLine) {
            drawHeaderBottomLine(headerHeight, width, canvas);
        }
    }

    private void drawHeaderBottomLine(float headerHeight, int width, Canvas canvas) {
        final int headerRowBottomLineWidth = config.headerRowBottomLineWidth;
        final float topMargin = headerHeight - headerRowBottomLineWidth;

        final Paint paint = new Paint();
        paint.setStrokeWidth(headerRowBottomLineWidth);
        paint.setColor(config.headerRowBottomLineColor);

        canvas.drawLine(0, topMargin, width, topMargin, paint);
    }

}

package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.model.WeekViewData;
import com.alamkanak.weekview.model.WeekViewEvent;
import com.alamkanak.weekview.model.WeekViewViewState;
import com.alamkanak.weekview.ui.WeekView;

import java.util.Calendar;
import java.util.List;

import static java.util.Calendar.DATE;

public class HeaderRowDrawer {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawConfig;

    private WeekViewData data;
    private WeekViewViewState viewState;

    public HeaderRowDrawer(WeekViewConfig config, WeekViewData data, WeekViewViewState viewState) {
        this.config = config;
        this.drawConfig = config.drawingConfig;
        this.data = data;
        this.viewState = viewState;
    }

    public void draw(Canvas canvas) {
        calculateAvailableSpaceForHeader();
        drawHeaderRow(canvas);
    }

    private void calculateAvailableSpaceForHeader() {
        int width = WeekView.getViewWidth();

        // Calculate the available width for each day
        drawConfig.headerColumnWidth = drawConfig.timeTextWidth + config.headerColumnPadding * 2;
        drawConfig.widthPerDay = width - drawConfig.headerColumnWidth - config.columnGap * (config.numberOfVisibleDays - 1);
        drawConfig.widthPerDay = drawConfig.widthPerDay / config.numberOfVisibleDays;

        // Calculate the header height
        drawConfig.headerHeight = calculateHeaderHeight(
                data.getAllDayEventChips(), config.numberOfVisibleDays, viewState.firstVisibleDay);
    }

    private float calculateHeaderHeight(List<EventChip> eventChips,
                                        int numberOfVisibleDays, Calendar firstVisibleDay) {
        if (eventChips == null || eventChips.isEmpty()) {
            return drawConfig.headerTextHeight;
        }

        // Make sure the header is the right size (depends on AllDay events)
        boolean containsAllDayEvent = false;
        for (int i = 0; i < numberOfVisibleDays; i++) {
            Calendar day = (Calendar) firstVisibleDay.clone();
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

        if (containsAllDayEvent) {
            // TODO: Make adapt to number of all-day events
            float headerTextSize = drawConfig.eventTextPaint.getTextSize();
            float totalEventPadding = config.eventPadding * 2;
            return drawConfig.headerTextHeight + (headerTextSize + totalEventPadding + drawConfig.headerMarginBottom);
        } else {
            return drawConfig.headerTextHeight;
        }
    }

    private void drawHeaderRow(Canvas canvas) {
        int width = WeekView.getViewWidth();

        canvas.restore();
        canvas.save();

        Paint headerBackground = drawConfig.headerBackgroundPaint;
        float headerHeight = drawConfig.headerHeight + config.headerRowPadding * 2;

        // Hide everything in the top left corner
        float topLeftCornerWidth = drawConfig.timeTextWidth + config.headerColumnPadding * 2;
        canvas.clipRect(0, 0, topLeftCornerWidth, headerHeight);
        canvas.drawRect(0, 0, topLeftCornerWidth, headerHeight, headerBackground);

        canvas.restore();
        canvas.save();

        // Clip to paint header row only.
        canvas.clipRect(drawConfig.headerColumnWidth, 0, width, headerHeight);

        // Draw the header background.
        canvas.drawRect(0, 0, width, headerHeight, headerBackground);
    }

}

package com.alamkanak.weekview;

import android.graphics.RectF;

import static com.alamkanak.weekview.Constants.HOURS_PER_DAY;
import static com.alamkanak.weekview.Constants.MINUTES_PER_DAY;

class EventChipRectCalculator {

    private WeekViewConfig config;

    EventChipRectCalculator(WeekViewConfig config) {
        this.config = config;
    }

    RectF calculateSingleEvent(EventChip eventChip, float startFromPixel) {
        final float eventMargin = config.eventMarginVertical;

        final float verticalOrigin = config.drawingConfig.currentOrigin.y;
        final float widthPerDay = config.drawingConfig.widthPerDay;

        final float headerHeight = config.drawingConfig.headerHeight;
        final float headerPadding = config.headerRowPadding * 2;
        final float headerBottomMargin = config.drawingConfig.headerMarginBottom;
        final float totalHeaderHeight = headerHeight + headerPadding + headerBottomMargin;

        // Calculate top
        final float verticalDistanceFromTop = config.hourHeight * HOURS_PER_DAY * eventChip.top / MINUTES_PER_DAY;
        final float top = verticalDistanceFromTop + verticalOrigin + totalHeaderHeight + eventMargin;

        // Calculate bottom
        final float verticalDistanceFromBottom = config.hourHeight * HOURS_PER_DAY * eventChip.bottom / MINUTES_PER_DAY;
        final float bottom = verticalDistanceFromBottom + verticalOrigin + totalHeaderHeight - eventMargin;

        // Calculate left and right
        float left = startFromPixel + eventChip.left * widthPerDay;
        float right = left + eventChip.width * widthPerDay;

        // Adjust left and right with overlappingEventGap
        if (left > startFromPixel) {
            left += config.overlappingEventGap / 2;
        }

        if (right < startFromPixel + widthPerDay) {
            right -= config.overlappingEventGap / 2;
        }

        boolean hasNoOverlaps = (right == startFromPixel + widthPerDay);
        if (config.numberOfVisibleDays == 1 && hasNoOverlaps) {
            right -= config.eventMarginHorizontal * 2;
        }

        return new RectF(left, top, right, bottom);
    }

    RectF calculateAllDayEvent(EventChip eventChip, float startFromPixel) {
        final float headerHeight = config.headerRowPadding + config.headerRowPadding / 2
            + config.drawingConfig.headerMarginBottom;
        final float widthPerDay = config.drawingConfig.widthPerDay;
        // Calculate top
        final float top = headerHeight + config.drawingConfig.headerTextHeight + config.eventMarginVertical;

        // Calculate bottom
        final float bottom = top + eventChip.bottom;

        // Calculate left & right
        float left = startFromPixel + eventChip.left * widthPerDay;
        float right = left + eventChip.width * widthPerDay;

        // Adjust left and right with overlappingEventGap
        if (left > startFromPixel) {
            left += config.overlappingEventGap / 2;
        }

        if (right < startFromPixel + widthPerDay) {
            right -= config.overlappingEventGap / 2;
        }

        boolean hasNoOverlaps = (right == startFromPixel + widthPerDay);
        if (config.numberOfVisibleDays == 1 && hasNoOverlaps) {
            right -= config.eventMarginHorizontal * 2;
        }

        return new RectF(left, top, right, bottom);
    }

}

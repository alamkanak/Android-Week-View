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

        // Calculate top
        final float verticalDistanceFromTop = config.hourHeight * HOURS_PER_DAY * eventChip.top / MINUTES_PER_DAY;
        final float top = verticalDistanceFromTop + verticalOrigin + config.drawingConfig.headerHeight + eventMargin;

        // Calculate bottom
        final float verticalDistanceFromBottom = config.hourHeight * HOURS_PER_DAY * eventChip.bottom / MINUTES_PER_DAY;
        final float bottom = verticalDistanceFromBottom + verticalOrigin + config.drawingConfig.headerHeight - eventMargin;

        // Calculate left and right
        float left = startFromPixel + eventChip.left * widthPerDay;
        float right = left + eventChip.width * widthPerDay;

        // Adjust left and right with overlappingEventGap
        if (left > startFromPixel) {
            left += config.overlappingEventGap / 2f;
        }

        if (right < startFromPixel + widthPerDay) {
            right -= config.overlappingEventGap / 2f;
        }

        boolean hasNoOverlaps = (right == startFromPixel + widthPerDay);
        if (config.numberOfVisibleDays == 1 && hasNoOverlaps) {
            right -= config.eventMarginHorizontal * 2;
        }

        return new RectF(left, top, right, bottom);
    }

    RectF calculateAllDayEvent(EventChip eventChip, float startFromPixel) {
        final float top = config.headerRowPadding * 1.5f + config.drawingConfig.headerTextHeight;
        final float widthPerDay = config.drawingConfig.widthPerDay;

        // Calculate top

        // Calculate bottom
        final float bottom = top + eventChip.bottom;

        // Calculate left & right
        float left = startFromPixel + eventChip.left * widthPerDay;
        float right = left + eventChip.width * widthPerDay;

        // Adjust left and right with overlappingEventGap
        if (left > startFromPixel) {
            left += config.overlappingEventGap / 2f;
        }

        if (right < startFromPixel + widthPerDay) {
            right -= config.overlappingEventGap / 2f;
        }

        boolean hasNoOverlaps = (right == startFromPixel + widthPerDay);
        if (config.numberOfVisibleDays == 1 && hasNoOverlaps) {
            right -= config.eventMarginHorizontal * 2;
        }

        return new RectF(left, top, right, bottom);
    }

}

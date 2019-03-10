package com.alamkanak.weekview;

import android.graphics.RectF;

class EventChipRectCalculator {

    private final WeekViewConfig config;

    EventChipRectCalculator(WeekViewConfig config) {
        this.config = config;
    }

    RectF calculateSingleEvent(EventChip eventChip, float startFromPixel) {
        final float eventMargin = config.eventMarginVertical;

        final float verticalOrigin = config.drawingConfig.currentOrigin.y;
        final float widthPerDay = config.drawingConfig.widthPerDay;

        // Calculate top
        final float verticalDistanceFromTop = config.hourHeight * config.getHoursPerDay() * eventChip.top / config.getMinutesPerDay();
        final float top = verticalDistanceFromTop + verticalOrigin + config.drawingConfig.headerHeight + eventMargin;

        // Calculate bottom
        final float verticalDistanceFromBottom = config.hourHeight * config.getHoursPerDay() * eventChip.bottom / config.getMinutesPerDay();
        final float bottom = verticalOrigin + config.drawingConfig.headerHeight + verticalDistanceFromBottom - eventMargin;

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

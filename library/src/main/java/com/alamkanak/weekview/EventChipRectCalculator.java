package com.alamkanak.weekview;

import android.graphics.RectF;

class EventChipRectCalculator {

    private final WeekViewConfigWrapper config;

    EventChipRectCalculator(WeekViewConfigWrapper config) {
        this.config = config;
    }

    RectF calculateSingleEvent(EventChip eventChip, float startFromPixel) {
        final float eventMargin = config.getEventMarginVertical();

        final float verticalOrigin = config.getCurrentOrigin().y;
        final float widthPerDay = config.getWidthPerDay();

        // TODO
        // Calculate top
        final float verticalDistanceFromTop = config.getHourHeight() * config.getHoursPerDay() * eventChip.top / config.getMinutesPerDay();
        final float top = verticalDistanceFromTop + verticalOrigin + config.getHeaderHeight() + eventMargin;

        // Calculate bottom
        final float verticalDistanceFromBottom = config.getHourHeight() * config.getHoursPerDay() * eventChip.bottom / config.getMinutesPerDay();
        final float bottom = verticalOrigin + config.getHeaderHeight() + verticalDistanceFromBottom - eventMargin;

        // Calculate left and right
        float left = startFromPixel + eventChip.left * widthPerDay;
        float right = left + eventChip.width * widthPerDay;

        // Adjust left and right with overlappingEventGap
        if (left > startFromPixel) {
            left += config.getOverlappingEventGap() / 2f;
        }

        if (right < startFromPixel + widthPerDay) {
            right -= config.getOverlappingEventGap() / 2f;
        }

        boolean hasNoOverlaps = (right == startFromPixel + widthPerDay);
        if (config.getNumberOfVisibleDays() == 1 && hasNoOverlaps) {
            right -= config.getEventMarginHorizontal() * 2;
        }

        return new RectF(left, top, right, bottom);
    }

    RectF calculateAllDayEvent(EventChip eventChip, float startFromPixel) {
        final float top = config.getHeaderRowPadding() * 1.5f + config.getHeaderTextHeight();
        final float widthPerDay = config.getWidthPerDay();

        // Calculate bottom
        final float bottom = top + eventChip.bottom;

        // Calculate left & right
        float left = startFromPixel + eventChip.left * widthPerDay;
        float right = left + eventChip.width * widthPerDay;

        // Adjust left and right with overlappingEventGap
        if (left > startFromPixel) {
            left += config.getOverlappingEventGap() / 2f;
        }

        if (right < startFromPixel + widthPerDay) {
            right -= config.getOverlappingEventGap() / 2f;
        }

        boolean hasNoOverlaps = (right == startFromPixel + widthPerDay);
        if (config.getNumberOfVisibleDays() == 1 && hasNoOverlaps) {
            right -= config.getEventMarginHorizontal() * 2;
        }

        return new RectF(left, top, right, bottom);
    }

}

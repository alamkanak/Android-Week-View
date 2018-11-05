package com.alamkanak.weekview.drawing;

import android.graphics.RectF;

import com.alamkanak.weekview.model.WeekViewConfig;

import static com.alamkanak.weekview.utils.Constants.HOURS_PER_DAY;
import static com.alamkanak.weekview.utils.Constants.MINUTES_PER_DAY;

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

        // Calculate left
        float left = startFromPixel + eventChip.left * widthPerDay;
        if (left < startFromPixel) {
            left += config.overlappingEventGap;
        }

        // Calculate right
        float right = left + eventChip.width * widthPerDay;
        if (right < startFromPixel + widthPerDay) {
            right -= config.overlappingEventGap;
        }

        boolean hasNoOverlaps = (right == startFromPixel + widthPerDay);
        if (config.numberOfVisibleDays == 1 && hasNoOverlaps) {
            right -= config.eventMarginHorizontal * 2;
        }

        return new RectF(left, top, right, bottom);
    }

    RectF calculateAllDayEvent(EventChip eventChip, float startFromPixel) {
        final float headerHeight = config.headerRowPadding * 2 + config.drawingConfig.headerMarginBottom;
        final float widthPerDay = config.drawingConfig.widthPerDay;
        final float halfTextHeight = config.drawingConfig.timeTextHeight / 2;

        // Calculate top
        final float top = headerHeight + halfTextHeight + config.eventMarginVertical;

        // Calculate bottom
        final float bottom = top + eventChip.bottom;

        // Calculate left
        float left = startFromPixel + eventChip.left * widthPerDay;
        if (left < startFromPixel) {
            left += config.overlappingEventGap;
        }

        // Calculate right
        float right = left + eventChip.width * widthPerDay;
        if (right < startFromPixel + widthPerDay) {
            right -= config.overlappingEventGap;
        }

        boolean hasNoOverlaps = (right == startFromPixel + widthPerDay);
        if (config.numberOfVisibleDays == 1 && hasNoOverlaps) {
            right -= config.eventMarginHorizontal * 2;
        }

        return new RectF(left, top, right, bottom);
    }

}

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

    RectF calculate(EventChip eventChip, float startFromPixel) {
        // TODO: Fix white bar at top of screen

        float eventMargin = config.eventMarginVertical;
        float halfTextHeight = config.drawingConfig.timeTextHeight / 2;

        float verticalOrigin = config.drawingConfig.currentOrigin.y;
        float widthPerDay = config.drawingConfig.widthPerDay;

        float headerHeight = config.drawingConfig.headerHeight;
        float headerPadding = config.headerRowPadding * 2;
        float headerBottomMargin = config.drawingConfig.headerMarginBottom;
        float totalHeaderHeight = headerHeight + headerPadding + headerBottomMargin;

        // Calculate top
        float verticalDistanceFromTop = config.hourHeight * HOURS_PER_DAY * eventChip.top / MINUTES_PER_DAY;
        float top = verticalDistanceFromTop + verticalOrigin + totalHeaderHeight + halfTextHeight + eventMargin;

        // Calculate bottom
        float verticalDistanceFromBottom = config.hourHeight * HOURS_PER_DAY * eventChip.bottom / MINUTES_PER_DAY;
        float bottom = verticalDistanceFromBottom + verticalOrigin + totalHeaderHeight + halfTextHeight - eventMargin;

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

    RectF calculateAllDay(EventChip eventChip, float startFromPixel) {
        float headerHeight = config.headerRowPadding * 2 + config.drawingConfig.headerMarginBottom;
        float widthPerDay = config.drawingConfig.widthPerDay;
        float halfTextHeight = config.drawingConfig.timeTextHeight / 2;

        // Calculate top
        float top = headerHeight + halfTextHeight + config.eventMarginVertical;

        // Calculate bottom
        float bottom = top + eventChip.bottom;

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

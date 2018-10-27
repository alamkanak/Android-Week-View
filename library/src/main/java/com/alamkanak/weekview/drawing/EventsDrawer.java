package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.style.StyleSpan;

import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.model.WeekViewEvent;

import java.util.Calendar;
import java.util.List;

public class EventsDrawer {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawingConfig;

    public EventsDrawer(WeekViewConfig config, WeekViewDrawingConfig drawingConfig) {
        this.config = config;
        this.drawingConfig = drawingConfig;
    }

    public void draw(List<EventRect> eventRects,
                     int width, int height,
                     Calendar date, float startFromPixel, Canvas canvas) {
        for (int i = 0; i < eventRects.size(); i++) {
            EventRect eventRect = eventRects.get(i);
            WeekViewEvent event = eventRect.event;
            if (!event.isSameDay(date) || event.isAllDay()) {
                continue;
            }

            // Calculate top.
            float top = config.hourHeight * 24 * eventRect.top / 1440 + drawingConfig.currentOrigin.y + drawingConfig.headerHeight + config.headerRowPadding * 2 + drawingConfig.headerMarginBottom + drawingConfig.timeTextHeight / 2 + config.eventMarginVertical;

            // Calculate bottom.
            float bottom = eventRect.bottom;
            bottom = config.hourHeight * 24 * bottom / 1440 + drawingConfig.currentOrigin.y + drawingConfig.headerHeight + config.headerRowPadding * 2 + drawingConfig.headerMarginBottom + drawingConfig.timeTextHeight / 2 - config.eventMarginVertical;

            // Calculate left and right.
            float left = startFromPixel + eventRect.left * drawingConfig.widthPerDay;
            if (left < startFromPixel) {
                left += config.overlappingEventGap;
            }

            float right = left + eventRect.width * drawingConfig.widthPerDay;
            if (right < startFromPixel + drawingConfig.widthPerDay) {
                right -= config.overlappingEventGap;
            }

            boolean hasNoOverlaps = (right == startFromPixel + drawingConfig.widthPerDay);
            if (config.numberOfVisibleDays == 1 && hasNoOverlaps) {
                right -= config.eventMarginHorizontal * 2;
            } else if (config.numberOfVisibleDays == 1) {
                right -= config.eventMarginHorizontal * 2;
            }

            // Draw the event and the event name on top of it.
            if (left < right
                    && left < width
                    && top < height
                    && right > drawingConfig.headerColumnWidth
                    && bottom > drawingConfig.headerHeight + config.headerRowPadding * 2 + drawingConfig.timeTextHeight / 2 + drawingConfig.headerMarginBottom) {
                eventRect.rectF = new RectF(left, top, right, bottom);
                drawingConfig.eventBackgroundPaint.setColor(event.getColor() == 0 ? drawingConfig.defaultEventColor : event.getColor());
                canvas.drawRoundRect(eventRect.rectF, config.eventCornerRadius, config.eventCornerRadius, drawingConfig.eventBackgroundPaint);
                drawEventTitle(event, eventRects.get(i).rectF, canvas, top, left);
            } else {
                eventRect.rectF = null;
            }
        }
    }

    /**
     * Draw all the all-day events of a particular day.
     *
     * @param date           The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas         The canvas to draw upon.
     */
    public void drawAllDayEvents(List<EventRect> eventRects,
                                 int width, int height,
                                 Calendar date, float startFromPixel, Canvas canvas) {
        if (eventRects == null) {
            return;
        }

        for (int i = 0; i < eventRects.size(); i++) {
            WeekViewEvent event = eventRects.get(i).event;
            if (!event.isSameDay(date) || !event.isAllDay()) {
                continue;
            }

            EventRect eventRect = eventRects.get(i);

            // Calculate top.
            float top = config.headerRowPadding * 2 + drawingConfig.headerMarginBottom + +drawingConfig.timeTextHeight / 2 + config.eventMarginVertical;

            // Calculate bottom.
            float bottom = top + eventRect.bottom;

            // Calculate left and right.
            float left = startFromPixel + eventRect.left * drawingConfig.widthPerDay;
            if (left < startFromPixel) {
                left += config.overlappingEventGap;
            }
            float right = left + eventRect.width * drawingConfig.widthPerDay;
            if (right < startFromPixel + drawingConfig.widthPerDay) {
                right -= config.overlappingEventGap;
            }

            boolean hasNoOverlaps = (right == startFromPixel + drawingConfig.widthPerDay);
            if (config.numberOfVisibleDays == 1 && hasNoOverlaps) {
                right -= config.eventMarginHorizontal * 2;
            }

            // Draw the event and the event name on top of it.
            if (left < right &&
                    left < width &&
                    top < height &&
                    right > drawingConfig.headerColumnWidth &&
                    bottom > 0) {
                eventRect.rectF = new RectF(left, top, right, bottom);
                drawingConfig.eventBackgroundPaint.setColor(event.getColor() == 0 ? drawingConfig.defaultEventColor : event.getColor());
                canvas.drawRoundRect(eventRect.rectF, config.eventCornerRadius, config.eventCornerRadius, drawingConfig.eventBackgroundPaint);
                drawEventTitle(event, eventRect.rectF, canvas, top, left);
            } else {
                eventRect.rectF = null;
            }
        }
    }

    /**
     * Draw the name of the event on top of the event rectangle.
     *
     * @param event        The event of which the title (and location) should be drawn.
     * @param rect         The rectangle on which the text is to be drawn.
     * @param canvas       The canvas to draw upon.
     * @param originalTop  The original top position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     * @param originalLeft The original left position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     */
    private void drawEventTitle(WeekViewEvent event, RectF rect, Canvas canvas, float originalTop, float originalLeft) {
        if (rect.right - rect.left - config.eventPadding * 2 < 0) return;
        if (rect.bottom - rect.top - config.eventPadding * 2 < 0) return;

        // Prepare the name of the event.
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        if (event.getName() != null) {
            stringBuilder.append(event.getName());
            stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, stringBuilder.length(), 0);
            stringBuilder.append(' ');
        }

        // Prepare the location of the event.
        if (event.getLocation() != null) {
            stringBuilder.append(event.getLocation());
        }

        int availableHeight = (int) (rect.bottom - originalTop - config.eventPadding * 2);
        int availableWidth = (int) (rect.right - originalLeft - config.eventPadding * 2);

        // Get text dimensions.
        StaticLayout textLayout = new StaticLayout(stringBuilder, drawingConfig.eventTextPaint, availableWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        int lineHeight = textLayout.getHeight() / textLayout.getLineCount();

        if (availableHeight >= lineHeight) {
            // Calculate available number of line counts.
            int availableLineCount = availableHeight / lineHeight;
            do {
                // Ellipsize text to fit into event rect.
                textLayout = new StaticLayout(TextUtils.ellipsize(stringBuilder, drawingConfig.eventTextPaint, availableLineCount * availableWidth, TextUtils.TruncateAt.END), drawingConfig.eventTextPaint, (int) (rect.right - originalLeft - config.eventPadding * 2), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

                // Reduce line count.
                availableLineCount--;

                // Repeat until text is short enough.
            } while (textLayout.getHeight() > availableHeight);

            // Draw text.
            canvas.save();
            canvas.translate(originalLeft + config.eventPadding, originalTop + config.eventPadding);
            textLayout.draw(canvas);
            canvas.restore();
        }
    }

}

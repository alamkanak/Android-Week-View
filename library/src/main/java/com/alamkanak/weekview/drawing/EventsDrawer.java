package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.style.StyleSpan;

import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.model.WeekViewEvent;

import java.util.Calendar;
import java.util.List;

import static com.alamkanak.weekview.utils.WeekViewUtil.isSameDay;

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
            if (isSameDay(eventRects.get(i).event.getStartTime(), date) && !eventRects.get(i).event.isAllDay()) {
                // Calculate top.
                float top = config.mHourHeight * 24 * eventRects.get(i).top / 1440 + drawingConfig.mCurrentOrigin.y + drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mHeaderMarginBottom + drawingConfig.mTimeTextHeight / 2 + config.mEventMarginVertical;

                // Calculate bottom.
                float bottom = eventRects.get(i).bottom;
                bottom = config.mHourHeight * 24 * bottom / 1440 + drawingConfig.mCurrentOrigin.y + drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mHeaderMarginBottom + drawingConfig.mTimeTextHeight / 2 - config.mEventMarginVertical;

                // Calculate left and right.
                float left = startFromPixel + eventRects.get(i).left * drawingConfig.mWidthPerDay;
                if (left < startFromPixel) {
                    left += config.mOverlappingEventGap;
                }

                float right = left + eventRects.get(i).width * drawingConfig.mWidthPerDay;
                if (right < startFromPixel + drawingConfig.mWidthPerDay) {
                    right -= config.mOverlappingEventGap;
                }

                boolean hasNoOverlaps = (right == startFromPixel + drawingConfig.mWidthPerDay);
                if (config.mNumberOfVisibleDays == 1 && hasNoOverlaps) {
                    right -= config.mEventMarginHorizontal * 2;
                } else if (config.mNumberOfVisibleDays == 1) {
                    right -= config.mEventMarginHorizontal * 2;
                }

                // Draw the event and the event name on top of it.
                if (left < right
                        && left < width
                        && top < height
                        && right > drawingConfig.mHeaderColumnWidth
                        && bottom > drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mTimeTextHeight / 2 + drawingConfig.mHeaderMarginBottom) {
                    eventRects.get(i).rectF = new RectF(left, top, right, bottom);
                    drawingConfig.mEventBackgroundPaint.setColor(eventRects.get(i).event.getColor() == 0 ? drawingConfig.mDefaultEventColor : eventRects.get(i).event.getColor());
                    canvas.drawRoundRect(eventRects.get(i).rectF, config.mEventCornerRadius, config.mEventCornerRadius, drawingConfig.mEventBackgroundPaint);
                    drawEventTitle(eventRects.get(i).event, eventRects.get(i).rectF, canvas, top, left);
                } else {
                    eventRects.get(i).rectF = null;
                }
            }
        }
    }

    /**
     * Draw all the Allday-events of a particular day.
     *
     * @param date           The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas         The canvas to draw upon.
     */
    public void drawAllDayEvents(List<EventRect> eventRects,
                                  int width, int height,
                                  Calendar date, float startFromPixel, Canvas canvas) {
        if (eventRects != null && eventRects.size() > 0) {
            for (int i = 0; i < eventRects.size(); i++) {
                WeekViewEvent event = eventRects.get(i).event;
                if (isSameDay(event.getStartTime(), date) && event.isAllDay()) {
                    EventRect eventRect = eventRects.get(i);

                    // Calculate top.
                    float top = config.mHeaderRowPadding * 2 + drawingConfig.mHeaderMarginBottom + +drawingConfig.mTimeTextHeight / 2 + config.mEventMarginVertical;

                    // Calculate bottom.
                    float bottom = top + eventRect.bottom;

                    // Calculate left and right.
                    float left = startFromPixel + eventRect.left * drawingConfig.mWidthPerDay;
                    if (left < startFromPixel) {
                        left += config.mOverlappingEventGap;
                    }
                    float right = left + eventRect.width * drawingConfig.mWidthPerDay;
                    if (right < startFromPixel + drawingConfig.mWidthPerDay) {
                        right -= config.mOverlappingEventGap;
                    }

                    boolean hasNoOverlaps = (right == startFromPixel + drawingConfig.mWidthPerDay);
                    if (config.mNumberOfVisibleDays == 1 && hasNoOverlaps) {
                        right -= config.mEventMarginHorizontal * 2;
                    }

                    // Draw the event and the event name on top of it.
                    if (left < right &&
                            left < width &&
                            top < height &&
                            right > drawingConfig.mHeaderColumnWidth &&
                            bottom > 0) {
                        eventRect.rectF = new RectF(left, top, right, bottom);
                        drawingConfig.mEventBackgroundPaint.setColor(event.getColor() == 0 ? drawingConfig.mDefaultEventColor : event.getColor());
                        canvas.drawRoundRect(eventRect.rectF, config.mEventCornerRadius, config.mEventCornerRadius, drawingConfig.mEventBackgroundPaint);
                        drawEventTitle(event, eventRect.rectF, canvas, top, left);
                    } else {
                        eventRect.rectF = null;
                    }
                }
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
        if (rect.right - rect.left - config.mEventPadding * 2 < 0) return;
        if (rect.bottom - rect.top - config.mEventPadding * 2 < 0) return;

        // Prepare the name of the event.
        SpannableStringBuilder bob = new SpannableStringBuilder();
        if (event.getName() != null) {
            bob.append(event.getName());
            bob.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, bob.length(), 0);
            bob.append(' ');
        }

        // Prepare the location of the event.
        if (event.getLocation() != null) {
            bob.append(event.getLocation());
        }

        int availableHeight = (int) (rect.bottom - originalTop - config.mEventPadding * 2);
        int availableWidth = (int) (rect.right - originalLeft - config.mEventPadding * 2);

        // Get text dimensions.
        StaticLayout textLayout = new StaticLayout(bob, drawingConfig.mEventTextPaint, availableWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        int lineHeight = textLayout.getHeight() / textLayout.getLineCount();

        if (availableHeight >= lineHeight) {
            // Calculate available number of line counts.
            int availableLineCount = availableHeight / lineHeight;
            do {
                // Ellipsize text to fit into event rect.
                textLayout = new StaticLayout(TextUtils.ellipsize(bob, drawingConfig.mEventTextPaint, availableLineCount * availableWidth, TextUtils.TruncateAt.END), drawingConfig.mEventTextPaint, (int) (rect.right - originalLeft - config.mEventPadding * 2), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

                // Reduce line count.
                availableLineCount--;

                // Repeat until text is short enough.
            } while (textLayout.getHeight() > availableHeight);

            // Draw text.
            canvas.save();
            canvas.translate(originalLeft + config.mEventPadding, originalTop + config.mEventPadding);
            textLayout.draw(canvas);
            canvas.restore();
        }
    }

}

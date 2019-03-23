package com.alamkanak.weekview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.MotionEvent;

import static android.text.Layout.Alignment.ALIGN_NORMAL;

/**
 * A class to hold reference to the events and their visual representation. An EventRect is
 * actually the rectangle that is drawn on the calendar for a given event. There may be more
 * than one rectangle for a single event (an event that expands more than one day). In that
 * case two instances of the EventRect will be used for a single event. The given event will be
 * stored in "originalEvent". But the event that corresponds to rectangle the rectangle
 * instance will be stored in "event".
 */
class EventChip<T> {

    final WeekViewEvent<T> event;
    final WeekViewEvent<T> originalEvent;

    RectF rect;
    float left;
    float width;
    float top;
    float bottom;

    /**
     * Create a new instance of event rect. An EventRect is actually the rectangle that is drawn
     * on the calendar for a given event. There may be more than one rectangle for a single
     * event (an event that expands more than one day). In that case two instances of the
     * EventRect will be used for a single event. The given event will be stored in
     * "originalEvent". But the event that corresponds to rectangle the rectangle instance will
     * be stored in "event".
     *
     * @param event         Represents the event which this instance of rectangle represents.
     * @param originalEvent The original event that was passed by the user.
     * @param rect         The rectangle.
     */
    EventChip(WeekViewEvent<T> event, WeekViewEvent<T> originalEvent, RectF rect) {
        this.event = event;
        this.rect = rect;
        this.originalEvent = originalEvent;
    }

    void draw(WeekViewConfigWrapper config, Canvas canvas) {
        draw(config, null, canvas);
    }

    void draw(WeekViewConfigWrapper config, @Nullable StaticLayout textLayout, Canvas canvas) {
        final float cornerRadius = config.getEventCornerRadius();
        final Paint backgroundPaint = getBackgroundPaint();
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, backgroundPaint);

        if (event.isNotAllDay()) {
            drawCornersForMultiDayEvents(backgroundPaint, cornerRadius, canvas);
        }

        if (textLayout != null) {
            // The text height has already been calculated
            drawEventTitle(config, textLayout, canvas);
        } else {
            calculateTextHeightAndDrawTitle(config, canvas);
        }
    }

    private void drawCornersForMultiDayEvents(Paint backgroundPaint,
                                              float cornerRadius, Canvas canvas) {
        if (event.startsOnEarlierDay(originalEvent)) {
            RectF topRect = new RectF(rect.left, rect.top, rect.right, rect.top + cornerRadius);
            canvas.drawRect(topRect, backgroundPaint);
        } else if (event.endsOnLaterDay(originalEvent)) {
            RectF bottomRect = new RectF(rect.left, rect.bottom - cornerRadius, rect.right, rect.bottom);
            canvas.drawRect(bottomRect, backgroundPaint);
        } else if (event.startsOnEarlierDayAndEndsOnLaterDay(originalEvent)) {
            RectF topRect = new RectF(rect.left, rect.top, rect.right, rect.top + cornerRadius);
            canvas.drawRect(topRect, backgroundPaint);

            RectF bottomRect = new RectF(rect.left, rect.bottom - cornerRadius, rect.right, rect.bottom);
            canvas.drawRect(bottomRect, backgroundPaint);
        }
    }

    private Paint getBackgroundPaint() {
        final Paint paint = new Paint();
        paint.setColor(event.getColorOrDefault());
        return paint;
    }

    private void calculateTextHeightAndDrawTitle(WeekViewConfigWrapper config, Canvas canvas) {
        final boolean negativeWidth = (rect.right - rect.left - config.getEventPadding() * 2) < 0;
        final boolean negativeHeight = (rect.bottom - rect.top - config.getEventPadding() * 2) < 0;
        if (negativeWidth || negativeHeight) {
            return;
        }

        // Prepare the name of the event.
        final SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        if (event.getTitle() != null) {
            stringBuilder.append(event.getTitle());
            stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, stringBuilder.length(), 0);
        }

        // Prepare the location of the event.
        if (event.getLocation() != null) {
            stringBuilder.append(' ');
            stringBuilder.append(event.getLocation());
        }

        final int availableHeight = (int) (rect.bottom - rect.top - config.getEventPadding() * 2);
        final int availableWidth = (int) (rect.right - rect.left - config.getEventPadding() * 2);

        // Get text dimensions.
        final TextPaint textPaint = config.getEventTextPaint();

        textPaint.setColor(event.getTextColorOrDefault(config));

        StaticLayout textLayout = new StaticLayout(stringBuilder,
                textPaint, availableWidth, ALIGN_NORMAL, 1.0f, 0.0f, false);

        final int lineHeight = textLayout.getHeight() / textLayout.getLineCount();

        if (availableHeight >= lineHeight) {
            int availableLineCount = availableHeight / lineHeight;
            do {
                // TODO: Don't truncate
                // Ellipsize text to fit into event rect.
                final int availableArea = availableLineCount * availableWidth;
                final CharSequence ellipsized = TextUtils.ellipsize(stringBuilder,
                        textPaint, availableArea, TextUtils.TruncateAt.END);

                final int width = (int) (rect.right - rect.left - config.getEventPadding() * 2);
                textLayout = new StaticLayout(ellipsized, textPaint, width, ALIGN_NORMAL, 1.0f, 0.0f, false);

                // Repeat until text is short enough.
                availableLineCount--;
            } while (textLayout.getHeight() > availableHeight);

            // Draw text.
            drawEventTitle(config, textLayout, canvas);
        }
    }

    private void drawEventTitle(WeekViewConfigWrapper config, StaticLayout textLayout, Canvas canvas) {
        canvas.save();
        canvas.translate(rect.left + config.getEventPadding(), rect.top + config.getEventPadding());
        textLayout.draw(canvas);
        canvas.restore();
    }

    boolean isHit(MotionEvent e) {
        if (rect == null) {
            return false;
        }

        return e.getX() > rect.left
                && e.getX() < rect.right
                && e.getY() > rect.top
                && e.getY() < rect.bottom;
    }

}

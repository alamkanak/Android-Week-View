package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.MotionEvent;

import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.model.WeekViewEvent;

/**
 * A class to hold reference to the events and their visual representation. An EventRect is
 * actually the rectangle that is drawn on the calendar for a given event. There may be more
 * than one rectangle for a single event (an event that expands more than one day). In that
 * case two instances of the EventRect will be used for a single event. The given event will be
 * stored in "originalEvent". But the event that corresponds to rectangle the rectangle
 * instance will be stored in "event".
 */
public class EventChip {

    public WeekViewEvent event;
    public WeekViewEvent originalEvent;

    public RectF rect;
    public float left;
    public float width;
    public float top;
    public float bottom;

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
    public EventChip(WeekViewEvent event, WeekViewEvent originalEvent, RectF rect) {
        this.event = event;
        this.rect = rect;
        this.originalEvent = originalEvent;
    }

    public void draw(WeekViewConfig config, Canvas canvas) {
        float cornerRadius = config.eventCornerRadius;
        Paint backgroundPaint = getBackgroundPaint(); // TODO: Get stuff out DrawingConfig
        //config.drawingConfig.setEventBackgroundColorOrDefault(event);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, backgroundPaint);
        drawEventTitle(config, canvas);
    }

    private Paint getBackgroundPaint() {
        Paint paint = new Paint();
        paint.setColor(event.getColorOrDefault());
        return paint;
    }

    private void drawEventTitle(WeekViewConfig config, Canvas canvas) {
        boolean negativeWidth = (rect.right - rect.left - config.eventPadding * 2) < 0;
        boolean negativeHeight = (rect.bottom - rect.top - config.eventPadding * 2) < 0;
        if (negativeWidth || negativeHeight) {
            return;
        }

        // Prepare the name of the event.
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        if (event.getTitle() != null) {
            stringBuilder.append(event.getTitle());
            stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, stringBuilder.length(), 0);
        }

        // TODO: Don't do work twice!
        // Prepare the location of the event.
        if (event.getLocation() != null) {
            stringBuilder.append(' ');
            stringBuilder.append(event.getLocation());
        }

        int availableHeight = (int) (rect.bottom - rect.top - config.eventPadding * 2);
        int availableWidth = (int) (rect.right - rect.left - config.eventPadding * 2);

        // TODO: Code quality
        // Get text dimensions.
        StaticLayout textLayout = new StaticLayout(stringBuilder, config.drawingConfig.eventTextPaint, availableWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        int lineHeight = textLayout.getHeight() / textLayout.getLineCount();

        if (availableHeight >= lineHeight) {
            // Calculate available number of line counts.
            int availableLineCount = availableHeight / lineHeight;
            do {
                // TODO: Code quality
                // TODO: Don't truncate
                // Ellipsize text to fit into event rect.
                int availableArea = availableLineCount * availableWidth;
                CharSequence ellipsized = TextUtils.ellipsize(stringBuilder, config.drawingConfig.eventTextPaint, availableArea, TextUtils.TruncateAt.END);
                textLayout = new StaticLayout(ellipsized, config.drawingConfig.eventTextPaint, (int) (rect.right - rect.left - config.eventPadding * 2), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

                // Reduce line count.
                availableLineCount--;

                // Repeat until text is short enough.
            } while (textLayout.getHeight() > availableHeight);

            // Draw text.
            canvas.save();
            canvas.translate(rect.left + config.eventPadding, rect.top + config.eventPadding);
            textLayout.draw(canvas);
            canvas.restore();
        }
    }

    public boolean isHit(MotionEvent e) {
        if (rect == null) {
            return false;
        }

        return e.getX() > rect.left
                && e.getX() < rect.right
                && e.getY() > rect.top
                && e.getY() < rect.bottom;
    }

}

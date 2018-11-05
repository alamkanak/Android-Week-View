package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.StyleSpan;

import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.model.WeekViewEvent;
import com.alamkanak.weekview.ui.WeekView;

import java.util.Calendar;
import java.util.List;

import static android.text.Layout.Alignment.ALIGN_NORMAL;

public class EventsDrawer<T> {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawingConfig;
    private EventChipRectCalculator rectCalculator;

    public EventsDrawer(WeekViewConfig config) {
        this.config = config;
        this.drawingConfig = config.drawingConfig;
        this.rectCalculator = new EventChipRectCalculator(config);
    }

    public void drawSingleEvents(List<EventChip<T>> eventChips,
                                 DrawingContext drawingContext, Canvas canvas) {
        float startPixel = drawingContext.startPixel;

        // Draw single events
        for (Calendar day : drawingContext.dayRange) {
            if (config.isSingleDay()) {
                // Add a margin at the start if we're in day view. Otherwise, screen space is too
                // precious and we refrain from doing so.
                startPixel = startPixel + config.eventMarginHorizontal;
            }

            drawEventsForDate(eventChips, day, startPixel, canvas);

            // In the next iteration, start from the next day.
            startPixel += drawingConfig.widthPerDay + config.columnGap;
        }
    }

    private void drawEventsForDate(List<EventChip<T>> eventChips, Calendar date,
                                   float startFromPixel, Canvas canvas) {
        if (eventChips == null) {
            return;
        }

        for (int i = 0; i < eventChips.size(); i++) {
            EventChip eventChip = eventChips.get(i);
            WeekViewEvent event = eventChip.event;
            if (!event.isSameDay(date)) {
                continue;
            }

            RectF chipRect = rectCalculator.calculateSingleEvent(eventChip, startFromPixel);
            if (isValidSingleEventRect(chipRect)) {
                eventChip.rect = chipRect;
                eventChip.draw(config, canvas);
            } else {
                eventChip.rect = null;
            }
        }
    }

    /**
     * Draw all the all-day events of a particular day.
     *
     * @param eventChips The list of {@link EventChip}s to draw
     * @param drawingContext The {@link DrawingContext} to use for drawing
     * @param canvas         The canvas to draw upon.
     */
    public void drawAllDayEvents(List<EventChip<T>> eventChips,
                                 DrawingContext drawingContext, Canvas canvas) {
        if (eventChips == null) {
            return;
        }

        float startPixel = drawingContext.startPixel;

        for (Calendar day : drawingContext.dayRange) {
            if (config.isSingleDay()) {
                startPixel = startPixel + config.eventMarginHorizontal;
            }

            for (EventChip eventChip : eventChips) {
                WeekViewEvent event = eventChip.event;
                if (!event.isSameDay(day)) {
                    continue;
                }

                drawAllDayEvent(eventChip, startPixel, canvas);
            }

            startPixel += drawingConfig.widthPerDay + config.columnGap;
        }

        // Hide events when they are in the top left corner
        final Paint headerBackground = drawingConfig.headerBackgroundPaint;

        float headerRowBottomLine = 0;
        if (config.showHeaderRowBottomLine) {
            headerRowBottomLine = config.headerRowBottomLineWidth;
        }

        final float height = drawingConfig.headerHeight + config.headerRowPadding * 2 - headerRowBottomLine;
        final float width = drawingConfig.timeTextWidth + config.headerColumnPadding * 2;

        canvas.clipRect(0, 0, width, height);
        canvas.drawRect(0, 0, width, height, headerBackground);

        canvas.restore();
        canvas.save();
    }

    private void drawAllDayEvent(EventChip eventChip, float startFromPixel, Canvas canvas) {
        final RectF chipRect = rectCalculator.calculateAllDayEvent(eventChip, startFromPixel);
        if (isValidAllDayEventRect(chipRect)) {
            eventChip.rect = chipRect;

            final StaticLayout textLayout = calculateChipTextLayout(eventChip);
            if (textLayout != null) {
                eventChip.draw(config, textLayout, canvas);
            }
        } else {
            eventChip.rect = null;
        }
    }

    private boolean isValidSingleEventRect(RectF rect) {
        final float totalHeaderHeight = drawingConfig.headerHeight
                + config.headerRowPadding * 2
                + drawingConfig.headerMarginBottom;

        return rect.left < rect.right
                && rect.left < WeekView.getViewWidth()
                && rect.top < WeekView.getViewHeight()
                && rect.right > drawingConfig.headerColumnWidth
                && rect.bottom > totalHeaderHeight;
    }

    private boolean isValidAllDayEventRect(RectF rect) {
        return rect.left < rect.right
                && rect.left < WeekView.getViewWidth()
                && rect.top < WeekView.getViewHeight()
                && rect.right > drawingConfig.headerColumnWidth
                && rect.bottom > 0;
    }

    @Nullable
    private StaticLayout calculateChipTextLayout(EventChip eventChip) {
        final WeekViewEvent event = eventChip.event;
        final float left = eventChip.rect.left;
        final float top = eventChip.rect.top;
        final float right = eventChip.rect.right;
        final float bottom = eventChip.rect.bottom;

        final boolean negativeWidth = (right - left - config.eventPadding * 2) < 0;
        final boolean negativeHeight = (bottom - top - config.eventPadding * 2) < 0;
        if (negativeWidth || negativeHeight) {
            return null;
        }

        // Prepare the name of the event.
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        if (event.getTitle() != null) {
            stringBuilder.append(event.getTitle());
            stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, stringBuilder.length(), 0);
        }

        // Prepare the location of the event.
        if (event.getLocation() != null) {
            stringBuilder.append(' ');
            stringBuilder.append(event.getLocation());
        }

        final int availableHeight = (int) (bottom - top - config.eventPadding * 2);
        final int availableWidth = (int) (right - left - config.eventPadding * 2);

        // Get text dimensions.
        final TextPaint textPaint = drawingConfig.eventTextPaint;
        StaticLayout textLayout = new StaticLayout(
                stringBuilder, textPaint, availableWidth, ALIGN_NORMAL, 1.0f, 0.0f, false);

        final int lineHeight = textLayout.getHeight() / textLayout.getLineCount();

        if (availableHeight >= lineHeight) {
            int availableLineCount = availableHeight / lineHeight;
            do {
                // Ellipsize text to fit into event rect.
                final int availableArea = availableLineCount * availableWidth;
                CharSequence ellipsized = TextUtils.ellipsize(stringBuilder, textPaint, availableArea, TruncateAt.END);
                final int width = (int) (right - left - config.eventPadding * 2);
                textLayout = new StaticLayout(ellipsized, textPaint, width, ALIGN_NORMAL, 1.0f, 0.0f, false);

                // Reduce line count.
                availableLineCount--;

                // Repeat until text is short enough.
            } while (textLayout.getHeight() > availableHeight);
        }

        final int chipHeight = lineHeight + (config.eventPadding * 2);
        eventChip.rect.bottom = eventChip.rect.top + chipHeight;
        return textLayout;
    }

}

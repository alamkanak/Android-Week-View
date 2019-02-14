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
import android.util.Pair;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.text.Layout.Alignment.ALIGN_NORMAL;

class EventsDrawer<T> {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawingConfig;
    private EventChipRectCalculator rectCalculator;

    EventsDrawer(WeekViewConfig config) {
        this.config = config;
        this.drawingConfig = config.drawingConfig;
        this.rectCalculator = new EventChipRectCalculator(config);
    }

    void drawSingleEvents(List<EventChip<T>> eventChips,
                          DrawingContext drawingContext, Canvas canvas) {
        float startPixel = drawingContext.getStartPixel();

        // Draw single events
        for (Calendar day : drawingContext.getDateRange()) {
            if (config.isSingleDay()) {
                // Add a margin at the start if we're in day view. Otherwise, screen space is too
                // precious and we refrain from doing so.
                startPixel = startPixel + config.eventMarginHorizontal;
            }

            drawEventsForDate(eventChips, day, startPixel, canvas);

            // In the next iteration, start from the next day.
            startPixel += config.getTotalDayWidth();
        }
    }

    private void drawEventsForDate(List<EventChip<T>> eventChips, Calendar date,
                                   float startFromPixel, Canvas canvas) {
        if (eventChips == null) {
            return;
        }

        for (int i = 0; i < eventChips.size(); i++) {
            final EventChip eventChip = eventChips.get(i);
            final WeekViewEvent event = eventChip.event;
            if (!event.isSameDay(date)) {
                continue;
            }

            final RectF chipRect = rectCalculator.calculateSingleEvent(eventChip, startFromPixel);
            if (isValidSingleEventRect(chipRect)) {
                eventChip.rect = chipRect;
                eventChip.draw(config, canvas);
            } else {
                eventChip.rect = null;
            }
        }
    }

    /**
     * Compute the StaticLayout for all-day events to update the header height
     *
     * @param eventChips The list of {@link EventChip}s to draw
     * @param drawingContext The {@link DrawingContext} to use for drawing
     * @return The association of {@link EventChip}s with his StaticLayout
     */
    List<Pair<EventChip<T>, StaticLayout>> prepareDrawAllDayEvents(List<EventChip<T>> eventChips,
                                                                   DrawingContext drawingContext) {
        drawingConfig.setCurrentAllDayEventHeight(0, config);
        if (eventChips == null) {
            return null;
        }

        List<Pair<EventChip<T>, StaticLayout>> result = new ArrayList<>();
        float startPixel = drawingContext.getStartPixel();

        for (Calendar day : drawingContext.getDateRange()) {
            if (config.isSingleDay()) {
                startPixel = startPixel + config.eventMarginHorizontal;
            }

            for (EventChip<T> eventChip : eventChips) {
                final WeekViewEvent event = eventChip.event;
                if (!event.isSameDay(day)) {
                    continue;
                }

                StaticLayout layout = prepareDrawAllDayEvent(eventChip, startPixel);
                if (layout != null) {
                    result.add(new Pair<>(eventChip, layout));
                }
            }

            startPixel += config.getTotalDayWidth();
        }

        return result;
    }

    private StaticLayout prepareDrawAllDayEvent(EventChip eventChip, float startFromPixel) {
        final RectF chipRect = rectCalculator.calculateAllDayEvent(eventChip, startFromPixel);
        if (isValidAllDayEventRect(chipRect)) {
            eventChip.rect = chipRect;
            return calculateChipTextLayout(eventChip);
        } else {
            eventChip.rect = null;
        }
        return null;
    }

    /**
     * Draw all the all-day events of a particular day.
     *
     * @param eventChips The list of Pair<{@link EventChip}, StaticLayout>s to draw
     * @param canvas         The canvas to draw upon.
     */
    void drawAllDayEvents(List<Pair<EventChip<T>, StaticLayout>> eventChips,
                          Canvas canvas) {
        if (eventChips == null) {
            return;
        }

        for (Pair<EventChip<T>, StaticLayout> pair : eventChips) {
            EventChip<T> eventChip = pair.first;
            StaticLayout layout = pair.second;
            eventChip.draw(config, layout, canvas);
        }

        // Hide events when they are in the top left corner
        final Paint headerBackground = drawingConfig.headerBackgroundPaint;

        float headerRowBottomLine = config.showHeaderRowBottomLine ? config.headerRowBottomLineWidth : 0;

        final float height = drawingConfig.headerHeight - headerRowBottomLine;
        final float width = drawingConfig.timeTextWidth + config.timeColumnPadding * 2;

        canvas.clipRect(0, 0, width, height);
        canvas.drawRect(0, 0, width, height, headerBackground);

        canvas.restore();
        canvas.save();
    }

    private boolean isValidSingleEventRect(RectF rect) {
        return rect.left < rect.right
                && rect.left < WeekView.getViewWidth()
                && rect.top < WeekView.getViewHeight()
                && rect.right > drawingConfig.timeColumnWidth
                && rect.bottom > drawingConfig.headerHeight;
    }

    private boolean isValidAllDayEventRect(RectF rect) {
        return rect.left < rect.right
                && rect.left < WeekView.getViewWidth()
                && rect.top < WeekView.getViewHeight()
                && rect.right > drawingConfig.timeColumnWidth
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

        final int availableWidth = (int) (right - left - config.eventPadding * 2);

        // Get text dimensions.
        final TextPaint textPaint = (event.isAllDay()) ? drawingConfig.allDayEventTextPaint
                                                       : drawingConfig.eventTextPaint;
        textPaint.setColor(event.getTextColorOrDefault(config));
        StaticLayout textLayout = new StaticLayout(
                stringBuilder, textPaint, availableWidth, ALIGN_NORMAL, 1.0f, 0.0f, false);

        final int lineHeight = textLayout.getHeight() / textLayout.getLineCount();

        // For an all day event, we display just one line
        final int chipHeight = lineHeight + (config.eventPadding * 2);
        eventChip.rect.bottom = eventChip.rect.top + chipHeight;

        // Compute the available height on the right size of the chip
        final int availableHeight = (int) (eventChip.rect.bottom - top - config.eventPadding * 2);

        if (availableHeight >= lineHeight) {
            int availableLineCount = availableHeight / lineHeight;
            do {
                // Ellipsize text to fit into event rect.
                final int availableArea = availableLineCount * availableWidth;
                final CharSequence ellipsized =
                        TextUtils.ellipsize(stringBuilder, textPaint, availableArea, TextUtils.TruncateAt.END);
                final int width = (int) (right - left - config.eventPadding * 2);
                textLayout = new StaticLayout(ellipsized, textPaint, width, ALIGN_NORMAL, 1.0f, 0.0f, false);

                // Reduce line count.
                availableLineCount--;

                // Repeat until text is short enough.
            } while (textLayout.getHeight() > availableHeight);
        }

        // Refresh the header height
        if (chipHeight > drawingConfig.getCurrentAllDayEventHeight()) {
            drawingConfig.setCurrentAllDayEventHeight(chipHeight, config);
        }

        return textLayout;
    }

}

package com.alamkanak.weekview;

import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.TextPaint;

import com.alamkanak.weekview.date.DateUtils2;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.alamkanak.weekview.Constants.MINUTES_PER_HOUR;

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 */
public class WeekViewEvent<T> implements WeekViewDisplayable, Comparable<WeekViewEvent> {

    private long id;
    private String title;
    private Calendar startTime;
    private Calendar endTime;
    private String location;
    private boolean isAllDay;
    private Style style;
    private T data;

    /**
     * @deprecated
     * Use {@link WeekViewEvent.Builder} instead to construct {@link WeekViewEvent}
     */
    @Deprecated
    public WeekViewEvent() {
        // Free ad space
    }

    /**
     * Initializes the event for week view.
     *
     * @deprecated
     * Use {@link WeekViewEvent.Builder} instead to construct {@link WeekViewEvent}
     *
     * @param id The id of the event.
     * @param title Name of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     */
    @Deprecated
    public WeekViewEvent(long id, String title, Calendar startTime, Calendar endTime) {
        this(id, title, startTime, endTime, null, false);
    }

    /**
     * Initializes the event for week view.
     *
     * @deprecated
     * Use {@link WeekViewEvent.Builder} instead to construct {@link WeekViewEvent}
     *
     * @param id The id of the event.
     * @param title Name of the event.
     * @param location The location of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     * @param isAllDay Is the event an all day event.
     */
    @Deprecated
    public WeekViewEvent(long id, String title, Calendar startTime,
                         Calendar endTime, String location, boolean isAllDay) {
        this(id, title, startTime, endTime, location, 0, isAllDay, null);
    }

    /**
     * @deprecated
     * Use {@link WeekViewEvent.Builder} instead to construct {@link WeekViewEvent}
     */
    @Deprecated
    public WeekViewEvent(long id, String title, Calendar startTime, Calendar endTime,
                         String location, int color, boolean isAllDay, T data) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.style = new Style.Builder().setBackgroundColor(color).build();
        this.isAllDay = isAllDay;
        this.data = data;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    int getEffectiveStartMinutes(WeekViewConfigWrapper config) {
        final int startHour = DateUtils2.getHour(startTime) - config.getMinHour();
        return startHour * MINUTES_PER_HOUR + DateUtils2.getMinute(startTime);
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    int getEffectiveEndMinutes(WeekViewConfigWrapper config) {
        final int endHour = DateUtils2.getHour(endTime) - config.getMinHour();
        return endHour * MINUTES_PER_HOUR + DateUtils2.getMinute(endTime);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public Style getStyle() {
        return style;
    }

    boolean isTextStrikeThrough() {
        return style.textStrikeThrough;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getColor() {
        return style.backgroundColor;
    }

    int getColorOrDefault(WeekViewConfigWrapper config) {
        return (style.backgroundColor != 0) ? style.backgroundColor : config.getDefaultEventColor();
    }

    public void setColor(int color) {
        style.backgroundColor = color;
    }

    boolean isAllDay() {
        return isAllDay;
    }

    boolean isNotAllDay() {
        return !isAllDay;
    }

    public void setIsAllDay(boolean allDay) {
        this.isAllDay = allDay;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    boolean isSameDay(Calendar other) {
        return DateUtils2.isSameDate(this.getStartTime(), other);
    }

    boolean isWithin(int minHour, int maxHour) {
        return DateUtils2.getHour(startTime) >= minHour && DateUtils2.getHour(endTime) <= maxHour;
    }

    public int getTextColor() {
        return style.textColor;
    }

    public void setTextColor(int textColor) {
        style.textColor = textColor;
    }

    public int getTextColorOrDefault(WeekViewConfigWrapper config) {
        return (style.textColor != 0) ? style.textColor : config.getEventTextPaint().getColor();
    }

    boolean hasBorder() {
        return style.borderWidth > 0;
    }

    int getBorderWidth() {
        return style.borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        style.borderWidth = borderWidth;
    }

    int getBorderColor() {
        return style.borderColor;
    }

    public void setBorderColor(int borderColor) {
        style.borderColor = borderColor;
    }

    TextPaint getTextPaint(WeekViewConfigWrapper config) {
        final TextPaint textPaint;

        if (isAllDay) {
            textPaint = config.getAllDayEventTextPaint();
        } else {
            textPaint = config.getEventTextPaint();
        }

        textPaint.setColor(getTextColorOrDefault(config));

        if (style.textStrikeThrough) {
            textPaint.setFlags(textPaint.getFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        return textPaint;
    }

    boolean collidesWith(WeekViewEvent other) {
        final boolean sameStart = DateUtils2.isEqual(startTime, other.startTime);
        final boolean sameEnd = DateUtils2.isEqual(endTime, other.endTime);
        if (sameStart && sameEnd) {
            // Complete overlap
            return true;
        }

        // Resolve collisions by shortening the preceding event by 1 ms
        if (DateUtils2.isEqual(endTime, other.startTime)) {
            endTime = DateUtils2.minusMillis(endTime, 1);
            return false;
        } else if (DateUtils2.isEqual(startTime, other.endTime)) {
            other.endTime = DateUtils2.minusMillis(other.endTime, 1);
        }

        return !DateUtils2.isAfter(startTime, other.endTime)
                && DateUtils2.isBefore(endTime, other.startTime);
    }

    @Override
    public int compareTo(@NonNull WeekViewEvent other) {
        int comparator = startTime.compareTo(other.startTime);
        if (comparator == 0) {
            comparator = endTime.compareTo(other.endTime);
        }
        return comparator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WeekViewEvent that = (WeekViewEvent) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    boolean startsOnEarlierDay(WeekViewEvent<T> originalEvent) {
        return !DateUtils2.isEqual(startTime, originalEvent.startTime);
    }

    boolean endsOnLaterDay(WeekViewEvent<T> originalEvent) {
        return !DateUtils2.isEqual(endTime, originalEvent.endTime);
    }

    @Override
    public String toString() {
        DateFormat sdf = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        return "WeekViewEvent{" +
                "title='" + title + '\'' +
                ", startTime=" + sdf.format(startTime.toString()) +
                ", endTime=" + sdf.format(endTime.toString()) +
                '}';
    }

    @NonNull
    @Override
    public WeekViewEvent<T> toWeekViewEvent() {
        return this;
    }

    public static class Style {

        private int backgroundColor;
        private int textColor;
        private boolean textStrikeThrough;
        private int borderWidth;
        private int borderColor;

        public static class Builder {

            private Style style;

            public Builder() {
                style = new Style();
            }

            public Builder setBackgroundColor(@ColorInt int color) {
                style.backgroundColor = color;
                return this;
            }

            public Builder setTextColor(@ColorInt int color) {
                style.textColor = color;
                return this;
            }

            public Builder setTextStrikeThrough(boolean strikeThrough) {
                style.textStrikeThrough = strikeThrough;
                return this;
            }

            public Builder setBorderWidth(int width) {
                style.borderWidth = width;
                return this;
            }

            public Builder setBorderColor(@ColorInt int color) {
                style.borderColor = color;
                return this;
            }

            public Style build() {
                return style;
            }

        }

    }

    public static class Builder<T> {

        private WeekViewEvent<T> event;

        public Builder() {
            event = new WeekViewEvent<>();
        }

        public Builder<T> setId(long id) {
            event.id = id;
            return this;
        }

        public Builder<T> setTitle(@NonNull String title) {
            event.title = title;
            return this;
        }

        public Builder<T> setStartTime(@NonNull Calendar startTime) {
            event.startTime = startTime;
            return this;
        }

        public Builder<T> setEndTime(@NonNull Calendar endTime) {
            event.endTime = endTime;
            return this;
        }

        public Builder<T> setLocation(String location) {
            event.location = location;
            return this;
        }

        public Builder<T> setStyle(WeekViewEvent.Style style) {
            event.style = style;
            return this;
        }

        public Builder<T> setAllDay(boolean isAllDay) {
            event.isAllDay = isAllDay;
            return this;
        }

        public Builder<T> setData(T data) {
            event.data = data;
            return this;
        }

        public WeekViewEvent<T> build() {
            return event;
        }

    }

}

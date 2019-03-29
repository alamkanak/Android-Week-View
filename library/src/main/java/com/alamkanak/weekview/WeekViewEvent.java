package com.alamkanak.weekview;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.alamkanak.weekview.Constants.MINUTES_PER_HOUR;
import static java.util.Calendar.DATE;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 */
public class WeekViewEvent<T> implements WeekViewDisplayable, Comparable<WeekViewEvent> {

    private long id;
    private String title;
    private Calendar startTime;
    private Calendar endTime;
    private String location;
    private int color;
    private boolean isAllDay;
    private int textColor = 0;

    private int borderWidth;
    private int borderColor;

    private T data;

    public WeekViewEvent() {
        // Free ad space
    }

    /**
     * Initializes the event for week view.
     * @param id The id of the event.
     * @param title Name of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     */
    public WeekViewEvent(long id, String title, Calendar startTime, Calendar endTime) {
        this(id, title, startTime, endTime, null, false);
    }

    /**
     * Initializes the event for week view.
     * @param id The id of the event.
     * @param title Name of the event.
     * @param location The location of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     */
    private WeekViewEvent(long id, String title, Calendar startTime, Calendar endTime, String location) {
        this(id, title, startTime, endTime, location, false);
    }

    /**
     * Initializes the event for week view.
     * @param id The id of the event.
     * @param title Name of the event.
     * @param location The location of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     * @param isAllDay Is the event an all day event.
     */
    public WeekViewEvent(long id, String title, Calendar startTime,
                         Calendar endTime, String location, boolean isAllDay) {
        this(id, title, startTime, endTime, location, 0, isAllDay, null);
    }

    public WeekViewEvent(long id, String title, Calendar startTime, Calendar endTime,
                         String location, int color, boolean isAllDay, T data) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.color = color;
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
        final int startHour = startTime.get(HOUR_OF_DAY) - config.getMinHour();
        return startHour * MINUTES_PER_HOUR + startTime.get(MINUTE);
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    int getEffectiveEndMinutes(WeekViewConfigWrapper config) {
        final int endHour = endTime.get(HOUR_OF_DAY) - config.getMinHour();
        return endHour * MINUTES_PER_HOUR + endTime.get(MINUTE);
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

    public void setLocation(String location) {
        this.location = location;
    }

    public int getColor() {
        return color;
    }

    public int getColorOrDefault() {
        return (color != 0) ? color : Defaults.EVENT_COLOR;
    }

    public void setColor(int color) {
        this.color = color;
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
        return DateUtils.isSameDay(startTime, other);
    }

    boolean isSameDay(WeekViewEvent other) {
        return DateUtils.isSameDay(startTime, other.startTime);
    }

    boolean isWithin(int minHour, int maxHour) {
        return startTime.get(HOUR_OF_DAY) >= minHour && endTime.get(HOUR_OF_DAY) <= maxHour;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getTextColorOrDefault(WeekViewConfigWrapper config) {
        return (textColor != 0) ? textColor : config.getEventTextPaint().getColor();
    }

    boolean hasBorder() {
        return borderWidth > 0;
    }

    int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    boolean collidesWith(WeekViewEvent other) {
        long thisStart = startTime.getTimeInMillis();
        long thisEnd = endTime.getTimeInMillis();
        long otherStart = other.getStartTime().getTimeInMillis();
        long otherEnd = other.getEndTime().getTimeInMillis();
        return !((thisStart >= otherEnd) || (thisEnd <= otherStart));
    }

    @Override
    public int compareTo(@NonNull WeekViewEvent other) {
        Long thisStart = this.getStartTime().getTimeInMillis();
        Long otherStart = other.getStartTime().getTimeInMillis();

        int comparator = thisStart.compareTo(otherStart);
        if (comparator == 0) {
            Long thisEnd = this.getEndTime().getTimeInMillis();
            Long otherEnd = other.getEndTime().getTimeInMillis();
            comparator = thisEnd.compareTo(otherEnd);
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
        return getStartTime().get(DATE) != originalEvent.getStartTime().get(DATE);
    }

    boolean endsOnLaterDay(WeekViewEvent<T> originalEvent) {
        return getEndTime().get(DATE) != originalEvent.getEndTime().get(DATE);
    }

    @Override
    public String toString() {
        DateFormat sdf = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        return "WeekViewEvent{" +
                "title='" + title + '\'' +
                ", startTime=" + sdf.format(startTime.getTime()) +
                ", endTime=" + sdf.format(endTime.getTime()) +
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
            event.color = style.backgroundColor;
            event.textColor = style.textColor;
            event.borderWidth = style.borderWidth;
            event.borderColor = style.borderColor;
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

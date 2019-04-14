package com.alamkanak.weekview;

import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.TextPaint;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.alamkanak.weekview.Constants.MINUTES_PER_HOUR;
import static com.alamkanak.weekview.DateUtils.toCalendar;
import static com.alamkanak.weekview.DateUtils.toZonedDateTime;

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 */
public class WeekViewEvent<T> implements WeekViewDisplayable, Comparable<WeekViewEvent> {

    private long id;
    private String title;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
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
        this.startTime = toZonedDateTime(startTime);
        this.endTime = toZonedDateTime(endTime);
        this.location = location;
        this.style = new Style.Builder().setBackgroundColor(color).build();
        this.isAllDay = isAllDay;
        this.data = data;
    }

    public Calendar getStartTime() {
        return toCalendar(startTime);
    }

    public ZonedDateTime getStartDateTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = toZonedDateTime(startTime);
    }

    public void setStartDateTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    int getEffectiveStartMinutes(WeekViewConfigWrapper config) {
        final int startHour = startTime.getHour() - config.getMinHour();
        return startHour * MINUTES_PER_HOUR + startTime.getMinute();
    }

    public Calendar getEndTime() {
        return toCalendar(endTime);
    }

    public ZonedDateTime getEndDateTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = toZonedDateTime(endTime);
    }

    public void setEndDateTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }

    int getEffectiveEndMinutes(WeekViewConfigWrapper config) {
        final int endHour = endTime.getHour() - config.getMinHour();
        return endHour * MINUTES_PER_HOUR + endTime.getMinute();
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

    public int getColorOrDefault() {
        return (style.backgroundColor != 0) ? style.backgroundColor : Defaults.EVENT_COLOR;
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

    boolean isSameDay(LocalDate other) {
        return startTime.toLocalDate().isEqual(other);
    }

    boolean isSameDay(WeekViewEvent other) {
        return DateUtils.isSameDate(startTime, other.startTime);
    }

    boolean isWithin(int minHour, int maxHour) {
        return startTime.getHour() >= minHour && endTime.getHour() <= maxHour;
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
        /*long thisStart = startTime.getTimeInMillis();
        long thisEnd = endTime.getTimeInMillis();
        long otherStart = other.getStartTime().getTimeInMillis();
        long otherEnd = other.getEndTime().getTimeInMillis();*/
        if (startTime.isEqual(other.startTime) && endTime.isEqual(other.endTime)) {
            // Complete overlap
            return true;
        }

        // Resolve collisions by reducing the following event by 1 ms
        if (endTime.isEqual(other.startTime)) {
            endTime = endTime.minus(1, ChronoUnit.MILLIS);
            return false;
        } else if (startTime.isEqual(other.endTime)) {
            other.endTime = other.endTime.minus(1, ChronoUnit.MILLIS);
        }

        return !startTime.isAfter(other.endTime) && !endTime.isBefore(other.startTime);

        /*return startTime.toEpochSecond() <= other.endTime.toEpochSecond()
                && endTime.toEpochSecond() >= other.startTime.toEpochSecond();*/

        // return startTime.isBefore(other.endTime) || endTime.isAfter(other.startTime);
        // return !(thisStart >= otherEnd) && !(thisEnd <= otherStart);
        // return !((thisStart >= otherEnd) || (thisEnd <= otherStart));
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
        return !startTime.toLocalDate().isEqual(originalEvent.startTime.toLocalDate());
    }

    boolean endsOnLaterDay(WeekViewEvent<T> originalEvent) {
        return !endTime.toLocalDate().isEqual(originalEvent.endTime.toLocalDate());
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
            event.startTime = toZonedDateTime(startTime);
            return this;
        }

        public Builder<T> setEndTime(@NonNull Calendar endTime) {
            event.endTime = toZonedDateTime(endTime);
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

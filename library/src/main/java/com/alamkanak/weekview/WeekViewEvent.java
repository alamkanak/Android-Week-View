package com.alamkanak.weekview;

import android.graphics.Color;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.alamkanak.weekview.DateUtils.isAtStartOfNewDay;
import static com.alamkanak.weekview.DateUtils.withTimeAtEndOfDay;

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://april-shower.com
 */
public class WeekViewEvent<T> implements WeekViewDisplayable, Comparable<WeekViewEvent> {

    private static final int DEFAULT_COLOR = Color.parseColor("#9fc6e7");

    private long id;
    private String title;
    private Calendar startTime;
    private Calendar endTime;
    private String location;
    private int color;
    private boolean isAllDay;

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

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
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
        return (color != 0) ? color : DEFAULT_COLOR;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isAllDay() {
        return isAllDay;
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

    public boolean isSameDay(Calendar other) {
        return DateUtils.isSameDay(startTime, other);
    }

    public boolean isSameDay(WeekViewEvent other) {
        return DateUtils.isSameDay(startTime, other.startTime);
    }

    public boolean collidesWith(WeekViewEvent other) {
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

    /**
     * Splits the {@link WeekViewEvent} by day into a list of {@link WeekViewEvent}s
     * @return A list of {@link WeekViewEvent}
     */
    List<WeekViewEvent<T>> splitWeekViewEvents() {
        List<WeekViewEvent<T>> events = new ArrayList<>();

        // Clone this end time for when we need to clone events
        Calendar newEndTime = (Calendar) this.endTime.clone();

        if (isAtStartOfNewDay(startTime, newEndTime)) {
            // Set end time to 1ms before midnight to ensure the EventRect will get drawn correctly
            WeekViewEvent<T> shortenedEvent = shortenTooLongAllDayEvent(newEndTime);
            events.add(shortenedEvent);
        } else if (!isSameDay(newEndTime)) {
            events = splitEventByDays(newEndTime);
        } else {
            events.add(this);
        }

        return events;
    }

    private WeekViewEvent<T> shortenTooLongAllDayEvent(Calendar newEndTime) {
        return new WeekViewEvent<>(id, title, startTime,
                withTimeAtEndOfDay(newEndTime), location, color, isAllDay, data);
    }

    private List<WeekViewEvent<T>> splitEventByDays(Calendar newEndTime) {
        List<WeekViewEvent<T>> results = new ArrayList<>();
        newEndTime = withTimeAtEndOfDay(newEndTime);

        WeekViewEvent<T> event1 = new WeekViewEvent<>(id, title,
                startTime, newEndTime, location, color, isAllDay, data);
        results.add(event1);

        // Add other days.
        Calendar otherDay = (Calendar) startTime.clone();
        otherDay.add(Calendar.DATE, 1);

        while (!DateUtils.isSameDay(otherDay, this.endTime)) {
            Calendar overDay = (Calendar) otherDay.clone();
            overDay.set(Calendar.HOUR_OF_DAY, 0);
            overDay.set(Calendar.MINUTE, 0);

            Calendar endOfOverDay = (Calendar) overDay.clone();
            endOfOverDay.set(Calendar.HOUR_OF_DAY, 23);
            endOfOverDay.set(Calendar.MINUTE, 59);

            WeekViewEvent<T> eventMore = new WeekViewEvent<>(id, title,
                    overDay, endOfOverDay, location, color, isAllDay, data);
            results.add(eventMore);

            // Add next day.
            otherDay.add(Calendar.DATE, 1);
        }

        // Add last day.
        Calendar startTime = (Calendar) this.endTime.clone();
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);

        WeekViewEvent<T> event2 = new WeekViewEvent<>(id, title,
                startTime, this.endTime, location, color, isAllDay, data);
        results.add(event2);

        return results;
    }

    @Override
    public WeekViewEvent<T> toWeekViewEvent() {
        return this;
    }

}

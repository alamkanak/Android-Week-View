package com.alamkanak.weekview.sample.apiclient;

import com.alamkanak.weekview.model.WeekViewDisplayable;
import com.alamkanak.weekview.model.WeekViewEvent;

import java.util.Calendar;

public class Event implements WeekViewDisplayable<Event> {

    private long id;
    private String title;
    private Calendar startTime;
    private Calendar endTime;
    private String location;
    private int color;
    private boolean isAllDay;

    public Event(long id, String title, Calendar startTime, Calendar endTime,
                 String location, int color, boolean isAllDay) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.color = color;
        this.isAllDay = isAllDay;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isAllDay() {
        return isAllDay;
    }

    public void setAllDay(boolean allDay) {
        isAllDay = allDay;
    }

    @Override
    public WeekViewEvent<Event> toWeekViewEvent() {
        return new WeekViewEvent<>(id, title, startTime, endTime, location, color, isAllDay, this);
    }

}

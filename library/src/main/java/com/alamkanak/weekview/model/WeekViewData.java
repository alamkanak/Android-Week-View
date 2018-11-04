package com.alamkanak.weekview.model;

import com.alamkanak.weekview.drawing.EventChip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeekViewData<T> {

    private List<EventChip<T>> eventChips;

    private List<EventChip<T>> normalEventChips;
    private List<EventChip<T>> allDayEventChips;

    public List<? extends WeekViewEvent<T>> previousPeriodEvents;
    public List<? extends WeekViewEvent<T>> currentPeriodEvents;
    public List<? extends WeekViewEvent<T>> nextPeriodEvents;

    public int fetchedPeriod = -1; // the middle period the calendar has fetched.

    public void setEventChips(List<EventChip<T>> eventChips) {
        this.eventChips = eventChips;
        normalEventChips = new ArrayList<>();
        allDayEventChips = new ArrayList<>();

        for (EventChip<T> eventChip : eventChips) {
            if (eventChip.event.isAllDay()) {
                allDayEventChips.add(eventChip);
            } else {
                normalEventChips.add(eventChip);
            }
        }
    }

    public List<EventChip<T>> getAllEventChips() {
        return eventChips;
    }

    public List<EventChip<T>> getNormalEventChips() {
        return normalEventChips;
    }

    public List<EventChip<T>> getAllDayEventChips() {
        return allDayEventChips;
    }

    public void clearEventChipsCache() {
        if (eventChips != null) {
            for (EventChip eventChip : eventChips) {
                eventChip.rect = null;
            }
        }
    }

    public void clear() {
        eventChips.clear();
        previousPeriodEvents = null;
        currentPeriodEvents = null;
        nextPeriodEvents = null;
        fetchedPeriod = -1;
    }

    /**
     * Sort and cache events.
     *
     * @param events The events to be sorted and cached.
     */
    public void sortAndCacheEvents(List<WeekViewEvent<T>> events) {
        sortEvents(events);
        for (WeekViewEvent<T> event : events) {
            cacheEvent(event);
        }
    }

    /**
     * Sorts the events in ascending order.
     *
     * @param events The events to be sorted.
     */
    private void sortEvents(List<WeekViewEvent<T>> events) {
        Collections.sort(events);
    }

    /**
     * Cache the event for smooth scrolling functionality.
     *
     * @param event The event to cache.
     */
    private void cacheEvent(WeekViewEvent<T> event) {
        if (event.getStartTime().compareTo(event.getEndTime()) >= 0) {
            return;
        }

        List<WeekViewEvent<T>> splittedEvents = event.splitWeekViewEvents();
        for (WeekViewEvent<T> splittedEvent : splittedEvents) {
            eventChips.add(new EventChip<>(splittedEvent, event, null));
        }
    }

}

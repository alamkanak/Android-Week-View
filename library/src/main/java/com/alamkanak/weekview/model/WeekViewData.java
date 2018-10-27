package com.alamkanak.weekview.model;

import com.alamkanak.weekview.drawing.EventChip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeekViewData {

    public List<EventChip> eventChips;

    public List<EventChip> normalEventChips;
    public List<EventChip> allDayEventChips;

    public List<? extends WeekViewEvent> previousPeriodEvents;
    public List<? extends WeekViewEvent> currentPeriodEvents;
    public List<? extends WeekViewEvent> nextPeriodEvents;

    public int fetchedPeriod = -1; // the middle period the calendar has fetched.

    public void clear() {
        eventChips.clear();
        previousPeriodEvents = null;
        currentPeriodEvents = null;
        nextPeriodEvents = null;
        fetchedPeriod = -1;
    }

    // TODO: Use this
    public void setEventChips(List<EventChip> eventChips) {
        normalEventChips = new ArrayList<>();
        allDayEventChips = new ArrayList<>();

        for (EventChip eventChip : eventChips) {
            if (eventChip.event.isAllDay()) {
                allDayEventChips.add(eventChip);
            } else {
                normalEventChips.add(eventChip);
            }
        }
    }

    /**
     * Sort and cache events.
     *
     * @param events The events to be sorted and cached.
     */
    public void sortAndCacheEvents(List<? extends WeekViewEvent> events) {
        sortEvents(events);
        for (WeekViewEvent event : events) {
            cacheEvent(event);
        }
    }

    /**
     * Sorts the events in ascending order.
     *
     * @param events The events to be sorted.
     */
    private void sortEvents(List<? extends WeekViewEvent> events) {
        Collections.sort(events);
    }

    /**
     * Cache the event for smooth scrolling functionality.
     *
     * @param event The event to cache.
     */
    private void cacheEvent(WeekViewEvent event) {
        if (event.getStartTime().compareTo(event.getEndTime()) >= 0) {
            return;
        }

        List<WeekViewEvent> splittedEvents = event.splitWeekViewEvents();
        for (WeekViewEvent splittedEvent : splittedEvents) {
            eventChips.add(new EventChip(splittedEvent, event, null));
        }
    }

}

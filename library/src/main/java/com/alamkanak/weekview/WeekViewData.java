package com.alamkanak.weekview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class WeekViewData<T> {

    private List<EventChip<T>> eventChips;

    private List<EventChip<T>> normalEventChips;
    private List<EventChip<T>> allDayEventChips;

    List<WeekViewEvent<T>> previousPeriodEvents;
    List<WeekViewEvent<T>> currentPeriodEvents;
    List<WeekViewEvent<T>> nextPeriodEvents;

    int fetchedPeriod = -1; // the middle period the calendar has fetched.

    void setEventChips(List<EventChip<T>> eventChips) {
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

    List<EventChip<T>> getAllEventChips() {
        return eventChips;
    }

    List<EventChip<T>> getNormalEventChips() {
        return normalEventChips;
    }

    List<EventChip<T>> getAllDayEventChips() {
        return allDayEventChips;
    }

    void clearEventChipsCache() {
        if (eventChips != null) {
            for (EventChip eventChip : eventChips) {
                eventChip.rect = null;
            }
        }
    }

    void clear() {
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
    void sortAndCacheEvents(List<WeekViewEvent<T>> events) {
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

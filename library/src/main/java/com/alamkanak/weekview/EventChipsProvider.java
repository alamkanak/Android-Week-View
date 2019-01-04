package com.alamkanak.weekview;

import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static java.lang.Math.abs;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;

class EventChipsProvider<T> {

    private WeekViewConfig config;
    private WeekViewData<T> data;
    private WeekViewLoader<T> weekViewLoader;
    private WeekViewViewState viewState;

    EventChipsProvider(WeekViewConfig config,
                              WeekViewData<T> data, WeekViewViewState viewState) {
        this.config = config;
        this.data = data;
        this.viewState = viewState;
    }

    void setWeekViewLoader(WeekViewLoader<T> weekViewLoader) {
        this.weekViewLoader = weekViewLoader;
    }

    void loadEventsIfNecessary(View view, List<Calendar> dayRange) {
        if (view.isInEditMode()) {
            return;
        }

        if (weekViewLoader == null) {
            throw new WeekViewException("No WeekViewLoader or MonthChangeListener provided. " +
                    "This is necessary to load new events");
        }

        for (Calendar day : dayRange) {
            final boolean hasNoEvents = data.getAllEventChips() == null;

            final boolean needsToFetchPeriod =
                    data.fetchedPeriod != weekViewLoader.toWeekViewPeriodIndex(day)
                    && abs(data.fetchedPeriod - weekViewLoader.toWeekViewPeriodIndex(day)) > 0.5;

            // Check if this particular day has been fetched
            if (hasNoEvents || viewState.getShouldRefreshEvents() || needsToFetchPeriod) {
                loadEventsAndCalculateEventChipPositions(view, day);
                viewState.setShouldRefreshEvents(false);
            }
        }
    }

    /**
     * Gets more events of one/more month(s) if necessary. This method is called when the user is
     * scrolling the week view. The week view stores the events of three months: the visible month,
     * the previous month, the next month.
     *
     * @param day The day the user is currently in.
     */
    private void loadEventsAndCalculateEventChipPositions(View view, Calendar day) {
        // Get more events if the month is changed.
        if (data.getAllEventChips() == null) {
            data.setEventChips(new ArrayList<EventChip<T>>());
        }

        if (weekViewLoader == null && !view.isInEditMode()) {
            throw new IllegalStateException("You must provide a MonthChangeListener");
        }

        // If a refresh was requested then reset some variables.
        if (viewState.getShouldRefreshEvents()) {
            data.clear();
        }

        if (weekViewLoader != null) {
            loadEvents(day);
        }

        // Prepare to calculate positions of each events.
        calculateEventChipPositions();
    }

    private void loadEvents(Calendar day) {
        final int periodToFetch = (int) weekViewLoader.toWeekViewPeriodIndex(day);
        final boolean isRefreshEligible = data.fetchedPeriod < 0
                || data.fetchedPeriod != periodToFetch
                || viewState.getShouldRefreshEvents();

        if (!isRefreshEligible) {
            return;
        }

        List<WeekViewEvent<T>> previousPeriodEvents = null;
        List<WeekViewEvent<T>> currentPeriodEvents = null;
        List<WeekViewEvent<T>> nextPeriodEvents = null;

        if (data.previousPeriodEvents != null
                && data.currentPeriodEvents != null && data.nextPeriodEvents != null) {
            if (periodToFetch == data.fetchedPeriod - 1) {
                currentPeriodEvents = data.previousPeriodEvents;
                nextPeriodEvents = data.currentPeriodEvents;
            } else if (periodToFetch == data.fetchedPeriod) {
                previousPeriodEvents = data.previousPeriodEvents;
                currentPeriodEvents = data.currentPeriodEvents;
                nextPeriodEvents = data.nextPeriodEvents;
            } else if (periodToFetch == data.fetchedPeriod + 1) {
                previousPeriodEvents = data.currentPeriodEvents;
                currentPeriodEvents = data.nextPeriodEvents;
            }
        }

        if (currentPeriodEvents == null) {
            currentPeriodEvents = weekViewLoader.onLoad(periodToFetch);
        }

        if (previousPeriodEvents == null) {
            previousPeriodEvents = weekViewLoader.onLoad(periodToFetch - 1);
        }

        if (nextPeriodEvents == null) {
            nextPeriodEvents = weekViewLoader.onLoad(periodToFetch + 1);
        }

        // Clear events.
        data.getAllEventChips().clear();
        data.sortAndCacheEvents(previousPeriodEvents);
        data.sortAndCacheEvents(currentPeriodEvents);
        data.sortAndCacheEvents(nextPeriodEvents);

        data.previousPeriodEvents = previousPeriodEvents;
        data.currentPeriodEvents = currentPeriodEvents;
        data.nextPeriodEvents = nextPeriodEvents;
        data.fetchedPeriod = periodToFetch;
    }

    private void calculateEventChipPositions() {
        // Prepare to calculate positions of each events.
        final List<EventChip<T>> tempEvents = data.getAllEventChips();
        final List<EventChip<T>> results = new ArrayList<>();

        // Iterate through each day with events to calculate the position of the events.
        while (!tempEvents.isEmpty()) {
            final List<EventChip<T>> eventChips = new ArrayList<>();

            final EventChip<T> firstRect = tempEvents.remove(0);
            eventChips.add(firstRect);

            int i = 0;
            while (i < tempEvents.size()) {
                // Collect all other events for same day.
                final EventChip<T> eventChip = tempEvents.get(i);
                final WeekViewEvent<T> event = eventChip.event;

                if (firstRect.event.isSameDay(event)) {
                    tempEvents.remove(i);
                    eventChips.add(eventChip);
                } else {
                    i++;
                }
            }

            computePositionOfEvents(eventChips);
            results.addAll(eventChips);
        }

        data.setEventChips(results);
    }

    /**
     * Calculates the left and right positions of each events. This comes handy specially if events
     * are overlapping.
     *
     * @param eventChips The events along with their wrapper class.
     */
    private void computePositionOfEvents(List<EventChip<T>> eventChips) {
        // Make "collision groups" for all events that collide with others.
        final List<List<EventChip>> collisionGroups = new ArrayList<>();
        for (EventChip eventChip : eventChips) {
            boolean isPlaced = false;

            outerLoop:
            for (List<EventChip> collisionGroup : collisionGroups) {
                for (EventChip groupEvent : collisionGroup) {
                    if (groupEvent.event.collidesWith(eventChip.event)
                            && groupEvent.event.isAllDay() == eventChip.event.isAllDay()) {
                        collisionGroup.add(eventChip);
                        isPlaced = true;
                        break outerLoop;
                    }
                }
            }

            if (!isPlaced) {
                final List<EventChip> newGroup = new ArrayList<>();
                newGroup.add(eventChip);
                collisionGroups.add(newGroup);
            }
        }

        for (List<EventChip> collisionGroup : collisionGroups) {
            expandEventsToMaxWidth(collisionGroup);
        }
    }

    /**
     * Expands all the events to maximum possible width. The events will try to occupy maximum
     * space available horizontally.
     *
     * @param collisionGroup The group of events which overlap with each other.
     */
    private void expandEventsToMaxWidth(List<EventChip> collisionGroup) {
        // Expand the events to maximum possible width.
        List<List<EventChip>> columns = new ArrayList<>();
        columns.add(new ArrayList<EventChip>());

        for (EventChip eventChip : collisionGroup) {
            boolean isPlaced = false;

            for (List<EventChip> column : columns) {
                if (column.size() == 0) {
                    column.add(eventChip);
                    isPlaced = true;
                } else if (!eventChip.event.collidesWith(column.get(column.size() - 1).event)) {
                    column.add(eventChip);
                    isPlaced = true;
                    break;
                }
            }

            if (!isPlaced) {
                List<EventChip> newColumn = new ArrayList<>();
                newColumn.add(eventChip);
                columns.add(newColumn);
            }
        }

        // Calculate left and right position for all the events.
        // Get the maxRowCount by looking in all columns.
        int maxRowCount = 0;
        for (List<EventChip> column : columns) {
            maxRowCount = Math.max(maxRowCount, column.size());
        }

        for (int i = 0; i < maxRowCount; i++) {
            // Set the left and right values of the event.
            float j = 0;
            for (List<EventChip> column : columns) {
                if (column.size() >= i + 1) {
                    EventChip eventChip = column.get(i);
                    eventChip.width = 1f / columns.size();
                    eventChip.left = j / columns.size();

                    if (!eventChip.event.isAllDay()) {
                        eventChip.top = eventChip.event.getStartTime().get(HOUR_OF_DAY) * 60
                                + eventChip.event.getStartTime().get(MINUTE);
                        eventChip.bottom = eventChip.event.getEndTime().get(HOUR_OF_DAY) * 60
                                + eventChip.event.getEndTime().get(MINUTE);
                    } else {
                        eventChip.top = 0;
                        eventChip.bottom = config.allDayEventHeight;
                    }
                }
                j++;
            }
        }
    }

}

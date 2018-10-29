package com.alamkanak.weekview.data;

import com.alamkanak.weekview.drawing.EventChip;
import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.model.WeekViewData;
import com.alamkanak.weekview.model.WeekViewEvent;
import com.alamkanak.weekview.model.WeekViewViewState;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static java.lang.Math.abs;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;

public class EventChipsProvider {

    private WeekViewConfig config;
    private WeekViewData data;
    private WeekViewLoader weekViewLoader;
    private WeekViewViewState viewState;

    public EventChipsProvider(WeekViewConfig config,
                              WeekViewData data, WeekViewViewState viewState) {
        this.config = config;
        this.data = data;
        this.viewState = viewState;
    }

    public void setWeekViewLoader(WeekViewLoader weekViewLoader) {
        this.weekViewLoader = weekViewLoader;
    }

    public void loadEventsIfNecessary(List<Calendar> dayRange /*, WeekViewLoader weekViewLoader*/) {
        for (Calendar day : dayRange) {
            //boolean isSameDay = isSameDay(day, today);
            boolean hasNoEvents = data.getAllEventChips() == null;
            boolean needsToFetchPeriod = data.fetchedPeriod != weekViewLoader.toWeekViewPeriodIndex(day)
                    && abs(data.fetchedPeriod - weekViewLoader.toWeekViewPeriodIndex(day)) > 0.5;

            // Check if this particular day has been fetched
            if (hasNoEvents || viewState.shouldRefreshEvents || needsToFetchPeriod) {
                loadEventsAndCalculateEventChipPositions(day);
                viewState.shouldRefreshEvents = false;
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
    private void loadEventsAndCalculateEventChipPositions(Calendar day) {
        // Get more events if the month is changed.
        if (data.getAllEventChips() == null) {
            data.setEventChips(new ArrayList<EventChip>()); // = new ArrayList<>();
        }

        if (weekViewLoader == null) { // TODO && !view.isInEditMode()) {
            throw new IllegalStateException("You must provide a MonthChangeListener");
        }

        // If a refresh was requested then reset some variables.
        if (viewState.shouldRefreshEvents) {
            data.clear();
        }

        if (weekViewLoader != null) {
            loadEvents(day);
        }

        // Prepare to calculate positions of each events.
        calculateEventChipPositions();
    }

    private void loadEvents(Calendar day) {
        int periodToFetch = (int) weekViewLoader.toWeekViewPeriodIndex(day);
        boolean isRefreshEligible = data.fetchedPeriod < 0
                || data.fetchedPeriod != periodToFetch
                || viewState.shouldRefreshEvents;

        if (!isRefreshEligible) {
            return;
        }

        List<? extends WeekViewEvent> previousPeriodEvents = null;
        List<? extends WeekViewEvent> currentPeriodEvents = null;
        List<? extends WeekViewEvent> nextPeriodEvents = null;

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
        // TODO: Polish this
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
        List<EventChip> tempEvents = data.getAllEventChips();
        List<EventChip> results = new ArrayList<>();
        //data.setEventChips(new ArrayList<EventChip>()); //.eventChips = new ArrayList<>();

        // Iterate through each day with events to calculate the position of the events.
        while (!tempEvents.isEmpty()) {
            List<EventChip> eventChips = new ArrayList<>();

            // Get first event for a day.
            EventChip firstRect = tempEvents.remove(0);
            eventChips.add(firstRect);

            int i = 0;
            while (i < tempEvents.size()) {
                // Collect all other events for same day.
                EventChip eventChip = tempEvents.get(i);
                WeekViewEvent event = eventChip.event;
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
    private void computePositionOfEvents(List<EventChip> eventChips) {
        // Make "collision groups" for all events that collide with others.
        List<List<EventChip>> collisionGroups = new ArrayList<>();
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
                List<EventChip> newGroup = new ArrayList<>();
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

                    // TODO data.getAllEventChips().add(eventChip);
                }
                j++;
            }
        }
    }

}

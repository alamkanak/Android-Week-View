package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.alamkanak.weekview.data.WeekViewLoader;
import com.alamkanak.weekview.data.WeekViewLoaderHelper;
import com.alamkanak.weekview.listeners.ScrollListener;
import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.model.WeekViewData;
import com.alamkanak.weekview.model.WeekViewEvent;
import com.alamkanak.weekview.model.WeekViewViewState;
import com.alamkanak.weekview.ui.WeekView;
import com.alamkanak.weekview.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.alamkanak.weekview.utils.DateUtils.isSameDay;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;

public class HeaderRowDrawer {

    // TODO: Fix all-day events not shown on first loading

    private Listener listener;

    private EventsDrawer eventsDrawer;
    private WeekViewConfig config;
    private WeekViewDrawingConfig drawConfig;

    private WeekViewData data;
    private WeekViewViewState viewState;

    private WeekViewLoader weekViewLoader;
    private ScrollListener scrollListener;

    public HeaderRowDrawer(Listener listener, EventsDrawer eventsDrawer,
                           WeekViewConfig config, WeekViewDrawingConfig drawConfig,
                           WeekViewData data, WeekViewViewState viewState) {
        this.listener = listener;
        this.eventsDrawer = eventsDrawer;
        this.config = config;
        this.drawConfig = drawConfig;
        this.data = data;
        this.viewState = viewState;
    }

    public void setWeekViewLoader(WeekViewLoader weekViewLoader) {
        this.weekViewLoader = weekViewLoader;
    }

    public void setScrollListener(ScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    // TODO: Make just as high as text + padding, no extra bottom padding or something
    private float calculateHeaderHeight(List<EventChip> eventChips,
                                        int numberOfVisibleDays, Calendar firstVisibleDay) {
        if (eventChips == null || eventChips.isEmpty()) {
            return drawConfig.headerTextHeight;
        }

        // Make sure the header is the right size (depends on AllDay events)
        boolean containsAllDayEvent = false;
        for (int i = 0; i < numberOfVisibleDays; i++) {
            Calendar day = (Calendar) firstVisibleDay.clone();
            day.add(Calendar.DATE, i);

            for (int j = 0; j < eventChips.size(); j++) {
                WeekViewEvent event = eventChips.get(j).event;
                if (event.isSameDay(day) && event.isAllDay()) {
                    containsAllDayEvent = true;
                    break;
                }
            }

            if (containsAllDayEvent) {
                break;
            }
        }

        if (containsAllDayEvent) {
            float headerTextSize = drawConfig.eventTextPaint.getTextSize();
            float totalEventPadding = config.eventPadding * 2;
            return drawConfig.headerTextHeight + (headerTextSize + totalEventPadding + drawConfig.headerMarginBottom);

            // TODO: Make adapt to number of all-day events

            //return drawConfig.headerTextHeight + (config.allDayEventHeight + drawConfig.headerMarginBottom);
        } else {
            return drawConfig.headerTextHeight;
        }
    }

    // TODO: Break up into multiple methods
    public void drawHeaderRowAndEvents(View view, Canvas canvas) {
        int width = WeekView.getViewWidth();
        int height = WeekView.getViewHeight();

        // Calculate the available width for each day.
        drawConfig.headerColumnWidth = drawConfig.timeTextWidth + config.headerColumnPadding * 2;
        drawConfig.widthPerDay = width - drawConfig.headerColumnWidth - config.columnGap * (config.numberOfVisibleDays - 1);
        drawConfig.widthPerDay = drawConfig.widthPerDay / config.numberOfVisibleDays;

        drawConfig.headerHeight = calculateHeaderHeight(
                data.getAllDayEventChips(), config.numberOfVisibleDays, viewState.firstVisibleDay);

        Calendar today = DateUtils.today();

        if (viewState.areDimensionsInvalid) {
            config.effectiveMinHourHeight = Math.max(config.minHourHeight, (int) ((height - drawConfig.headerHeight - config.headerRowPadding * 2 - drawConfig.headerMarginBottom) / 24));

            viewState.areDimensionsInvalid = false;
            if (viewState.scrollToDay != null) {
                listener.goToDate(viewState.scrollToDay);
            }

            viewState.areDimensionsInvalid = false;
            if (viewState.scrollToHour >= 0) {
                listener.goToHour(viewState.scrollToHour);
            }

            viewState.scrollToDay = null;
            viewState.scrollToHour = -1;
            viewState.areDimensionsInvalid = false;
        }

        if (viewState.isFirstDraw) {
            viewState.isFirstDraw = false;

            // If the week view is being drawn for the first time, then consider the first day of the week.
            if (config.numberOfVisibleDays >= 7 && today.get(Calendar.DAY_OF_WEEK) != config.firstDayOfWeek && config.showFirstDayOfWeekFirst) {
                int difference = (today.get(Calendar.DAY_OF_WEEK) - config.firstDayOfWeek);
                drawConfig.currentOrigin.x += (drawConfig.widthPerDay + config.columnGap) * difference;
            }
        }

        // Calculate the new height due to the zooming.
        if (drawConfig.newHourHeight > 0) {
            if (drawConfig.newHourHeight < config.effectiveMinHourHeight) {
                drawConfig.newHourHeight = config.effectiveMinHourHeight;
            } else if (drawConfig.newHourHeight > config.maxHourHeight) {
                drawConfig.newHourHeight = config.maxHourHeight;
            }

            drawConfig.currentOrigin.y = (drawConfig.currentOrigin.y / config.hourHeight) * drawConfig.newHourHeight;
            config.hourHeight = drawConfig.newHourHeight;
            drawConfig.newHourHeight = -1;
        }

        // If the new currentOrigin.y is invalid, make it valid.
        if (drawConfig.currentOrigin.y < height - config.hourHeight * 24 - drawConfig.headerHeight - config.headerRowPadding * 2 - drawConfig.headerMarginBottom - drawConfig.timeTextHeight / 2)
            drawConfig.currentOrigin.y = height - config.hourHeight * 24 - drawConfig.headerHeight - config.headerRowPadding * 2 - drawConfig.headerMarginBottom - drawConfig.timeTextHeight / 2;

        // Don't put an "else if" because it will trigger a glitch when completely zoomed out and
        // scrolling vertically.
        if (drawConfig.currentOrigin.y > 0) {
            drawConfig.currentOrigin.y = 0;
        }

        // Consider scroll offset.
        int leftDaysWithGaps = (int) -(Math.ceil(drawConfig.currentOrigin.x / (drawConfig.widthPerDay + config.columnGap)));
        float startFromPixel = drawConfig.currentOrigin.x + (drawConfig.widthPerDay + config.columnGap) * leftDaysWithGaps +
                drawConfig.headerColumnWidth;
        float startPixel = startFromPixel;

        // Prepare to iterate for each day.
        Calendar day = (Calendar) today.clone();
        day.add(Calendar.HOUR, 6);

        // Prepare to iterate for each hour to drawTimeColumn the hour lines.
        int lineCount = (int) ((height - drawConfig.headerHeight - config.headerRowPadding * 2 -
                drawConfig.headerMarginBottom) / config.hourHeight) + 1;
        lineCount = (lineCount) * (config.numberOfVisibleDays + 1);
        float[] hourLines = new float[lineCount * 4];

        // Clear the cache for event rectangles.
        if (data.getAllEventChips() != null) {
            for (EventChip eventChip : data.getAllEventChips()) {
                eventChip.rectF = null;
            }
        }

        canvas.save();

        // Clip to paint events only.
        canvas.clipRect(drawConfig.headerColumnWidth, drawConfig.headerHeight + config.headerRowPadding * 2 + drawConfig.headerMarginBottom + drawConfig.timeTextHeight / 2, width, height);

        // Iterate through each day.
        Calendar oldFirstVisibleDay = viewState.firstVisibleDay;
        viewState.firstVisibleDay = (Calendar) today.clone();
        viewState.firstVisibleDay.add(Calendar.DATE, -(Math.round(drawConfig.currentOrigin.x / (drawConfig.widthPerDay + config.columnGap))));
        if (!viewState.firstVisibleDay.equals(oldFirstVisibleDay) && scrollListener != null) {
            scrollListener.onFirstVisibleDayChanged(viewState.firstVisibleDay, oldFirstVisibleDay);
        }

        DayBackgroundDrawer dayBackgroundDrawer = new DayBackgroundDrawer(config, drawConfig);
        BackgroundGridDrawer backgroundGridDrawer = new BackgroundGridDrawer(config, drawConfig);
        NowLineDrawer nowLineDrawer = new NowLineDrawer(config, drawConfig);

        // TODO: Filter eventRects and allDayEventRects

        // TODO: Cleanup
        for (int dayNumber = leftDaysWithGaps + 1;
             dayNumber <= leftDaysWithGaps + config.numberOfVisibleDays + 1;
             dayNumber++) {

            // Check if the day is today.
            day = (Calendar) today.clone();
            viewState.lastVisibleDay = (Calendar) day.clone();
            day.add(Calendar.DATE, dayNumber - 1);
            viewState.lastVisibleDay.add(Calendar.DATE, dayNumber - 2);
            boolean sameDay = isSameDay(day, today);

            // Get more events if necessary. We want to store the events 3 months beforehand. Get
            // events only when it is the first iteration of the loop.
            // TODO: Cleanup
            if (data.getAllEventChips() == null || viewState.shouldRefreshEvents ||
                    (dayNumber == leftDaysWithGaps + 1 && data.fetchedPeriod != (int) weekViewLoader.toWeekViewPeriodIndex(day) &&
                            Math.abs(data.fetchedPeriod - weekViewLoader.toWeekViewPeriodIndex(day)) > 0.5)) {
                getMoreEvents(view, day);
                viewState.shouldRefreshEvents = false;
            }

            float startX = (startPixel < drawConfig.headerColumnWidth ? drawConfig.headerColumnWidth : startPixel);
            dayBackgroundDrawer.drawDayBackground(day, height, startX, startPixel, canvas);
            backgroundGridDrawer.drawGrid(hourLines, height, startX, startPixel, canvas);

            if (config.isSingleDay()) {
                // Add a margin at the start if we're in day view. Otherwise, screen space is too
                // precious and we refrain from doing so.
                startPixel = startPixel + config.eventMarginHorizontal;
            }

            eventsDrawer.drawEvents(data.getNormalEventChips(), width, height, day, startPixel, canvas);

            // Draw the line at the current time.
            if (config.showNowLine && sameDay) {
                nowLineDrawer.drawLine(startX, startPixel, canvas);
            }

            // In the next iteration, start from the next day.
            startPixel += drawConfig.widthPerDay + config.columnGap;
        }

        canvas.restore();
        canvas.save();

        // Hide everything in the first cell (top left corner).
        // TODO: Code quality
        canvas.clipRect(0, 0, drawConfig.timeTextWidth + config.headerColumnPadding * 2, drawConfig.headerHeight + config.headerRowPadding * 2);

        // TODO: Code quality
        canvas.drawRect(0, 0, drawConfig.timeTextWidth + config.headerColumnPadding * 2, drawConfig.headerHeight + config.headerRowPadding * 2, drawConfig.headerBackgroundPaint);

        canvas.restore();
        canvas.save();

        // Clip to paint header row only.
        // TODO: Code quality
        canvas.clipRect(drawConfig.headerColumnWidth, 0, width, drawConfig.headerHeight + config.headerRowPadding * 2);

        // Draw the header background.
        // TODO: Code quality
        canvas.drawRect(0, 0, width, drawConfig.headerHeight + config.headerRowPadding * 2, drawConfig.headerBackgroundPaint);

        // Draw the header row texts.
        startPixel = startFromPixel;

        int size = leftDaysWithGaps + config.numberOfVisibleDays + 1;
        for (int dayNumber = leftDaysWithGaps + 1; dayNumber <= size; dayNumber++) {
            // Check if the day is today.
            day = (Calendar) today.clone();
            day.add(Calendar.DATE, dayNumber - 1);
            boolean isSameDay = isSameDay(day, today);

            // Draw the day labels.
            String dayLabel = drawConfig.dateTimeInterpreter.interpretDate(day);
            if (dayLabel == null) {
                throw new IllegalStateException("A DateTimeInterpreter must not return null date");
            }

            // TODO: Code quality (Move to DayLabelDrawer?)
            float x = startPixel + drawConfig.widthPerDay / 2;
            float y = drawConfig.headerTextHeight + config.headerRowPadding;
            Paint textPaint = isSameDay ? drawConfig.todayHeaderTextPaint : drawConfig.headerTextPaint;
            canvas.drawText(dayLabel, x, y, textPaint);

            if (config.isSingleDay()) {
                startPixel = startPixel + config.eventMarginHorizontal;
            }

            // TODO: Code quality
            eventsDrawer.drawAllDayEvents(data.getAllDayEventChips(), day, startPixel, canvas);
            startPixel += drawConfig.widthPerDay + config.columnGap;
        }
    }

    /**
     * Gets more events of one/more month(s) if necessary. This method is called when the user is
     * scrolling the week view. The week view stores the events of three months: the visible month,
     * the previous month, the next month.
     *
     * @param day The day where the user is currently is.
     */
    private void getMoreEvents(View view, Calendar day) {
        // Get more events if the month is changed.
        if (data.getAllEventChips() == null) {
            data.setEventChips(new ArrayList<EventChip>()); // = new ArrayList<>();
        }

        if (weekViewLoader == null && !view.isInEditMode()) {
            throw new IllegalStateException("You must provide a MonthChangeListener");
        }

        // If a refresh was requested then reset some variables.
        if (viewState.shouldRefreshEvents) {
            data.clear();
        }

        if (weekViewLoader != null && !view.isInEditMode()) {
            WeekViewLoaderHelper.load(data, viewState, weekViewLoader, day);
        }

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

                    //data.getAllEventChips().add(eventChip);
                }
                j++;
            }
        }
    }

    public interface Listener {

        void goToDate(Calendar date);

        void goToHour(double hour);

    }

}

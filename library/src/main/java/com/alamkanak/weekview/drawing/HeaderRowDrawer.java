package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.alamkanak.weekview.data.EventChipsProvider;
import com.alamkanak.weekview.data.WeekViewLoader;
import com.alamkanak.weekview.listeners.ScrollListener;
import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.model.WeekViewData;
import com.alamkanak.weekview.model.WeekViewEvent;
import com.alamkanak.weekview.model.WeekViewViewState;
import com.alamkanak.weekview.ui.WeekView;

import java.util.Calendar;
import java.util.List;

import static com.alamkanak.weekview.utils.DateUtils.isSameDay;
import static com.alamkanak.weekview.utils.DateUtils.today;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_WEEK;

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
                           WeekViewConfig config, WeekViewData data, WeekViewViewState viewState) {
        this.listener = listener;
        this.eventsDrawer = eventsDrawer;
        this.config = config;
        this.drawConfig = config.drawingConfig;
        this.data = data;
        this.viewState = viewState;
    }

    public void setWeekViewLoader(WeekViewLoader weekViewLoader) {
        this.weekViewLoader = weekViewLoader;
    }

    public void setScrollListener(ScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    private float calculateHeaderHeight(List<EventChip> eventChips,
                                        int numberOfVisibleDays, Calendar firstVisibleDay) {
        if (eventChips == null || eventChips.isEmpty()) {
            return drawConfig.headerTextHeight;
        }

        // Make sure the header is the right size (depends on AllDay events)
        boolean containsAllDayEvent = false;
        for (int i = 0; i < numberOfVisibleDays; i++) {
            Calendar day = (Calendar) firstVisibleDay.clone();
            day.add(DATE, i);

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
            // TODO: Make adapt to number of all-day events
            float headerTextSize = drawConfig.eventTextPaint.getTextSize();
            float totalEventPadding = config.eventPadding * 2;
            return drawConfig.headerTextHeight + (headerTextSize + totalEventPadding + drawConfig.headerMarginBottom);
        } else {
            return drawConfig.headerTextHeight;
        }
    }

    private void calculateAvailableSpace() {
        int width = WeekView.getViewWidth();

        // Calculate the available width for each day
        drawConfig.headerColumnWidth = drawConfig.timeTextWidth + config.headerColumnPadding * 2;
        drawConfig.widthPerDay = width - drawConfig.headerColumnWidth - config.columnGap * (config.numberOfVisibleDays - 1);
        drawConfig.widthPerDay = drawConfig.widthPerDay / config.numberOfVisibleDays;

        // Calculate the header height
        drawConfig.headerHeight = calculateHeaderHeight(
                data.getAllDayEventChips(), config.numberOfVisibleDays, viewState.firstVisibleDay);
    }

    private void scrollToDateAndHourIfNecessary() {
        int height = WeekView.getViewHeight();

        if (!viewState.areDimensionsInvalid) {
            return;
        }

        // TODO: Why here?
        config.effectiveMinHourHeight = max(config.minHourHeight, (int) ((height - drawConfig.headerHeight - config.headerRowPadding * 2 - drawConfig.headerMarginBottom) / 24));

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

    private void moveCurrentOriginIfFirstDraw() {
        Calendar today = today();

        if (viewState.isFirstDraw) {
            viewState.isFirstDraw = false;

            // If the week view is being drawn for the first time, then consider the first day of the week.
            boolean isWeekView = config.numberOfVisibleDays >= 7;
            boolean currentDayIsNotToday = today.get(DAY_OF_WEEK) != config.firstDayOfWeek;
            if (isWeekView && currentDayIsNotToday && config.showFirstDayOfWeekFirst) {
                int difference = today.get(DAY_OF_WEEK) - config.firstDayOfWeek;
                drawConfig.currentOrigin.x += (drawConfig.widthPerDay + config.columnGap) * difference;
            }
        }
    }

    private void calculateNewHourHeighAfterZoomingIfNecessary() {
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
    }

    private void updateVerticalOriginIfNecessary() {
        int height = WeekView.getViewHeight();

        // If the new currentOrigin.y is invalid, make it valid.
        float dayHeight = config.hourHeight * 24;
        float headerHeight = drawConfig.headerHeight
                + config.headerRowPadding * 2
                + drawConfig.headerMarginBottom;
        float halfTextHeight = drawConfig.timeTextHeight / 2;

        float potentialNewVerticalOrigin = height - (dayHeight + headerHeight + halfTextHeight);

        drawConfig.currentOrigin.y = max(drawConfig.currentOrigin.y, potentialNewVerticalOrigin);

        // TODO: Figure out why this is needed
        drawConfig.currentOrigin.y = min(drawConfig.currentOrigin.y, 0);
    }

    private void drawDayLabelsAndAllDayEvents(int start, int size, float startPixel, Canvas canvas) {
        DayLabelDrawer dayLabelDrawer = new DayLabelDrawer(config);

        Calendar today = today();
        Calendar day;

        for (int dayNumber = start; dayNumber <= size; dayNumber++) {
            // Check if the day is today.
            day = (Calendar) today.clone();
            day.add(DATE, dayNumber - 1);

            dayLabelDrawer.draw(day, startPixel, canvas);

            if (config.isSingleDay()) {
                startPixel = startPixel + config.eventMarginHorizontal;
            }

            eventsDrawer.drawAllDayEvents(data.getAllDayEventChips(), day, startPixel, canvas);
            startPixel += drawConfig.widthPerDay + config.columnGap;
        }
    }

    private void drawMainAreaWithEvents(float[] hourLines, int start, int end, float startPixel, Canvas canvas) {
        DayBackgroundDrawer dayBackgroundDrawer = new DayBackgroundDrawer(config);
        BackgroundGridDrawer backgroundGridDrawer = new BackgroundGridDrawer(config);
        NowLineDrawer nowLineDrawer = new NowLineDrawer(config);

        // TODO: Filter eventRects and allDayEventRects

        EventChipsProvider eventChipsProvider = new EventChipsProvider(config, data, viewState);
        eventChipsProvider.setWeekViewLoader(weekViewLoader);

        Calendar today = today();
        Calendar day;

        for (int dayNumber = start; dayNumber <= end; dayNumber++) {

            // Check if the day is today.
            day = (Calendar) today.clone();
            viewState.lastVisibleDay = (Calendar) day.clone();
            day.add(DATE, dayNumber - 1);
            viewState.lastVisibleDay.add(DATE, dayNumber - 2);
            boolean sameDay = isSameDay(day, today);

            // Get more events if necessary. We want to store the events 3 months beforehand. Get
            // events only when it is the first iteration of the loop.
            // TODO: Cleanup
            if (data.getAllEventChips() == null || viewState.shouldRefreshEvents ||
                    (dayNumber == start && data.fetchedPeriod != (int) weekViewLoader.toWeekViewPeriodIndex(day) &&
                            Math.abs(data.fetchedPeriod - weekViewLoader.toWeekViewPeriodIndex(day)) > 0.5)) {
                //getMoreEvents(view, day);
                eventChipsProvider.loadEventsAndCalculateEventChipPositions(day);
                viewState.shouldRefreshEvents = false;
            }

            float startX = (startPixel < drawConfig.headerColumnWidth ? drawConfig.headerColumnWidth : startPixel);
            dayBackgroundDrawer.drawDayBackground(day, startX, startPixel, canvas);
            backgroundGridDrawer.drawGrid(hourLines, startX, startPixel, canvas);

            if (config.isSingleDay()) {
                // Add a margin at the start if we're in day view. Otherwise, screen space is too
                // precious and we refrain from doing so.
                startPixel = startPixel + config.eventMarginHorizontal;
            }

            eventsDrawer.drawEvents(data.getNormalEventChips(), day, startPixel, canvas);

            // Draw the line at the current time.
            if (config.showNowLine && sameDay) {
                nowLineDrawer.drawLine(startX, startPixel, canvas);
            }

            // In the next iteration, start from the next day.
            startPixel += drawConfig.widthPerDay + config.columnGap;
        }
    }

    private float[] getHourLines() {
        int height = WeekView.getViewHeight();
        float headerHeight = drawConfig.headerHeight
                + config.headerRowPadding * 2
                + drawConfig.headerMarginBottom;
        int lineCount = (int) ((height - headerHeight) / config.hourHeight) + 1;
        lineCount = (lineCount) * (config.numberOfVisibleDays + 1);
        return new float[lineCount * 4];
    }

    private void clipEventsRect(Canvas canvas) {
        int width = WeekView.getViewWidth();
        int height = WeekView.getViewHeight();

        // Clip to paint events only.
        float headerHeight = drawConfig.headerHeight
                + config.headerRowPadding * 2
                + drawConfig.headerMarginBottom;

        float halfTextHeight = drawConfig.timeTextHeight / 2;
        canvas.clipRect(drawConfig.headerColumnWidth, headerHeight + halfTextHeight, width, height);
    }

    private void notifyScrollListeners() {
        // Iterate through each day.
        Calendar oldFirstVisibleDay = viewState.firstVisibleDay;
        Calendar today = today();

        viewState.firstVisibleDay = (Calendar) today.clone();

        float totalDayWidth = drawConfig.widthPerDay + config.columnGap;
        viewState.firstVisibleDay.add(DATE, round(drawConfig.currentOrigin.x / totalDayWidth) * -1);

        if (!viewState.firstVisibleDay.equals(oldFirstVisibleDay) && scrollListener != null) {
            scrollListener.onFirstVisibleDayChanged(viewState.firstVisibleDay, oldFirstVisibleDay);
        }
    }

    private void drawCompleteHeaderRow(Canvas canvas) {
        int width = WeekView.getViewWidth();

        canvas.restore();
        canvas.save();

        Paint headerBackground = drawConfig.headerBackgroundPaint;
        float headerHeight = drawConfig.headerHeight + config.headerRowPadding * 2;

        // Hide everything in the first cell (top left corner).
        // TODO: Code quality
        float topLeftCornerWidth = drawConfig.timeTextWidth + config.headerColumnPadding * 2;
        canvas.clipRect(0, 0, topLeftCornerWidth, headerHeight);

        // TODO: Code quality
        canvas.drawRect(0, 0, topLeftCornerWidth, headerHeight, headerBackground);

        canvas.restore();
        canvas.save();

        // Clip to paint header row only.
        // TODO: Code quality
        canvas.clipRect(drawConfig.headerColumnWidth, 0, width, headerHeight);

        // Draw the header background.
        // TODO: Code quality
        canvas.drawRect(0, 0, width, headerHeight, headerBackground);
    }

    // TODO: Break up into multiple methods
    // Move parts into EventsDrawer
    public void drawHeaderRowAndEvents(Canvas canvas) {
        // TODO: List all methods here
        calculateAvailableSpace();
        scrollToDateAndHourIfNecessary();
        moveCurrentOriginIfFirstDraw();
        calculateNewHourHeighAfterZoomingIfNecessary();
        updateVerticalOriginIfNecessary();

        // Consider scroll offset.
        int leftDaysWithGaps = (int) -(Math.ceil(drawConfig.currentOrigin.x / (drawConfig.widthPerDay + config.columnGap)));
        float totalDayWidth = drawConfig.widthPerDay + config.columnGap;
        float startPixel = drawConfig.currentOrigin.x
                + totalDayWidth * leftDaysWithGaps
                + drawConfig.headerColumnWidth;

        Calendar today = today();

        // Prepare to iterate for each day.
        Calendar day = (Calendar) today.clone();
        day.add(Calendar.HOUR, 6);

        // Prepare to iterate for each hour to draw the hour lines.
        float[] hourLines = getHourLines();

        // Clear the cache for event rectangles.
        data.clearEventChipsCache();

        canvas.save();

        clipEventsRect(canvas);

        notifyScrollListeners();

        int start = leftDaysWithGaps + 1;
        int end = start + config.numberOfVisibleDays + 1;

        drawMainAreaWithEvents(hourLines, start, end, startPixel, canvas);

        drawCompleteHeaderRow(canvas);

        /*
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
        */

        drawDayLabelsAndAllDayEvents(start, end, startPixel, canvas);
    }

    /*
     * Gets more events of one/more month(s) if necessary. This method is called when the user is
     * scrolling the week view. The week view stores the events of three months: the visible month,
     * the previous month, the next month.
     *
     * @param day The day where the user is currently is.
     */
    /*
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
    */

    /*
     * Calculates the left and right positions of each events. This comes handy specially if events
     * are overlapping.
     *
     * @param eventChips The events along with their wrapper class.
     */
    /*
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
    */

    /*
     * Expands all the events to maximum possible width. The events will try to occupy maximum
     * space available horizontally.
     *
     * @param collisionGroup The group of events which overlap with each other.
     */
    /*
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
    */

    public interface Listener {

        void goToDate(Calendar date);

        void goToHour(int hour);

    }

}

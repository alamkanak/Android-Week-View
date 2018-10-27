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
import com.alamkanak.weekview.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.alamkanak.weekview.utils.DateUtils.isSameDay;

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
    public float calculateHeaderHeight(List<EventRect> eventRects,
                                       int numberOfVisibleDays, Calendar firstVisibleDay) {
        if (eventRects == null || eventRects.isEmpty()) {
            return drawConfig.headerTextHeight;
        }

        // Make sure the header is the right size (depends on AllDay events)
        boolean containsAllDayEvent = false;
        for (int i = 0; i < numberOfVisibleDays; i++) {
            Calendar day = (Calendar) firstVisibleDay.clone();
            day.add(Calendar.DATE, i);

            for (int j = 0; j < eventRects.size(); j++) {
                WeekViewEvent event = eventRects.get(j).event;
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
            return drawConfig.headerTextHeight + (config.allDayEventHeight + drawConfig.headerMarginBottom);
        } else {
            return drawConfig.headerTextHeight;
        }
    }

    public void drawHeaderRowAndEvents(View view, Canvas canvas) {
        int width = view.getWidth();
        int height = view.getHeight();

        // Calculate the available width for each day.
        drawConfig.headerColumnWidth = drawConfig.timeTextWidth + config.headerColumnPadding * 2;
        drawConfig.widthPerDay = width - drawConfig.headerColumnWidth - config.columnGap * (config.numberOfVisibleDays - 1);
        drawConfig.widthPerDay = drawConfig.widthPerDay / config.numberOfVisibleDays;

        drawConfig.headerHeight = calculateHeaderHeight(
                data.eventRects, config.numberOfVisibleDays, viewState.firstVisibleDay);

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

        // Prepare to iterate for each hour to draw the hour lines.
        int lineCount = (int) ((height - drawConfig.headerHeight - config.headerRowPadding * 2 -
                drawConfig.headerMarginBottom) / config.hourHeight) + 1;
        lineCount = (lineCount) * (config.numberOfVisibleDays + 1);
        float[] hourLines = new float[lineCount * 4];

        // Clear the cache for event rectangles.
        if (data.eventRects != null) {
            for (EventRect eventRect : data.eventRects) {
                eventRect.rectF = null;
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
            if (data.eventRects == null || viewState.shouldRefreshEvents ||
                    (dayNumber == leftDaysWithGaps + 1 && data.fetchedPeriod != (int) weekViewLoader.toWeekViewPeriodIndex(day) &&
                            Math.abs(data.fetchedPeriod - weekViewLoader.toWeekViewPeriodIndex(day)) > 0.5)) {
                getMoreEvents(view, day);
                viewState.shouldRefreshEvents = false;
            }

            // Draw background color for each day.
            float start = (startPixel < drawConfig.headerColumnWidth ? drawConfig.headerColumnWidth : startPixel);
            if (drawConfig.widthPerDay + startPixel - start > 0) {
                if (config.showDistinctPastFutureColor) {
                    boolean isWeekend = day.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || day.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
                    Paint pastPaint = isWeekend && config.showDistinctWeekendColor ? drawConfig.pastWeekendBackgroundPaint : drawConfig.pastBackgroundPaint;
                    Paint futurePaint = isWeekend && config.showDistinctWeekendColor ? drawConfig.futureWeekendBackgroundPaint : drawConfig.futureBackgroundPaint;
                    float startY = drawConfig.headerHeight + config.headerRowPadding * 2 + drawConfig.timeTextHeight / 2 + drawConfig.headerMarginBottom + drawConfig.currentOrigin.y;

                    if (sameDay) {
                        Calendar now = Calendar.getInstance();
                        float beforeNow = (now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0f) * config.hourHeight;
                        canvas.drawRect(start, startY, startPixel + drawConfig.widthPerDay, startY + beforeNow, pastPaint);
                        canvas.drawRect(start, startY + beforeNow, startPixel + drawConfig.widthPerDay, height, futurePaint);
                    } else if (day.before(today)) {
                        canvas.drawRect(start, startY, startPixel + drawConfig.widthPerDay, height, pastPaint);
                    } else {
                        canvas.drawRect(start, startY, startPixel + drawConfig.widthPerDay, height, futurePaint);
                    }
                } else {
                    canvas.drawRect(start, drawConfig.headerHeight + config.headerRowPadding * 2 + drawConfig.timeTextHeight / 2 + drawConfig.headerMarginBottom, startPixel + drawConfig.widthPerDay, height, sameDay ? drawConfig.todayBackgroundPaint : drawConfig.dayBackgroundPaint);
                }
            }

            // Prepare the separator lines for hours.
            int i = 0;
            for (int hourNumber = 0; hourNumber < 24; hourNumber++) {
                float top = drawConfig.headerHeight + config.headerRowPadding * 2 + drawConfig.currentOrigin.y + config.hourHeight * hourNumber + drawConfig.timeTextHeight / 2 + drawConfig.headerMarginBottom;
                if (top > drawConfig.headerHeight + config.headerRowPadding * 2 + drawConfig.timeTextHeight / 2 + drawConfig.headerMarginBottom - config.hourSeparatorStrokeWidth && top < height && startPixel + drawConfig.widthPerDay - start > 0) {
                    hourLines[i * 4] = start;
                    hourLines[i * 4 + 1] = top;
                    hourLines[i * 4 + 2] = startPixel + drawConfig.widthPerDay;
                    hourLines[i * 4 + 3] = top;
                    i++;
                }
            }

            // Draw the lines for hours.
            canvas.drawLines(hourLines, drawConfig.hourSeparatorPaint);

            // Draw the events.
            if (config.numberOfVisibleDays == 1) {
                startPixel = startPixel + config.eventMarginHorizontal;
            }

            eventsDrawer.draw(data.eventRects, width, height, day, startPixel, canvas);

            // Draw the line at the current time.
            if (config.showNowLine && sameDay) {
                // TODO: Code quality
                float startY = drawConfig.headerHeight + config.headerRowPadding * 2 + drawConfig.timeTextHeight / 2 + drawConfig.headerMarginBottom + drawConfig.currentOrigin.y;
                Calendar now = Calendar.getInstance();

                // TODO: Draw dot at the beginning of the line
                float beforeNow = (now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0f) * config.hourHeight;
                canvas.drawLine(start, startY + beforeNow, startPixel + drawConfig.widthPerDay, startY + beforeNow, drawConfig.nowLinePaint);
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
            boolean sameDay = isSameDay(day, today);

            // Draw the day labels.
            String dayLabel = drawConfig.dateTimeInterpreter.interpretDate(day);
            if (dayLabel == null) {
                throw new IllegalStateException("A DateTimeInterpreter must not return null date");
            }

            // TODO: Code quality
            float x = startPixel + drawConfig.widthPerDay / 2;
            float y = drawConfig.headerTextHeight + config.headerRowPadding;
            Paint textPaint = sameDay ? drawConfig.todayHeaderTextPaint : drawConfig.headerTextPaint;
            canvas.drawText(dayLabel, x, y, textPaint);

            if (config.isSingleDay()) {
                startPixel = startPixel + config.eventMarginHorizontal;
            }

            // TODO: Code quality
            eventsDrawer.drawAllDayEvents(data.eventRects, width, height, day, startPixel, canvas);
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
        if (data.eventRects == null) {
            data.eventRects = new ArrayList<>();
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
        List<EventRect> tempEvents = data.eventRects;
        data.eventRects = new ArrayList<>();

        // Iterate through each day with events to calculate the position of the events.
        while (!tempEvents.isEmpty()) {
            ArrayList<EventRect> eventRects = new ArrayList<>(tempEvents.size());

            // Get first event for a day.
            EventRect firstRect = tempEvents.remove(0);
            eventRects.add(firstRect);

            int i = 0;
            while (i < tempEvents.size()) {
                // Collect all other events for same day.
                EventRect eventRect = tempEvents.get(i);
                WeekViewEvent event = eventRect.event;
                if (firstRect.event.isSameDay(event)) {
                    tempEvents.remove(i);
                    eventRects.add(eventRect);
                } else {
                    i++;
                }
            }
            computePositionOfEvents(eventRects);
        }
    }

    /**
     * Calculates the left and right positions of each events. This comes handy specially if events
     * are overlapping.
     *
     * @param eventRects The events along with their wrapper class.
     */
    private void computePositionOfEvents(List<EventRect> eventRects) {
        // Make "collision groups" for all events that collide with others.
        List<List<EventRect>> collisionGroups = new ArrayList<>();
        for (EventRect eventRect : eventRects) {
            boolean isPlaced = false;

            outerLoop:
            for (List<EventRect> collisionGroup : collisionGroups) {
                for (EventRect groupEvent : collisionGroup) {
                    if (groupEvent.event.collidesWith(eventRect.event)
                            && groupEvent.event.isAllDay() == eventRect.event.isAllDay()) {
                        collisionGroup.add(eventRect);
                        isPlaced = true;
                        break outerLoop;
                    }
                }
            }

            if (!isPlaced) {
                List<EventRect> newGroup = new ArrayList<>();
                newGroup.add(eventRect);
                collisionGroups.add(newGroup);
            }
        }

        for (List<EventRect> collisionGroup : collisionGroups) {
            expandEventsToMaxWidth(collisionGroup);
        }
    }

    /**
     * Expands all the events to maximum possible width. The events will try to occupy maximum
     * space available horizontally.
     *
     * @param collisionGroup The group of events which overlap with each other.
     */
    private void expandEventsToMaxWidth(List<EventRect> collisionGroup) {
        // Expand the events to maximum possible width.
        List<List<EventRect>> columns = new ArrayList<>();
        columns.add(new ArrayList<EventRect>());

        for (EventRect eventRect : collisionGroup) {
            boolean isPlaced = false;

            for (List<EventRect> column : columns) {
                if (column.size() == 0) {
                    column.add(eventRect);
                    isPlaced = true;
                } else if (!eventRect.event.collidesWith(column.get(column.size() - 1).event)) {
                    column.add(eventRect);
                    isPlaced = true;
                    break;
                }
            }

            if (!isPlaced) {
                List<EventRect> newColumn = new ArrayList<>();
                newColumn.add(eventRect);
                columns.add(newColumn);
            }
        }

        // Calculate left and right position for all the events.
        // Get the maxRowCount by looking in all columns.
        int maxRowCount = 0;
        for (List<EventRect> column : columns) {
            maxRowCount = Math.max(maxRowCount, column.size());
        }

        for (int i = 0; i < maxRowCount; i++) {
            // Set the left and right values of the event.
            float j = 0;
            for (List<EventRect> column : columns) {
                if (column.size() >= i + 1) {
                    EventRect eventRect = column.get(i);
                    eventRect.width = 1f / columns.size();
                    eventRect.left = j / columns.size();

                    if (!eventRect.event.isAllDay()) {
                        eventRect.top = eventRect.event.getStartTime().get(Calendar.HOUR_OF_DAY) * 60
                                + eventRect.event.getStartTime().get(Calendar.MINUTE);
                        eventRect.bottom = eventRect.event.getEndTime().get(Calendar.HOUR_OF_DAY) * 60
                                + eventRect.event.getEndTime().get(Calendar.MINUTE);
                    } else {
                        eventRect.top = 0;
                        eventRect.bottom = config.allDayEventHeight;
                    }

                    data.eventRects.add(eventRect);
                }
                j++;
            }
        }
    }

    public interface Listener {

        void goToDate(Calendar date);

        void goToHour(double hour); // TODO: Use JodaTime for this

    }

}

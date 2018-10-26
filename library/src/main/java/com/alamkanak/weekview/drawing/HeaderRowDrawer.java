package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.alamkanak.weekview.WeekViewLoader;
import com.alamkanak.weekview.listeners.ScrollListener;
import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.model.WeekViewData;
import com.alamkanak.weekview.model.WeekViewEvent;
import com.alamkanak.weekview.model.WeekViewViewState;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.alamkanak.weekview.utils.WeekViewUtil.isSameDay;
import static com.alamkanak.weekview.utils.WeekViewUtil.today;

public class HeaderRowDrawer {

    private Listener listener;

    private EventsDrawer eventsDrawer;
    private WeekViewConfig config;
    private WeekViewDrawingConfig drawingConfig;

    private WeekViewData data;
    private WeekViewViewState viewState;

    private WeekViewLoader weekViewLoader;
    private ScrollListener scrollListener;

    public HeaderRowDrawer(Listener listener, EventsDrawer eventsDrawer,
                           WeekViewConfig config, WeekViewDrawingConfig drawingConfig,
                           WeekViewData data, WeekViewViewState viewState) {
        this.listener = listener;
        this.eventsDrawer = eventsDrawer;
        this.config = config;
        this.drawingConfig = drawingConfig;
        this.data = data;
        this.viewState = viewState;
    }

    public void setWeekViewLoader(WeekViewLoader weekViewLoader) {
        this.weekViewLoader = weekViewLoader;
    }

    public void setScrollListener(ScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    public float calculateHeaderHeight(List<EventRect> eventRects,
                                       int numberOfVisibleDays, Calendar firstVisibleDay) {
        //Make sure the header is the right size (depends on AllDay events)
        boolean containsAllDayEvent = false;
        if (eventRects != null && eventRects.size() > 0) {
            for (int dayNumber = 0;
                 dayNumber < numberOfVisibleDays;
                 dayNumber++) {
                Calendar day = (Calendar) firstVisibleDay.clone();
                day.add(Calendar.DATE, dayNumber);
                for (int i = 0; i < eventRects.size(); i++) {
                    if (isSameDay(eventRects.get(i).event.getStartTime(), day) && eventRects.get(i).event.isAllDay()) {
                        containsAllDayEvent = true;
                        break;
                    }
                }
                if (containsAllDayEvent) {
                    break;
                }
            }
        }
        if (containsAllDayEvent) {
            return drawingConfig.mHeaderTextHeight + (config.mAllDayEventHeight + drawingConfig.mHeaderMarginBottom);
        } else {
            return drawingConfig.mHeaderTextHeight;
        }
    }

    public void drawHeaderRowAndEvents(View view, Canvas canvas) {
        int width = view.getWidth();
        int height = view.getHeight();

        // Calculate the available width for each day.
        drawingConfig.mHeaderColumnWidth = drawingConfig.mTimeTextWidth + config.mHeaderColumnPadding * 2;
        drawingConfig.mWidthPerDay = width - drawingConfig.mHeaderColumnWidth - config.mColumnGap * (config.mNumberOfVisibleDays - 1);
        drawingConfig.mWidthPerDay = drawingConfig.mWidthPerDay / config.mNumberOfVisibleDays;

        // TODO
        // drawingConfig.mWidthPerDay = drawingConfig.mWidthPerDay;

        drawingConfig.mHeaderHeight = calculateHeaderHeight(
                data.eventRects, config.mNumberOfVisibleDays, viewState.firstVisibleDay);

        Calendar today = today();

        if (viewState.areDimensionsInvalid) {
            config.mEffectiveMinHourHeight = Math.max(config.mMinHourHeight, (int) ((height - drawingConfig.mHeaderHeight - config.mHeaderRowPadding * 2 - drawingConfig.mHeaderMarginBottom) / 24));

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
            if (config.mNumberOfVisibleDays >= 7 && today.get(Calendar.DAY_OF_WEEK) != config.mFirstDayOfWeek && config.mShowFirstDayOfWeekFirst) {
                int difference = (today.get(Calendar.DAY_OF_WEEK) - config.mFirstDayOfWeek);
                drawingConfig.mCurrentOrigin.x += (drawingConfig.mWidthPerDay + config.mColumnGap) * difference;
            }
        }

        // Calculate the new height due to the zooming.
        if (drawingConfig.mNewHourHeight > 0) {
            if (drawingConfig.mNewHourHeight < config.mEffectiveMinHourHeight) {
                drawingConfig.mNewHourHeight = config.mEffectiveMinHourHeight;
            } else if (drawingConfig.mNewHourHeight > config.mMaxHourHeight) {
                drawingConfig.mNewHourHeight = config.mMaxHourHeight;
            }

            drawingConfig.mCurrentOrigin.y = (drawingConfig.mCurrentOrigin.y / config.mHourHeight) * drawingConfig.mNewHourHeight;
            config.mHourHeight = drawingConfig.mNewHourHeight;
            drawingConfig.mNewHourHeight = -1;
        }

        // If the new mCurrentOrigin.y is invalid, make it valid.
        if (drawingConfig.mCurrentOrigin.y < height - config.mHourHeight * 24 - drawingConfig.mHeaderHeight - config.mHeaderRowPadding * 2 - drawingConfig.mHeaderMarginBottom - drawingConfig.mTimeTextHeight / 2)
            drawingConfig.mCurrentOrigin.y = height - config.mHourHeight * 24 - drawingConfig.mHeaderHeight - config.mHeaderRowPadding * 2 - drawingConfig.mHeaderMarginBottom - drawingConfig.mTimeTextHeight / 2;

        // Don't put an "else if" because it will trigger a glitch when completely zoomed out and
        // scrolling vertically.
        if (drawingConfig.mCurrentOrigin.y > 0) {
            drawingConfig.mCurrentOrigin.y = 0;
        }

        // Consider scroll offset.
        int leftDaysWithGaps = (int) -(Math.ceil(drawingConfig.mCurrentOrigin.x / (drawingConfig.mWidthPerDay + config.mColumnGap)));
        float startFromPixel = drawingConfig.mCurrentOrigin.x + (drawingConfig.mWidthPerDay + config.mColumnGap) * leftDaysWithGaps +
                drawingConfig.mHeaderColumnWidth;
        float startPixel = startFromPixel;

        // Prepare to iterate for each day.
        Calendar day = (Calendar) today.clone();
        day.add(Calendar.HOUR, 6);

        // Prepare to iterate for each hour to draw the hour lines.
        int lineCount = (int) ((height - drawingConfig.mHeaderHeight - config.mHeaderRowPadding * 2 -
                drawingConfig.mHeaderMarginBottom) / config.mHourHeight) + 1;
        lineCount = (lineCount) * (config.mNumberOfVisibleDays + 1);
        float[] hourLines = new float[lineCount * 4];

        // Clear the cache for event rectangles.
        if (data.eventRects != null) {
            for (EventRect eventRect : data.eventRects) {
                eventRect.rectF = null;
            }
        }

        canvas.save();

        // Clip to paint events only.
        canvas.clipRect(drawingConfig.mHeaderColumnWidth, drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mHeaderMarginBottom + drawingConfig.mTimeTextHeight / 2, width
                , height);

        // Iterate through each day.
        Calendar oldFirstVisibleDay = viewState.firstVisibleDay;
        viewState.firstVisibleDay = (Calendar) today.clone();
        viewState.firstVisibleDay.add(Calendar.DATE, -(Math.round(drawingConfig.mCurrentOrigin.x / (drawingConfig.mWidthPerDay + config.mColumnGap))));
        if (!viewState.firstVisibleDay.equals(oldFirstVisibleDay) && scrollListener != null) {
            scrollListener.onFirstVisibleDayChanged(viewState.firstVisibleDay, oldFirstVisibleDay);
        }
        for (int dayNumber = leftDaysWithGaps + 1;
             dayNumber <= leftDaysWithGaps + config.mNumberOfVisibleDays + 1;
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
            float start = (startPixel < drawingConfig.mHeaderColumnWidth ? drawingConfig.mHeaderColumnWidth : startPixel);
            if (drawingConfig.mWidthPerDay + startPixel - start > 0) {
                if (config.mShowDistinctPastFutureColor) {
                    boolean isWeekend = day.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || day.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
                    Paint pastPaint = isWeekend && config.mShowDistinctWeekendColor ? drawingConfig.mPastWeekendBackgroundPaint : drawingConfig.mPastBackgroundPaint;
                    Paint futurePaint = isWeekend && config.mShowDistinctWeekendColor ? drawingConfig.mFutureWeekendBackgroundPaint : drawingConfig.mFutureBackgroundPaint;
                    float startY = drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mTimeTextHeight / 2 + drawingConfig.mHeaderMarginBottom + drawingConfig.mCurrentOrigin.y;

                    if (sameDay) {
                        Calendar now = Calendar.getInstance();
                        float beforeNow = (now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0f) * config.mHourHeight;
                        canvas.drawRect(start, startY, startPixel + drawingConfig.mWidthPerDay, startY + beforeNow, pastPaint);
                        canvas.drawRect(start, startY + beforeNow, startPixel + drawingConfig.mWidthPerDay, height, futurePaint);
                    } else if (day.before(today)) {
                        canvas.drawRect(start, startY, startPixel + drawingConfig.mWidthPerDay, height, pastPaint);
                    } else {
                        canvas.drawRect(start, startY, startPixel + drawingConfig.mWidthPerDay, height, futurePaint);
                    }
                } else {
                    canvas.drawRect(start, drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mTimeTextHeight / 2 + drawingConfig.mHeaderMarginBottom, startPixel + drawingConfig.mWidthPerDay, height, sameDay ? drawingConfig.mTodayBackgroundPaint : drawingConfig.mDayBackgroundPaint);
                }
            }

            // Prepare the separator lines for hours.
            int i = 0;
            for (int hourNumber = 0; hourNumber < 24; hourNumber++) {
                float top = drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mCurrentOrigin.y + config.mHourHeight * hourNumber + drawingConfig.mTimeTextHeight / 2 + drawingConfig.mHeaderMarginBottom;
                if (top > drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mTimeTextHeight / 2 + drawingConfig.mHeaderMarginBottom - config.mHourSeparatorStrokeWidth && top < height && startPixel + drawingConfig.mWidthPerDay - start > 0) {
                    hourLines[i * 4] = start;
                    hourLines[i * 4 + 1] = top;
                    hourLines[i * 4 + 2] = startPixel + drawingConfig.mWidthPerDay;
                    hourLines[i * 4 + 3] = top;
                    i++;
                }
            }

            // Draw the lines for hours.
            canvas.drawLines(hourLines, drawingConfig.mHourSeparatorPaint);

            // Draw the events.
            if (config.mNumberOfVisibleDays == 1) {
                startPixel = startPixel + config.mEventMarginHorizontal;
            }

            eventsDrawer.draw(data.eventRects, width, height, day, startPixel, canvas);

            // Draw the line at the current time.
            if (config.mShowNowLine && sameDay) {
                float startY = drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mTimeTextHeight / 2 + drawingConfig.mHeaderMarginBottom + drawingConfig.mCurrentOrigin.y;
                Calendar now = Calendar.getInstance();
                float beforeNow = (now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0f) * config.mHourHeight;
                canvas.drawLine(start, startY + beforeNow, startPixel + drawingConfig.mWidthPerDay, startY + beforeNow, drawingConfig.mNowLinePaint);
            }

            // In the next iteration, start from the next day.
            startPixel += drawingConfig.mWidthPerDay + config.mColumnGap;
        }

        canvas.restore();
        canvas.save();

        // Hide everything in the first cell (top left corner).
        canvas.clipRect(0, 0, drawingConfig.mTimeTextWidth + config.mHeaderColumnPadding * 2, drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2);

        canvas.drawRect(0, 0, drawingConfig.mTimeTextWidth + config.mHeaderColumnPadding * 2, drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2, drawingConfig.mHeaderBackgroundPaint);

        canvas.restore();
        canvas.save();

        // Clip to paint header row only.
        canvas.clipRect(drawingConfig.mHeaderColumnWidth, 0, width
                , drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2);

        // Draw the header background.
        canvas.drawRect(0, 0, width
                , drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2, drawingConfig.mHeaderBackgroundPaint);

        // Draw the header row texts.
        startPixel = startFromPixel;
        for (int dayNumber = leftDaysWithGaps + 1; dayNumber <= leftDaysWithGaps + config.mNumberOfVisibleDays + 1; dayNumber++) {
            // Check if the day is today.
            day = (Calendar) today.clone();
            day.add(Calendar.DATE, dayNumber - 1);
            boolean sameDay = isSameDay(day, today);

            // Draw the day labels.
            String dayLabel = drawingConfig.mDateTimeInterpreter.interpretDate(day);
            if (dayLabel == null) {
                throw new IllegalStateException("A DateTimeInterpreter must not return null date");
            }

            canvas.drawText(dayLabel, startPixel + drawingConfig.mWidthPerDay / 2, drawingConfig.mHeaderTextHeight + config.mHeaderRowPadding, sameDay ? drawingConfig.mTodayHeaderTextPaint : drawingConfig.mHeaderTextPaint);

            if (config.mNumberOfVisibleDays == 1) {
                startPixel = startPixel + config.mEventMarginHorizontal;
            }

            eventsDrawer.drawAllDayEvents(data.eventRects, width, height, day, startPixel, canvas);
            startPixel += drawingConfig.mWidthPerDay + config.mColumnGap;
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
            data.eventRects.clear();
            data.previousPeriodEvents = null;
            data.currentPeriodEvents = null;
            data.nextPeriodEvents = null;
            data.fetchedPeriod = -1;
        }

        if (weekViewLoader != null) {
            int periodToFetch = (int) weekViewLoader.toWeekViewPeriodIndex(day);
            if (!view.isInEditMode() && (data.fetchedPeriod < 0 || data.fetchedPeriod != periodToFetch || viewState.shouldRefreshEvents)) {
                List<? extends WeekViewEvent> previousPeriodEvents = null;
                List<? extends WeekViewEvent> currentPeriodEvents = null;
                List<? extends WeekViewEvent> nextPeriodEvents = null;

                if (data.previousPeriodEvents != null && data.currentPeriodEvents != null && data.nextPeriodEvents != null) {
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
                data.eventRects.clear();
                data.sortAndCacheEvents(previousPeriodEvents);
                data.sortAndCacheEvents(currentPeriodEvents);
                data.sortAndCacheEvents(nextPeriodEvents);

                drawingConfig.mHeaderHeight = calculateHeaderHeight(
                        data.eventRects, config.mNumberOfVisibleDays, viewState.firstVisibleDay);

                data.previousPeriodEvents = previousPeriodEvents;
                data.currentPeriodEvents = currentPeriodEvents;
                data.nextPeriodEvents = nextPeriodEvents;
                data.fetchedPeriod = periodToFetch;
            }
        }

        // Prepare to calculate positions of each events.
        List<EventRect> tempEvents = data.eventRects;
        data.eventRects = new ArrayList<>();

        // Iterate through each day with events to calculate the position of the events.
        while (tempEvents.size() > 0) {
            ArrayList<EventRect> eventRects = new ArrayList<>(tempEvents.size());

            // Get first event for a day.
            EventRect eventRect1 = tempEvents.remove(0);
            eventRects.add(eventRect1);

            int i = 0;
            while (i < tempEvents.size()) {
                // Collect all other events for same day.
                EventRect eventRect2 = tempEvents.get(i);
                if (isSameDay(eventRect1.event.getStartTime(), eventRect2.event.getStartTime())) {
                    tempEvents.remove(i);
                    eventRects.add(eventRect2);
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
                    if (isEventsCollide(groupEvent.event, eventRect.event)
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
                } else if (!isEventsCollide(eventRect.event, column.get(column.size() - 1).event)) {
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
                        eventRect.bottom = config.mAllDayEventHeight;
                    }
                    data.eventRects.add(eventRect);
                }
                j++;
            }
        }
    }

    /**
     * Checks if two events overlap.
     *
     * @param event1 The first event.
     * @param event2 The second event.
     * @return true if the events overlap.
     */
    private boolean isEventsCollide(WeekViewEvent event1, WeekViewEvent event2) {
        long start1 = event1.getStartTime().getTimeInMillis();
        long end1 = event1.getEndTime().getTimeInMillis();
        long start2 = event2.getStartTime().getTimeInMillis();
        long end2 = event2.getEndTime().getTimeInMillis();
        return !((start1 >= end2) || (end1 <= start2));
    }

    public interface Listener {

        void goToDate(Calendar date);

        void goToHour(double hour); // TODO: Use JodaTime for this

    }

}

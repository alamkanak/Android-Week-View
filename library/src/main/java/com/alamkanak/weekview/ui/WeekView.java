package com.alamkanak.weekview.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import com.alamkanak.weekview.data.EventChipsProvider;
import com.alamkanak.weekview.data.MonthLoader;
import com.alamkanak.weekview.data.WeekViewLoader;
import com.alamkanak.weekview.drawing.BackgroundGridDrawer;
import com.alamkanak.weekview.drawing.DayBackgroundDrawer;
import com.alamkanak.weekview.drawing.DayLabelDrawer;
import com.alamkanak.weekview.drawing.DrawingContext;
import com.alamkanak.weekview.drawing.EventsDrawer;
import com.alamkanak.weekview.drawing.HeaderRowDrawer;
import com.alamkanak.weekview.drawing.NowLineDrawer;
import com.alamkanak.weekview.drawing.TimeColumnDrawer;
import com.alamkanak.weekview.drawing.WeekViewDrawingConfig;
import com.alamkanak.weekview.listeners.EmptyViewClickListener;
import com.alamkanak.weekview.listeners.EmptyViewLongPressListener;
import com.alamkanak.weekview.listeners.EventClickListener;
import com.alamkanak.weekview.listeners.EventLongPressListener;
import com.alamkanak.weekview.listeners.ScrollListener;
import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.model.WeekViewData;
import com.alamkanak.weekview.model.WeekViewViewState;
import com.alamkanak.weekview.scrolling.WeekViewGestureHandler;
import com.alamkanak.weekview.utils.DateTimeInterpreter;
import com.alamkanak.weekview.utils.DateUtils;

import java.util.Calendar;

import static com.alamkanak.weekview.utils.Constants.HOURS_PER_DAY;
import static com.alamkanak.weekview.utils.DateUtils.today;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_WEEK;

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://alamkanak.github.io/
 */
public class WeekView extends View implements WeekViewGestureHandler.Listener {

    private static int width;
    private static int height;

    public enum Direction {
        NONE, LEFT, RIGHT, VERTICAL
    }

    private WeekViewConfig config;
    private WeekViewData data;

    private WeekViewViewState viewState;
    private WeekViewGestureHandler gestureHandler;

    private HeaderRowDrawer headerRowDrawer;
    private DayLabelDrawer dayLabelDrawer;
    private EventsDrawer eventsDrawer;
    private TimeColumnDrawer timeColumnDrawer;
    private DayBackgroundDrawer dayBackgroundDrawer;
    private BackgroundGridDrawer backgroundGridDrawer;
    private NowLineDrawer nowLineDrawer;

    private EventChipsProvider eventChipsProvider;

    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        config = new WeekViewConfig(context, attrs);

        data = new WeekViewData();
        viewState = new WeekViewViewState();

        config.drawingConfig = new WeekViewDrawingConfig(context, config);
        gestureHandler = new WeekViewGestureHandler(context, this, config, data);

        eventsDrawer = new EventsDrawer(config);
        timeColumnDrawer = new TimeColumnDrawer(config);

        headerRowDrawer = new HeaderRowDrawer(config, data, viewState);
        dayLabelDrawer = new DayLabelDrawer(config);

        dayBackgroundDrawer = new DayBackgroundDrawer(config);
        backgroundGridDrawer = new BackgroundGridDrawer(config);
        nowLineDrawer = new NowLineDrawer(config);

        eventChipsProvider = new EventChipsProvider(config, data, viewState);
        eventChipsProvider.setWeekViewLoader(getWeekViewLoader());
    }

    public static int getViewWidth() {
        return width;
    }

    public static int getViewHeight() {
        return height;
    }

    // fix rotation changes
    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        viewState.areDimensionsInvalid = true;

        WeekView.width = width;
        WeekView.height = height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: Move to DrawingConfig
        scrollToDateAndHourIfNecessary();
        moveCurrentOriginIfFirstDraw();
        calculateNewHourHeighAfterZoomingIfNecessary();
        updateVerticalOriginIfNecessary();
        notifyScrollListeners();

        prepareEventDrawing(canvas);

        final DrawingContext drawingContext = DrawingContext.create(config);
        eventChipsProvider.loadEventsIfNecessary(drawingContext.dayRange);

        dayBackgroundDrawer.draw(drawingContext, canvas);
        backgroundGridDrawer.draw(drawingContext, canvas);

        eventsDrawer.drawSingleEvents(data.getNormalEventChips(), drawingContext, canvas);

        nowLineDrawer.draw(drawingContext, canvas);
        headerRowDrawer.draw(canvas);
        dayLabelDrawer.draw(drawingContext, canvas);

        // TODO Unify with drawSingleDayEvents()
        eventsDrawer.drawAllDayEvents(data.getAllDayEventChips(), drawingContext, canvas);

        timeColumnDrawer.drawTimeColumn(canvas);
    }

    private void scrollToDateAndHourIfNecessary() {
        final WeekViewDrawingConfig drawConfig = config.drawingConfig;

        int height = WeekView.getViewHeight();

        if (!viewState.areDimensionsInvalid) {
            return;
        }

        // TODO: Why here?
        config.effectiveMinHourHeight = max(config.minHourHeight, (int) ((height - drawConfig.headerHeight - config.headerRowPadding * 2 - drawConfig.headerMarginBottom) / 24));

        viewState.areDimensionsInvalid = false;
        if (viewState.scrollToDay != null) {
            goToDate(viewState.scrollToDay);
        }

        viewState.areDimensionsInvalid = false;
        if (viewState.scrollToHour >= 0) {
            goToHour(viewState.scrollToHour);
        }

        viewState.scrollToDay = null;
        viewState.scrollToHour = -1;
        viewState.areDimensionsInvalid = false;
    }

    private void moveCurrentOriginIfFirstDraw() {
        final WeekViewDrawingConfig drawConfig = config.drawingConfig;
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
        final WeekViewDrawingConfig drawConfig = config.drawingConfig;

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
        final WeekViewDrawingConfig drawConfig = config.drawingConfig;

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

    private void notifyScrollListeners() {
        final WeekViewDrawingConfig drawConfig = config.drawingConfig;

        // Iterate through each day.
        Calendar oldFirstVisibleDay = viewState.firstVisibleDay;
        Calendar today = today();

        viewState.firstVisibleDay = (Calendar) today.clone();

        float totalDayWidth = drawConfig.widthPerDay + config.columnGap;
        viewState.firstVisibleDay.add(DATE, round(drawConfig.currentOrigin.x / totalDayWidth) * -1);

        boolean hasFirstVisibleDayChanged = !viewState.firstVisibleDay.equals(oldFirstVisibleDay);
        if (hasFirstVisibleDayChanged && getScrollListener() != null) {
            getScrollListener().onFirstVisibleDayChanged(viewState.firstVisibleDay, oldFirstVisibleDay);
        }
    }

    private void clipEventsRect(Canvas canvas) {
        final WeekViewDrawingConfig drawConfig = config.drawingConfig;

        int width = WeekView.getViewWidth();
        int height = WeekView.getViewHeight();

        // Clip to paint events only.
        float headerHeight = drawConfig.headerHeight
                + config.headerRowPadding * 2
                + drawConfig.headerMarginBottom;

        float halfTextHeight = drawConfig.timeTextHeight / 2;
        canvas.clipRect(drawConfig.headerColumnWidth, headerHeight + halfTextHeight, width, height);
    }

    private void prepareEventDrawing(Canvas canvas) {
        // Clear the cache for event rectangles.
        data.clearEventChipsCache();
        canvas.save();
        clipEventsRect(canvas);
    }

    @Override
    public void onScaled() {
        invalidate();
    }

    @Override
    public void onScrolled() {
        postInvalidateOnAnimation();
    }

    @Override
    public void performHapticFeedback() {
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        viewState.invalidate();
    }

    /////////////////////////////////////////////////////////////////
    //
    //   Getters & Setters
    //
    /////////////////////////////////////////////////////////////////

    public void setOnEventClickListener(EventClickListener listener) {
        gestureHandler.setEventClickListener(listener);
    }

    public EventClickListener getEventClickListener() {
        return gestureHandler.getEventClickListener();
    }

    @Nullable
    public MonthLoader.MonthChangeListener getMonthChangeListener() {
        if (gestureHandler.getWeekViewLoader() instanceof MonthLoader) {
            return ((MonthLoader) gestureHandler.getWeekViewLoader()).getOnMonthChangeListener();
        }
        return null;
    }

    public void setMonthChangeListener(@Nullable MonthLoader.MonthChangeListener monthChangeListener) {
        WeekViewLoader weekViewLoader = new MonthLoader(monthChangeListener);
        gestureHandler.setWeekViewLoader(weekViewLoader);
        eventChipsProvider.setWeekViewLoader(weekViewLoader);
    }

    /**
     * Get event loader in the week view. Event loaders define the  interval after which the events
     * are loaded in week view. For a MonthLoader events are loaded for every month. You can define
     * your custom event loader by extending WeekViewLoader.
     *
     * @return The event loader.
     */
    public WeekViewLoader getWeekViewLoader() {
        return gestureHandler.getWeekViewLoader();
    }

    /**
     * Set event loader in the week view. For example, a MonthLoader. Event loaders define the
     * interval after which the events are loaded in week view. For a MonthLoader events are loaded
     * for every month. You can define your custom event loader by extending WeekViewLoader.
     *
     * @param weekViewLoader The event loader.
     */
    public void setWeekViewLoader(WeekViewLoader weekViewLoader) {
        gestureHandler.setWeekViewLoader(weekViewLoader);
        eventChipsProvider.setWeekViewLoader(weekViewLoader);
    }

    public EventLongPressListener getEventLongPressListener() {
        return gestureHandler.getEventLongPressListener();
    }

    public void setEventLongPressListener(EventLongPressListener eventLongPressListener) {
        gestureHandler.setEventLongPressListener(eventLongPressListener);
    }

    public void setEmptyViewClickListener(EmptyViewClickListener emptyViewClickListener) {
        gestureHandler.setEmptyViewClickListener(emptyViewClickListener);
    }

    public EmptyViewClickListener getEmptyViewClickListener() {
        return gestureHandler.getEmptyViewClickListener();
    }

    public void setEmptyViewLongPressListener(EmptyViewLongPressListener emptyViewLongPressListener) {
        gestureHandler.setEmptyViewLongPressListener(emptyViewLongPressListener);
    }

    public EmptyViewLongPressListener getEmptyViewLongPressListener() {
        return gestureHandler.getEmptyViewLongPressListener();
    }

    public void setScrollListener(ScrollListener scrollListener) {
        gestureHandler.setScrollListener(scrollListener);
    }

    public ScrollListener getScrollListener() {
        return gestureHandler.getScrollListener();
    }

    /**
     * Get the interpreter which provides the text to show in the header column and the header row.
     *
     * @return The date, time interpreter.
     */
    public DateTimeInterpreter getDateTimeInterpreter() {
        return config.drawingConfig.getDateTimeInterpreter(getContext());
    }

    /**
     * Set the interpreter which provides the text to show in the header column and the header row.
     *
     * @param dateTimeInterpreter The date, time interpreter.
     */
    public void setDateTimeInterpreter(DateTimeInterpreter dateTimeInterpreter) {
        config.drawingConfig.setDateTimeInterpreter(dateTimeInterpreter, getContext());
    }


    /**
     * Get the number of visible days in a week.
     *
     * @return The number of visible days in a week.
     */
    public int getNumberOfVisibleDays() {
        return config.numberOfVisibleDays;
    }

    /**
     * Set the number of visible days in a week.
     *
     * @param numberOfVisibleDays The number of visible days in a week.
     */
    public void setNumberOfVisibleDays(int numberOfVisibleDays) {
        config.setNumberOfVisibleDays(numberOfVisibleDays);
        invalidate();
    }

    public int getHourHeight() {
        return config.hourHeight;
    }

    public void setHourHeight(int hourHeight) {
        config.drawingConfig.newHourHeight = hourHeight;
        invalidate();
    }

    public int getColumnGap() {
        return config.columnGap;
    }

    public void setColumnGap(int columnGap) {
        config.columnGap = columnGap;
        invalidate();
    }

    public int getFirstDayOfWeek() {
        return config.firstDayOfWeek;
    }

    /**
     * Set the first day of the week. First day of the week is used only when the week view is first
     * drawn. It does not of any effect after user starts scrolling horizontally.
     * <p>
     * <b>Note:</b> This method will only work if the week view is set to display more than 6 days at
     * once.
     * </p>
     *
     * @param firstDayOfWeek The supported values are {@link java.util.Calendar#SUNDAY},
     *                       {@link java.util.Calendar#MONDAY}, {@link java.util.Calendar#TUESDAY},
     *                       {@link java.util.Calendar#WEDNESDAY}, {@link java.util.Calendar#THURSDAY},
     *                       {@link java.util.Calendar#FRIDAY}.
     */
    public void setFirstDayOfWeek(int firstDayOfWeek) {
        config.firstDayOfWeek = firstDayOfWeek;
        invalidate();
    }

    public boolean isShowFirstDayOfWeekFirst() {
        return config.showFirstDayOfWeekFirst;
    }

    public void setShowFirstDayOfWeekFirst(boolean show) {
        config.showFirstDayOfWeekFirst = show;
    }

    public int getTextSize() {
        return config.textSize;
    }

    public void setTextSize(int textSize) {
        config.setTextSize(textSize);
        invalidate();
    }

    public int getHeaderColumnPadding() {
        return config.headerColumnPadding;
    }

    public void setHeaderColumnPadding(int headerColumnPadding) {
        config.headerColumnPadding = headerColumnPadding;
        invalidate();
    }

    public int getHeaderColumnTextColor() {
        return config.headerColumnTextColor;
    }

    public void setHeaderColumnTextColor(int headerColumnTextColor) {
        config.setHeaderColumnTextColor(headerColumnTextColor);
        invalidate();
    }

    public int getHeaderRowPadding() {
        return config.headerRowPadding;
    }

    public void setHeaderRowPadding(int headerRowPadding) {
        config.headerRowPadding = headerRowPadding;
        invalidate();
    }

    public int getHeaderRowBackgroundColor() {
        return config.headerRowBackgroundColor;
    }

    public void setHeaderRowBackgroundColor(int headerRowBackgroundColor) {
        config.setHeaderColumnTextColor(headerRowBackgroundColor);
        invalidate();
    }

    public int getDayBackgroundColor() {
        return config.dayBackgroundColor;
    }

    public void setDayBackgroundColor(int dayBackgroundColor) {
        config.setDayBackgroundColor(dayBackgroundColor);
        invalidate();
    }

    public int getHourSeparatorColor() {
        return config.hourSeparatorColor;
    }

    public void setHourSeparatorColor(int hourSeparatorColor) {
        config.hourSeparatorColor = hourSeparatorColor;
        config.drawingConfig.hourSeparatorPaint.setColor(hourSeparatorColor);
        invalidate();
    }

    public int getTodayBackgroundColor() {
        return config.todayBackgroundColor;
    }

    public void setTodayBackgroundColor(int todayBackgroundColor) {
        config.setTodayBackgroundColor(todayBackgroundColor);
        invalidate();
    }

    public int getHourSeparatorStrokeWidth() {
        return config.hourSeparatorStrokeWidth;
    }

    public void setHourSeparatorStrokeWidth(int hourSeparatorWidth) {
        config.setHourSeparatorStrokeWidth(hourSeparatorWidth);
        invalidate();
    }

    public int getTodayHeaderTextColor() {
        return config.todayHeaderTextColor;
    }

    public void setTodayHeaderTextColor(int todayHeaderTextColor) {
        config.setTodayHeaderTextColor(todayHeaderTextColor);
        invalidate();
    }

    public int getEventTextSize() {
        return config.eventTextSize;
    }

    public void setEventTextSize(int eventTextSize) {
        config.setEventTextSize(eventTextSize);
        invalidate();
    }

    public int getEventTextColor() {
        return config.eventTextColor;
    }

    public void setEventTextColor(int eventTextColor) {
        config.setEventTextColor(eventTextColor);
        invalidate();
    }

    public int getEventPadding() {
        return config.eventPadding;
    }

    public void setEventPadding(int eventPadding) {
        config.eventPadding = eventPadding;
        invalidate();
    }

    public int getHeaderColumnBackgroundColor() {
        return config.headerColumnBackgroundColor;
    }

    public void setHeaderColumnBackgroundColor(int headerColumnBackgroundColor) {
        config.setHeaderColumnBackgroundColor(headerColumnBackgroundColor);
        invalidate();
    }

    public int getDefaultEventColor() {
        return config.drawingConfig.defaultEventColor;
    }

    public void setDefaultEventColor(int defaultEventColor) {
        config.drawingConfig.defaultEventColor = defaultEventColor;
        invalidate();
    }

    public int getOverlappingEventGap() {
        return config.overlappingEventGap;
    }

    /**
     * Set the gap between overlapping events.
     *
     * @param overlappingEventGap The gap between overlapping events.
     */
    public void setOverlappingEventGap(int overlappingEventGap) {
        config.overlappingEventGap = overlappingEventGap;
        invalidate();
    }

    public int getEventCornerRadius() {
        return config.eventCornerRadius;
    }

    /**
     * Set corner radius for event rect.
     *
     * @param eventCornerRadius the radius in px.
     */
    public void setEventCornerRadius(int eventCornerRadius) {
        config.eventCornerRadius = eventCornerRadius;
    }

    public int getEventMarginVertical() {
        return config.eventMarginVertical;
    }

    /**
     * Set the top and bottom margin of the event. The event will release this margin from the top
     * and bottom edge. This margin is useful for differentiation consecutive events.
     *
     * @param eventMarginVertical The top and bottom margin.
     */
    public void setEventMarginVertical(int eventMarginVertical) {
        config.eventMarginVertical = eventMarginVertical;
        invalidate();
    }

    /**
     * Set the start and end margin of the event. The event will release this margin from the start
     * and end edge.
     *
     * @param eventMarginHorizontal The start and end margin.
     */
    public void setEventMarginHorizontal(int eventMarginHorizontal) {
        config.eventMarginHorizontal = eventMarginHorizontal;
        invalidate();
    }

    public int getEventMarginHorizontal() {
        return config.eventMarginHorizontal;
    }

    /**
     * Returns the first visible day in the week view.
     *
     * @return The first visible day in the week view.
     */
    public Calendar getFirstVisibleDay() {
        return viewState.firstVisibleDay;
    }

    /**
     * Returns the last visible day in the week view.
     *
     * @return The last visible day in the week view.
     */
    public Calendar getLastVisibleDay() {
        return viewState.lastVisibleDay;
    }

    /**
     * Get the scrolling speed factor in horizontal direction.
     *
     * @return The speed factor in horizontal direction.
     */
    public float getXScrollingSpeed() {
        return config.xScrollingSpeed;
    }

    /**
     * Sets the speed for horizontal scrolling.
     *
     * @param xScrollingSpeed The new horizontal scrolling speed.
     */
    public void setXScrollingSpeed(float xScrollingSpeed) {
        config.xScrollingSpeed = xScrollingSpeed;
    }

    /**
     * Whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     *
     * @return True if weekends should have different background colors.
     */
    public boolean isShowDistinctWeekendColor() {
        return config.showDistinctWeekendColor;
    }

    /**
     * Set whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     *
     * @param showDistinctWeekendColor True if weekends should have different background colors.
     */
    public void setShowDistinctWeekendColor(boolean showDistinctWeekendColor) {
        config.showDistinctWeekendColor = showDistinctWeekendColor;
        invalidate();
    }

    /**
     * Whether past and future days should have two different background colors. The past and
     * future day colors are defined by the attributes `futureBackgroundColor` and
     * `pastBackgroundColor`.
     *
     * @return True if past and future days should have two different background colors.
     */
    public boolean isShowDistinctPastFutureColor() {
        return config.showDistinctPastFutureColor;
    }

    /**
     * Set whether weekends should have a background color different from the normal day background
     * color. The past and future day colors are defined by the attributes `futureBackgroundColor`
     * and `pastBackgroundColor`.
     *
     * @param showDistinctPastFutureColor True if past and future should have two different
     *                                    background colors.
     */
    public void setShowDistinctPastFutureColor(boolean showDistinctPastFutureColor) {
        config.showDistinctPastFutureColor = showDistinctPastFutureColor;
        invalidate();
    }

    /**
     * Get whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineThickness`.
     *
     * @return True if "now" line should be displayed.
     */
    public boolean isShowNowLine() {
        return config.showNowLine;
    }

    /**
     * Set whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineThickness`.
     *
     * @param showNowLine True if "now" line should be displayed.
     */
    public void setShowNowLine(boolean showNowLine) {
        config.showNowLine = showNowLine;
        invalidate();
    }

    /**
     * Get the "now" line color.
     *
     * @return The color of the "now" line.
     */
    public int getNowLineColor() {
        return config.nowLineColor;
    }

    /**
     * Set the "now" line color.
     *
     * @param nowLineColor The color of the "now" line.
     */
    public void setNowLineColor(int nowLineColor) {
        config.nowLineColor = nowLineColor;
        invalidate();
    }

    /**
     * Get the "now" line thickness.
     *
     * @return The thickness of the "now" line.
     */
    public int getNowLineThickness() {
        return config.nowLineThickness;
    }

    /**
     * Set the "now" line thickness.
     *
     * @param nowLineThickness The thickness of the "now" line.
     */
    public void setNowLineThickness(int nowLineThickness) {
        config.nowLineThickness = nowLineThickness;
        invalidate();
    }

    /**
     * Get whether the week view should fling horizontally.
     *
     * @return True if the week view has horizontal fling enabled.
     */
    public boolean isHorizontalFlingEnabled() {
        return config.horizontalFlingEnabled;
    }

    /**
     * Set whether the week view should fling horizontally.
     */
    public void setHorizontalFlingEnabled(boolean enabled) {
        config.horizontalFlingEnabled = enabled;
    }

    /**
     * Get whether the week view should fling vertically.
     *
     * @return True if the week view has vertical fling enabled.
     */
    public boolean isVerticalFlingEnabled() {
        return config.verticalFlingEnabled;
    }

    /**
     * Set whether the week view should fling vertically.
     */
    public void setVerticalFlingEnabled(boolean enabled) {
        config.verticalFlingEnabled = enabled;
    }

    /**
     * Get the height of AllDay-events.
     *
     * @return Height of AllDay-events.
     */
    public int getAllDayEventHeight() {
        return config.allDayEventHeight;
    }

    /**
     * Set the height of AllDay-events.
     */
    public void setAllDayEventHeight(int height) {
        config.allDayEventHeight = height;
    }

    /**
     * Get scroll duration
     *
     * @return scroll duration
     */
    public int getScrollDuration() {
        return config.scrollDuration;
    }

    /**
     * Set the scroll duration
     */
    public void setScrollDuration(int scrollDuration) {
        config.scrollDuration = scrollDuration;
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to scrolling.
    //
    /////////////////////////////////////////////////////////////////

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureHandler.onTouchEvent(event);
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        gestureHandler.computeScroll();
    }


    /////////////////////////////////////////////////////////////////
    //
    //      Public methods.
    //
    /////////////////////////////////////////////////////////////////

    /**
     * Show today on the week view.
     */
    public void goToToday() {
        Calendar today = Calendar.getInstance();
        goToDate(today);
    }

    public void goToCurrentTime() {
        // TODO
    }

    /**
     * Show a specific day on the week view.
     *
     * @param date The date to show.
     */
    public void goToDate(Calendar date) {
        gestureHandler.scroller.forceFinished(true);
        gestureHandler.currentScrollDirection = gestureHandler.currentFlingDirection = Direction.NONE;

        date = DateUtils.withTimeAtStartOfDay(date);

        if (viewState.areDimensionsInvalid) {
            viewState.scrollToDay = date;
            return;
        }

        viewState.shouldRefreshEvents = true;

        int diff = DateUtils.getDaysUntilDate(date);
        config.drawingConfig.currentOrigin.x = diff * (-1) * config.getTotalDayWidth();
        invalidate();
    }

    /**
     * Refreshes the view and loads the events again.
     */
    public void notifyDataSetChanged() {
        viewState.shouldRefreshEvents = true;
        invalidate();
    }

    /**
     * Vertically scroll to a specific hour in the week view.
     *
     * @param hour The hour to scroll to in 24-hour format. Supported values are 0-24.
     */
    public void goToHour(int hour) {
        if (viewState.areDimensionsInvalid) {
            viewState.scrollToHour = hour;
            return;
        }

        hour = (int) Math.min(hour, HOURS_PER_DAY);
        int verticalOffset = config.hourHeight * hour;

        float dayHeight = config.getTotalDayHeight();
        double viewHeight = getHeight();

        double desiredOffset = dayHeight - viewHeight;
        verticalOffset = (int) Math.min(desiredOffset, verticalOffset);

        config.drawingConfig.currentOrigin.y = -verticalOffset;
        invalidate();
    }

    /**
     * Get the first hour that is visible on the screen.
     *
     * @return The first hour that is visible.
     */
    public double getFirstVisibleHour() {
        return (config.drawingConfig.currentOrigin.y * -1) / config.hourHeight;
    }

}

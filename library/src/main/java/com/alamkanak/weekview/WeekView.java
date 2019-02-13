package com.alamkanak.weekview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import java.util.Calendar;
import java.util.List;

import static com.alamkanak.weekview.Constants.HOURS_PER_DAY;
import static com.alamkanak.weekview.DateUtils.today;
import static java.lang.Math.round;
import static java.lang.Math.min;
import static java.util.Calendar.DATE;
import static java.util.Calendar.HOUR_OF_DAY;

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://alamkanak.github.io/
 */
public final class WeekView<T> extends View
        implements WeekViewGestureHandler.Listener, WeekViewViewState.UpdateListener {

    private static int width;
    private static int height;

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawConfig;
    private WeekViewData<T> data;

    private WeekViewViewState viewState;
    private WeekViewGestureHandler<T> gestureHandler;

    private HeaderRowDrawer<T> headerRowDrawer;
    private DayLabelDrawer dayLabelDrawer;
    private EventsDrawer<T> eventsDrawer;
    private TimeColumnDrawer timeColumnDrawer;
    private DayBackgroundDrawer dayBackgroundDrawer;
    private BackgroundGridDrawer backgroundGridDrawer;
    private NowLineDrawer nowLineDrawer;

    private EventChipsProvider<T> eventChipsProvider;

    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        config = new WeekViewConfig(context, attrs);
        drawConfig = new WeekViewDrawingConfig(context, config);
        config.drawingConfig = drawConfig;

        data = new WeekViewData<>();
        viewState = new WeekViewViewState();

        gestureHandler = new WeekViewGestureHandler<>(context, this, config, data);

        eventsDrawer = new EventsDrawer<>(config);
        timeColumnDrawer = new TimeColumnDrawer(config);

        headerRowDrawer = new HeaderRowDrawer<>(config, data, viewState);
        dayLabelDrawer = new DayLabelDrawer(config);

        dayBackgroundDrawer = new DayBackgroundDrawer(config);
        backgroundGridDrawer = new BackgroundGridDrawer(config);
        nowLineDrawer = new NowLineDrawer(config);

        eventChipsProvider = new EventChipsProvider<>(config, data, viewState);
        eventChipsProvider.setWeekViewLoader(getWeekViewLoader());
    }

    public static int getViewWidth() {
        return width;
    }

    public static int getViewHeight() {
        return height;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, config.numberOfVisibleDays);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        config.setNumberOfVisibleDays(savedState.numberOfVisibleDays);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        viewState.areDimensionsInvalid = true;

        WeekView.width = width;
        WeekView.height = height;

        if (config.showCompleteDay) {
          config.hourHeight = (height - drawConfig.headerHeight) / HOURS_PER_DAY;
          drawConfig.newHourHeight = config.hourHeight;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final boolean isFirstDraw = viewState.isFirstDraw();

        calculateWidthPerDay();

        viewState.update(config, this);

        config.drawingConfig.refreshAfterZooming(config);
        config.drawingConfig.updateVerticalOrigin(config);

        notifyScrollListeners();
        prepareEventDrawing(canvas);

        if (viewState.isFirstDraw()) {
            viewState.setFirstDraw(false);
            config.drawingConfig.moveCurrentOriginIfFirstDraw(config);
        }

        final DrawingContext drawingContext = DrawingContext.create(config);
        eventChipsProvider.loadEventsIfNecessary(this, drawingContext.getDayRange());

        List<Pair<EventChip<T>, StaticLayout>> allDayEvents =
                eventsDrawer.prepareDrawAllDayEvents(data.getAllDayEventChips(), drawingContext);

        dayBackgroundDrawer.draw(drawingContext, canvas);
        backgroundGridDrawer.draw(drawingContext, canvas);

        eventsDrawer.drawSingleEvents(data.getNormalEventChips(), drawingContext, canvas);

        nowLineDrawer.draw(drawingContext, canvas);
        headerRowDrawer.draw(canvas);
        dayLabelDrawer.draw(drawingContext, canvas);

        eventsDrawer.drawAllDayEvents(allDayEvents, canvas);

        timeColumnDrawer.drawTimeColumn(canvas);

        if (isFirstDraw) {
            // Temporary workaround to make sure that the events are actually being displayed
            invalidate();
        }

        if (viewState.requiresPostInvalidateOnAnimation) {
            viewState.requiresPostInvalidateOnAnimation = false;
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void notifyScrollListeners() {
        // Iterate through each day.
        final Calendar oldFirstVisibleDay = viewState.getFirstVisibleDay();
        final Calendar today = today();

        viewState.setFirstVisibleDay((Calendar) today.clone());
        viewState.setLastVisibleDay((Calendar) today.clone());

        final float totalDayWidth = config.getTotalDayWidth();
        final int delta = round(drawConfig.currentOrigin.x / totalDayWidth) * -1;
        viewState.getFirstVisibleDay().add(DATE, delta);
        viewState.getLastVisibleDay().add(DATE, config.numberOfVisibleDays - 1 + delta);

        final boolean hasFirstVisibleDayChanged = !viewState.getFirstVisibleDay().equals(oldFirstVisibleDay);
        if (hasFirstVisibleDayChanged && getScrollListener() != null) {
            getScrollListener().onFirstVisibleDayChanged(viewState.getFirstVisibleDay(), oldFirstVisibleDay);
        }
    }

    private void prepareEventDrawing(Canvas canvas) {
        // Clear the cache for event rectangles.
        data.clearEventChipsCache();
        canvas.save();
        clipEventsRect(canvas);
    }

    private void calculateWidthPerDay() {
        // Initialize drawConfig.timeColumnWidth at first call
        if (drawConfig.timeColumnWidth == 0) {
            drawConfig.timeColumnWidth = drawConfig.timeTextWidth + config.timeColumnPadding * 2;
        }
        // Calculate the available width for each day
        drawConfig.widthPerDay = getWidth()
                - drawConfig.timeColumnWidth
                - config.columnGap * (config.numberOfVisibleDays - 1);
        drawConfig.widthPerDay = drawConfig.widthPerDay / config.numberOfVisibleDays;
    }

    private void clipEventsRect(Canvas canvas) {
        final int width = WeekView.getViewWidth();
        final int height = WeekView.getViewHeight();

        // Clip to paint events only.
        canvas.clipRect(drawConfig.timeColumnWidth, drawConfig.headerHeight, width, height);
    }

    @Override
    public void onScaled() {
        invalidate();
    }

    @Override
    public void onScrolled() {
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        viewState.invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Calendar configuration
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public int getFirstDayOfWeek() {
        return config.firstDayOfWeek;
    }

    /**
     * Set the first day of the week. First day of the week is used only when the week view is first
     * drawn. It does not of any effect after user starts scrolling horizontally.
     * <p>
     * <b>Note:</b> This method will only work if WeekView is set to display more than 6 days at
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

    public boolean isShowFirstDayOfWeekFirst() {
        return config.showFirstDayOfWeekFirst;
    }

    public void setShowFirstDayOfWeekFirst(boolean show) {
        config.showFirstDayOfWeekFirst = show;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Header bottom line
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean getShowHeaderRowBottomLine() {
        return config.showHeaderRowBottomLine;
    }

    public void setShowHeaderRowBottomLine(boolean showHeaderRowBottomLine) {
        config.showHeaderRowBottomLine = showHeaderRowBottomLine;
        invalidate();
    }

    public int getHeaderRowBottomLineColor() {
        return config.headerRowBottomLineColor;
    }

    public void setHeaderRowBottomLineColor(int headerRowBottomLineColor) {
        config.headerRowBottomLineColor = headerRowBottomLineColor;
        invalidate();
    }

    public int getHeaderRowBottomLineWidth() {
        return config.headerRowBottomLineWidth;
    }

    public void setHeaderRowBottomLineWidth(int headerRowBottomLineWidth) {
        config.headerRowBottomLineWidth= headerRowBottomLineWidth;
        invalidate();
    }

    public int getTodayHeaderTextColor() {
        return config.todayHeaderTextColor;
    }

    public void setTodayHeaderTextColor(int todayHeaderTextColor) {
        config.setTodayHeaderTextColor(todayHeaderTextColor);
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Time column
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public int getTimeColumnPadding() {
        return config.timeColumnPadding;
    }

    public void setTimeColumnPadding(int timeColumnPadding) {
        config.timeColumnPadding = timeColumnPadding;
        invalidate();
    }

    public int getTimeColumnTextColor() {
        return config.timeColumnTextColor;
    }

    public void setTimeColumnTextColor(int timeColumnTextColor) {
        config.setTimeColumnTextColor(timeColumnTextColor);
        invalidate();
    }

    public int getTimeColumnBackgroundColor() {
        return config.timeColumnBackgroundColor;
    }

    public void setTimeColumnBackgroundColor(int timeColumnBackgroundColor) {
        config.setTimeColumnBackgroundColor(timeColumnBackgroundColor);
        invalidate();
    }

    public int getTimeColumTextSize() {
        return config.timeColumnTextSize;
    }

    public void setTimeColumnTextSize(int textSize) {
        config.setTimeColumnTextSize(textSize);
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Time column separator
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean getShowTimeColumnSeparator() {
        return config.showTimeColumnSeparator;
    }

    public void setShowTimeColumnSeparator(boolean showTimeColumnSeparator) {
        config.showTimeColumnSeparator = showTimeColumnSeparator;
        invalidate();
    }

    public int getTimeColumnSeparatorColor() {
        return config.timeColumnSeparatorColor;
    }

    public void setTimeColumnSeparatorColor(int timeColumnSeparatorColor) {
        config.timeColumnSeparatorColor = timeColumnSeparatorColor;
        invalidate();
    }

    public int getTimeColumnSeparatorWidth() {
        return config.timeColumnSeparatorStrokeWidth;
    }

    public void setTimeColumnSeparatorWidth(int timeColumnSeparatorStrokeWidth) {
        config.timeColumnSeparatorStrokeWidth= timeColumnSeparatorStrokeWidth;
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Header row
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
        config.setHeaderRowBackgroundColor(headerRowBackgroundColor);
        invalidate();
    }

    public int getHeaderRowTextColor() {
        return config.headerRowTextColor;
    }

    public void setHeaderRowTextColor(int headerRowTextColor) {
        config.headerRowTextColor = headerRowTextColor;
        invalidate();
    }

    public int getHeaderRowTextSize() {
        return config.headerRowTextSize;
    }

    public void setHeaderRowTextSize(int textSize) {
        config.headerRowTextSize = textSize;
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Event chips
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get the max height of all-day events.
     *
     * @return max Height of all-day events.
     */
    public int getMaxAllDayEventHeight() {
        return config.maxAllDayEventHeight;
    }

    /**
     * Set the height of AllDay-events.
     */
    public void setMaxAllDayEventHeight(int height) {
        config.maxAllDayEventHeight = height;
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

    public int getDefaultEventColor() {
        return config.defaultEventColor;
    }

    public void setDefaultEventColor(int defaultEventColor) {
        config.defaultEventColor = defaultEventColor;
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Event margins
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public int getColumnGap() {
        return config.columnGap;
    }

    public void setColumnGap(int columnGap) {
        config.columnGap = columnGap;
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Colors
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public int getDayBackgroundColor() {
        return config.dayBackgroundColor;
    }

    public void setDayBackgroundColor(int dayBackgroundColor) {
        config.setDayBackgroundColor(dayBackgroundColor);
        invalidate();
    }

    public int getTodayBackgroundColor() {
        return config.todayBackgroundColor;
    }

    public void setTodayBackgroundColor(int todayBackgroundColor) {
        config.setTodayBackgroundColor(todayBackgroundColor);
        invalidate();
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

    // TODO: Past & future background color, pastWeekend and futureWeekend

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Hour height
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public float getHourHeight() {
        return config.hourHeight;
    }

    public void setHourHeight(float hourHeight) {
        config.drawingConfig.newHourHeight = hourHeight;
        invalidate();
    }

    // TODO: Min/max hour height

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Now line
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineStrokeWidth`.
     *
     * @return True if "now" line should be displayed.
     */
    public boolean isShowNowLine() {
        return config.showNowLine;
    }

    /**
     * Set whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineStrokeWidth`.
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
    public int getNowLineStrokeWidth() {
        return config.nowLineStrokeWidth;
    }

    /**
     * Set the "now" line thickness.
     *
     * @param nowLineStrokeWidth The thickness of the "now" line.
     */
    public void setNowLineStrokeWidth(int nowLineStrokeWidth) {
        config.nowLineStrokeWidth = nowLineStrokeWidth;
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Now line dot
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get whether the dot on the left-hand side of the "now" line is displayed.
     *
     * @return True if "now" line dot is be displayed.
     */
    public boolean isShowNowLineDot() {
        return config.showNowLineDot;
    }

    /**
     * Set whether the dot on the left-hand side of the "now" line should be displayed
     *
     * @param showNowLineDot True if "now" line dot should be displayed.
     */
    public void setShowNowLineDot(boolean showNowLineDot) {
        config.showNowLineDot = showNowLineDot;
        invalidate();
    }

    /**
     * Get the color of the dot on the left-hand side of the "now" line.
     *
     * @return The color of the "now" line dot.
     */
    public int getNowLineDotColor() {
        return config.nowLineDotColor;
    }

    /**
     * Set the color of the dot on the left-hand side of the "now" line.
     *
     * @param nowLineDotColor The color of the "now" line dot.
     */
    public void setNowLineDotColor(int nowLineDotColor) {
        config.nowLineDotColor = nowLineDotColor;
        invalidate();
    }

    /**
     * Get the radius of the dot on the left-hand side of the "now" line.
     *
     * @return The radius of the "now" line dot.
     */
    public int getNowLineDotRadius() {
        return config.nowLineDotRadius;
    }

    /**
     * Set the radius of the dot on the left-hand side of the "now" line.
     *
     * @param nowLineDotRadius The radius of the "now" line dot.
     */
    public void setNowLineDotRadius(int nowLineDotRadius) {
        config.nowLineDotRadius = nowLineDotRadius;
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Hour separators
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean showHourSeparators() {
        return config.showHourSeparator;
    }

    public void setShowHourSeparators(boolean showHourSeparators) {
        config.showHourSeparator = showHourSeparators;
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

    public int getHourSeparatorStrokeWidth() {
        return config.hourSeparatorStrokeWidth;
    }

    public void setHourSeparatorStrokeWidth(int hourSeparatorWidth) {
        config.setHourSeparatorStrokeWidth(hourSeparatorWidth);
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Day separators
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean showDaySeparators() {
        return config.showDaySeparator;
    }

    public void setShowDaySeparators(boolean showDaySeparators) {
        config.showDaySeparator = showDaySeparators;
        invalidate();
    }

    public int getDaySeparatorColor() {
        return config.daySeparatorColor;
    }

    public void setDaySeparatorColor(int daySeparatorColor) {
        config.daySeparatorColor = daySeparatorColor;
        config.drawingConfig.daySeparatorPaint.setColor(daySeparatorColor);
        invalidate();
    }

    public int getDaySeparatorStrokeWidth() {
        return config.daySeparatorStrokeWidth;
    }

    public void setDaySeparatorStrokeWidth(int daySeparatorWidth) {
        config.daySeparatorStrokeWidth = daySeparatorWidth;
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Scrolling
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
     * Returns whether the user can scroll horizontally. If not, the user can
     * only scroll vertically.
     *
     * @return True if horizontal scrolling is enabled. Default is true.
     */
    public boolean isHorizontalScrollingEnabled() {
        return config.horizontalScrollingEnabled;
    }

    /**
     * Sets whether the user can scroll horizontally.
     */
    public void setHorizontalScrollingEnabled(boolean enabled) {
        config.horizontalScrollingEnabled = enabled;
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
    //  Functions related to scrolling
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
    //  Public methods
    //
    /////////////////////////////////////////////////////////////////

    /**
     * Returns the first visible day in the week view.
     *
     * @return The first visible day in the week view.
     */
    public Calendar getFirstVisibleDay() {
        return viewState.getFirstVisibleDay();
    }

    /**
     * Returns the last visible day in the week view.
     *
     * @return The last visible day in the week view.
     */
    public Calendar getLastVisibleDay() {
        return viewState.getLastVisibleDay();
    }

    /**
     * Show today on the week view.
     */
    public void goToToday() {
        goToDate(today());
    }

    public void goToCurrentTime() {
        final Calendar today = Calendar.getInstance();
        final int hour = today.get(HOUR_OF_DAY);
        goToDate(today);
        goToHour(hour);
    }

    /**
     * Show a specific day on the week view.
     *
     * @param date The date to show.
     */
    public void goToDate(@NonNull Calendar date) {
        gestureHandler.forceScrollFinished();

        if (viewState.areDimensionsInvalid) {
            viewState.setScrollToDay(date);
            return;
        }

        viewState.setShouldRefreshEvents(true);

        int diff = DateUtils.getDaysUntilDate(date);

        if (config.numberOfVisibleDays >= 7 && config.showFirstDayOfWeekFirst) {
            diff -= config.drawingConfig.computeDifferenceWithFirstDayOfWeek(config, date);
        }

        config.drawingConfig.currentOrigin.x = diff * (-1) * config.getTotalDayWidth();
        viewState.requiresPostInvalidateOnAnimation = true;
        invalidate();
    }

    /**
     * Refreshes the view and loads the events again.
     */
    public void notifyDataSetChanged() {
        viewState.setShouldRefreshEvents(true);
        invalidate();
    }

    /**
     * Vertically scroll to a specific hour in the week view.
     *
     * @param hour The hour to scroll to in 24-hour format. Supported values are 0-24.
     */
    public void goToHour(int hour) {
        if (viewState.areDimensionsInvalid) {
            viewState.setScrollToHour(hour);
            return;
        }

        hour = min(hour, HOURS_PER_DAY);
        float verticalOffset = config.hourHeight * hour;

        final float dayHeight = config.getTotalDayHeight();
        final double viewHeight = getHeight();

        final double desiredOffset = dayHeight - viewHeight;
        verticalOffset = min((float)desiredOffset, verticalOffset);

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

    /////////////////////////////////////////////////////////////////
    //
    //  Listeners
    //
    /////////////////////////////////////////////////////////////////

    public void setOnEventClickListener(EventClickListener<T> listener) {
        gestureHandler.setEventClickListener(listener);
    }

    public EventClickListener getEventClickListener() {
        return gestureHandler.getEventClickListener();
    }

    @Nullable
    public MonthChangeListener getMonthChangeListener() {
        if (gestureHandler.getWeekViewLoader() instanceof MonthLoader) {
            return ((MonthLoader) gestureHandler.getWeekViewLoader()).getOnMonthChangeListener();
        }
        return null;
    }

    public void setMonthChangeListener(@Nullable MonthChangeListener<T> monthChangeListener) {
        WeekViewLoader<T> weekViewLoader = new MonthLoader<>(monthChangeListener);
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
    public WeekViewLoader<T> getWeekViewLoader() {
        return gestureHandler.getWeekViewLoader();
    }

    /**
     * Set event loader in the week view. For example, a MonthLoader. Event loaders define the
     * interval after which the events are loaded in week view. For a MonthLoader events are loaded
     * for every month. You can define your custom event loader by extending WeekViewLoader.
     *
     * @param weekViewLoader The event loader.
     */
    public void setWeekViewLoader(WeekViewLoader<T> weekViewLoader) {
        gestureHandler.setWeekViewLoader(weekViewLoader);
        eventChipsProvider.setWeekViewLoader(weekViewLoader);
    }

    public EventLongPressListener getEventLongPressListener() {
        return gestureHandler.getEventLongPressListener();
    }

    public void setEventLongPressListener(EventLongPressListener<T> eventLongPressListener) {
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

    protected static class SavedState extends BaseSavedState {

        private final int numberOfVisibleDays;

        private SavedState(Parcelable superState, int numberOfVisibleDays) {
            super(superState);
            this.numberOfVisibleDays = numberOfVisibleDays;
        }

        private SavedState(Parcel in) {
            super(in);
            numberOfVisibleDays = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel destination, int flags) {
            super.writeToParcel(destination, flags);
            destination.writeInt(numberOfVisibleDays);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

        };

    }

}

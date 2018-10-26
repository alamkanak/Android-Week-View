package com.alamkanak.weekview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import com.alamkanak.weekview.drawing.EventsDrawer;
import com.alamkanak.weekview.drawing.HeaderRowDrawer;
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
import com.alamkanak.weekview.scrolling.WeekViewScrollHandler;

import java.util.Calendar;

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://alamkanak.github.io/
 */
public class WeekView extends View implements WeekViewScrollHandler.Listener {

    public enum Direction {
        NONE, LEFT, RIGHT, VERTICAL
    }

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawingConfig;

    private WeekViewData data;
    private WeekViewViewState viewState;

    private WeekViewScrollHandler scrollHandler;

    private HeaderRowDrawer headerRowDrawer;
    private TimeColumnDrawer timeColumnDrawer;

    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        config = new WeekViewConfig(context, attrs);

        HeaderRowDrawer.Listener listener = new HeaderRowDrawer.Listener() {
            @Override
            public void goToDate(Calendar date) {
                WeekView.this.goToDate(date);
            }

            @Override
            public void goToHour(double hour) {
                WeekView.this.goToHour(hour);
            }
        };

        data = new WeekViewData();
        viewState = new WeekViewViewState();

        drawingConfig = new WeekViewDrawingConfig(context, config);
        scrollHandler = new WeekViewScrollHandler(context, this, config, drawingConfig, data);

        EventsDrawer eventsDrawer = new EventsDrawer(config, drawingConfig);
        timeColumnDrawer = new TimeColumnDrawer(config, drawingConfig);

        headerRowDrawer = new HeaderRowDrawer(listener, eventsDrawer, config, drawingConfig, data, viewState);

    }

    // fix rotation changes
    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        viewState.areDimensionsInvalid = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        headerRowDrawer.drawHeaderRowAndEvents(this, canvas);
        timeColumnDrawer.draw(canvas, getHeight());
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
    public int getViewHeight() {
        return getHeight();
    }

    @Override
    public void performHapticFeedback() {
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        viewState.areDimensionsInvalid = true;
    }

    /////////////////////////////////////////////////////////////////
    //
    //   Getters & Setters
    //
    /////////////////////////////////////////////////////////////////

    public void setOnEventClickListener(EventClickListener listener) {
        scrollHandler.eventClickListener = listener;
    }

    public EventClickListener getEventClickListener() {
        return scrollHandler.eventClickListener;
    }

    @Nullable
    public MonthLoader.MonthChangeListener getMonthChangeListener() {
        if (scrollHandler.weekViewLoader instanceof MonthLoader) {
            return ((MonthLoader) scrollHandler.weekViewLoader).getOnMonthChangeListener();
        }
        return null;
    }

    public void setMonthChangeListener(@Nullable MonthLoader.MonthChangeListener monthChangeListener) {
        scrollHandler.weekViewLoader = new MonthLoader(monthChangeListener);
        headerRowDrawer.setWeekViewLoader(scrollHandler.weekViewLoader);
    }

    /**
     * Get event loader in the week view. Event loaders define the  interval after which the events
     * are loaded in week view. For a MonthLoader events are loaded for every month. You can define
     * your custom event loader by extending WeekViewLoader.
     *
     * @return The event loader.
     */
    public WeekViewLoader getWeekViewLoader() {
        return scrollHandler.weekViewLoader;
    }

    /**
     * Set event loader in the week view. For example, a MonthLoader. Event loaders define the
     * interval after which the events are loaded in week view. For a MonthLoader events are loaded
     * for every month. You can define your custom event loader by extending WeekViewLoader.
     *
     * @param weekViewLoader The event loader.
     */
    public void setWeekViewLoader(WeekViewLoader weekViewLoader) {
        scrollHandler.weekViewLoader = weekViewLoader;
        headerRowDrawer.setWeekViewLoader(weekViewLoader);
    }

    public EventLongPressListener getEventLongPressListener() {
        return scrollHandler.eventLongPressListener;
    }

    public void setEventLongPressListener(EventLongPressListener eventLongPressListener) {
        scrollHandler.eventLongPressListener = eventLongPressListener;
    }

    public void setEmptyViewClickListener(EmptyViewClickListener emptyViewClickListener) {
        scrollHandler.emptyViewClickListener = emptyViewClickListener;
    }

    public EmptyViewClickListener getEmptyViewClickListener() {
        return scrollHandler.emptyViewClickListener;
    }

    public void setEmptyViewLongPressListener(EmptyViewLongPressListener emptyViewLongPressListener) {
        scrollHandler.emptyViewLongPressListener = emptyViewLongPressListener;
    }

    public EmptyViewLongPressListener getEmptyViewLongPressListener() {
        return scrollHandler.emptyViewLongPressListener;
    }

    public void setScrollListener(ScrollListener scrolledListener) {
        scrollHandler.scrollListener = scrolledListener;
        headerRowDrawer.setScrollListener(scrolledListener);
    }

    public ScrollListener getScrollListener() {
        return scrollHandler.scrollListener;
    }

    /**
     * Get the interpreter which provides the text to show in the header column and the header row.
     *
     * @return The date, time interpreter.
     */
    public DateTimeInterpreter getDateTimeInterpreter() {
        return drawingConfig.getDateTimeInterpreter(getContext());
    }

    /**
     * Set the interpreter which provides the text to show in the header column and the header row.
     *
     * @param dateTimeInterpreter The date, time interpreter.
     */
    public void setDateTimeInterpreter(DateTimeInterpreter dateTimeInterpreter) {
        drawingConfig.mDateTimeInterpreter = dateTimeInterpreter;

        // Refresh time column width
        drawingConfig.initTextTimeWidth(getContext());
    }


    /**
     * Get the number of visible days in a week.
     *
     * @return The number of visible days in a week.
     */
    public int getNumberOfVisibleDays() {
        return config.mNumberOfVisibleDays;
    }

    /**
     * Set the number of visible days in a week.
     *
     * @param numberOfVisibleDays The number of visible days in a week.
     */
    public void setNumberOfVisibleDays(int numberOfVisibleDays) {
        config.mNumberOfVisibleDays = numberOfVisibleDays;
        drawingConfig.mCurrentOrigin.x = 0;
        drawingConfig.mCurrentOrigin.y = 0;
        invalidate();
    }

    public int getHourHeight() {
        return config.mHourHeight;
    }

    public void setHourHeight(int hourHeight) {
        drawingConfig.mNewHourHeight = hourHeight;
        invalidate();
    }

    public int getColumnGap() {
        return config.mColumnGap;
    }

    public void setColumnGap(int columnGap) {
        config.mColumnGap = columnGap;
        invalidate();
    }

    public int getFirstDayOfWeek() {
        return config.mFirstDayOfWeek;
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
        config.mFirstDayOfWeek = firstDayOfWeek;
        invalidate();
    }

    public boolean isShowFirstDayOfWeekFirst() {
        return config.mShowFirstDayOfWeekFirst;
    }

    public void setShowFirstDayOfWeekFirst(boolean show) {
        config.mShowFirstDayOfWeekFirst = show;
    }

    public int getTextSize() {
        return config.mTextSize;
    }

    public void setTextSize(int textSize) {
        config.mTextSize = textSize;
        drawingConfig.mTodayHeaderTextPaint.setTextSize(textSize);
        drawingConfig.mHeaderTextPaint.setTextSize(textSize);
        drawingConfig.mTimeTextPaint.setTextSize(textSize);
        invalidate();
    }

    public int getHeaderColumnPadding() {
        return config.mHeaderColumnPadding;
    }

    public void setHeaderColumnPadding(int headerColumnPadding) {
        config.mHeaderColumnPadding = headerColumnPadding;
        invalidate();
    }

    public int getHeaderColumnTextColor() {
        return config.mHeaderColumnTextColor;
    }

    public void setHeaderColumnTextColor(int headerColumnTextColor) {
        config.mHeaderColumnTextColor = headerColumnTextColor;
        drawingConfig.mHeaderTextPaint.setColor(headerColumnTextColor);
        drawingConfig.mTimeTextPaint.setColor(headerColumnTextColor);
        invalidate();
    }

    public int getHeaderRowPadding() {
        return config.mHeaderRowPadding;
    }

    public void setHeaderRowPadding(int headerRowPadding) {
        config.mHeaderRowPadding = headerRowPadding;
        invalidate();
    }

    public int getHeaderRowBackgroundColor() {
        return config.mHeaderRowBackgroundColor;
    }

    public void setHeaderRowBackgroundColor(int headerRowBackgroundColor) {
        config.mHeaderRowBackgroundColor = headerRowBackgroundColor;
        drawingConfig.mHeaderBackgroundPaint.setColor(headerRowBackgroundColor);
        invalidate();
    }

    public int getDayBackgroundColor() {
        return config.mDayBackgroundColor;
    }

    public void setDayBackgroundColor(int dayBackgroundColor) {
        config.mDayBackgroundColor = dayBackgroundColor;
        drawingConfig.mDayBackgroundPaint.setColor(dayBackgroundColor);
        invalidate();
    }

    public int getHourSeparatorColor() {
        return config.mHourSeparatorColor;
    }

    public void setHourSeparatorColor(int hourSeparatorColor) {
        config.mHourSeparatorColor = hourSeparatorColor;
        drawingConfig.mHourSeparatorPaint.setColor(hourSeparatorColor);
        invalidate();
    }

    public int getTodayBackgroundColor() {
        return config.mTodayBackgroundColor;
    }

    public void setTodayBackgroundColor(int todayBackgroundColor) {
        config.mTodayBackgroundColor = todayBackgroundColor;
        drawingConfig.mTodayBackgroundPaint.setColor(todayBackgroundColor);
        invalidate();
    }

    public int getHourSeparatorStrokeWidth() {
        return config.mHourSeparatorStrokeWidth;
    }

    public void setHourSeparatorStrokeWidth(int hourSeparatorWidth) {
        config.mHourSeparatorStrokeWidth = hourSeparatorWidth;
        drawingConfig.mHourSeparatorPaint.setStrokeWidth(hourSeparatorWidth);
        invalidate();
    }

    public int getTodayHeaderTextColor() {
        return config.mTodayHeaderTextColor;
    }

    public void setTodayHeaderTextColor(int todayHeaderTextColor) {
        config.mTodayHeaderTextColor = todayHeaderTextColor;
        drawingConfig.mTodayHeaderTextPaint.setColor(todayHeaderTextColor);
        invalidate();
    }

    public int getEventTextSize() {
        return config.mEventTextSize;
    }

    public void setEventTextSize(int eventTextSize) {
        config.mEventTextSize = eventTextSize;
        drawingConfig.mEventTextPaint.setTextSize(eventTextSize);
        invalidate();
    }

    public int getEventTextColor() {
        return config.mEventTextColor;
    }

    public void setEventTextColor(int eventTextColor) {
        config.mEventTextColor = eventTextColor;
        drawingConfig.mEventTextPaint.setColor(eventTextColor);
        invalidate();
    }

    public int getEventPadding() {
        return config.mEventPadding;
    }

    public void setEventPadding(int eventPadding) {
        config.mEventPadding = eventPadding;
        invalidate();
    }

    public int getHeaderColumnBackgroundColor() {
        return config.mHeaderColumnBackgroundColor;
    }

    public void setHeaderColumnBackgroundColor(int headerColumnBackgroundColor) {
        config.mHeaderColumnBackgroundColor = headerColumnBackgroundColor;
        drawingConfig.mHeaderColumnBackgroundPaint.setColor(headerColumnBackgroundColor);
        invalidate();
    }

    public int getDefaultEventColor() {
        return drawingConfig.mDefaultEventColor;
    }

    public void setDefaultEventColor(int defaultEventColor) {
        drawingConfig.mDefaultEventColor = defaultEventColor;
        invalidate();
    }

    public int getOverlappingEventGap() {
        return config.mOverlappingEventGap;
    }

    /**
     * Set the gap between overlapping events.
     *
     * @param overlappingEventGap The gap between overlapping events.
     */
    public void setOverlappingEventGap(int overlappingEventGap) {
        config.mOverlappingEventGap = overlappingEventGap;
        invalidate();
    }

    public int getEventCornerRadius() {
        return config.mEventCornerRadius;
    }

    /**
     * Set corner radius for event rect.
     *
     * @param eventCornerRadius the radius in px.
     */
    public void setEventCornerRadius(int eventCornerRadius) {
        config.mEventCornerRadius = eventCornerRadius;
    }

    public int getEventMarginVertical() {
        return config.mEventMarginVertical;
    }

    /**
     * Set the top and bottom margin of the event. The event will release this margin from the top
     * and bottom edge. This margin is useful for differentiation consecutive events.
     *
     * @param eventMarginVertical The top and bottom margin.
     */
    public void setEventMarginVertical(int eventMarginVertical) {
        config.mEventMarginVertical = eventMarginVertical;
        invalidate();
    }

    /**
     * Set the start and end margin of the event. The event will release this margin from the start
     * and end edge.
     *
     * @param eventMarginHorizontal The start and end margin.
     */
    public void setEventMarginHorizontal(int eventMarginHorizontal) {
        config.mEventMarginHorizontal = eventMarginHorizontal;
        invalidate();
    }

    public int getEventMarginHorizontal() {
        return config.mEventMarginHorizontal;
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
        return config.mXScrollingSpeed;
    }

    /**
     * Sets the speed for horizontal scrolling.
     *
     * @param xScrollingSpeed The new horizontal scrolling speed.
     */
    public void setXScrollingSpeed(float xScrollingSpeed) {
        config.mXScrollingSpeed = xScrollingSpeed;
    }

    /**
     * Whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     *
     * @return True if weekends should have different background colors.
     */
    public boolean isShowDistinctWeekendColor() {
        return config.mShowDistinctWeekendColor;
    }

    /**
     * Set whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     *
     * @param showDistinctWeekendColor True if weekends should have different background colors.
     */
    public void setShowDistinctWeekendColor(boolean showDistinctWeekendColor) {
        config.mShowDistinctWeekendColor = showDistinctWeekendColor;
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
        return config.mShowDistinctPastFutureColor;
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
        config.mShowDistinctPastFutureColor = showDistinctPastFutureColor;
        invalidate();
    }

    /**
     * Get whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineThickness`.
     *
     * @return True if "now" line should be displayed.
     */
    public boolean isShowNowLine() {
        return config.mShowNowLine;
    }

    /**
     * Set whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineThickness`.
     *
     * @param showNowLine True if "now" line should be displayed.
     */
    public void setShowNowLine(boolean showNowLine) {
        config.mShowNowLine = showNowLine;
        invalidate();
    }

    /**
     * Get the "now" line color.
     *
     * @return The color of the "now" line.
     */
    public int getNowLineColor() {
        return config.mNowLineColor;
    }

    /**
     * Set the "now" line color.
     *
     * @param nowLineColor The color of the "now" line.
     */
    public void setNowLineColor(int nowLineColor) {
        config.mNowLineColor = nowLineColor;
        invalidate();
    }

    /**
     * Get the "now" line thickness.
     *
     * @return The thickness of the "now" line.
     */
    public int getNowLineThickness() {
        return config.mNowLineThickness;
    }

    /**
     * Set the "now" line thickness.
     *
     * @param nowLineThickness The thickness of the "now" line.
     */
    public void setNowLineThickness(int nowLineThickness) {
        config.mNowLineThickness = nowLineThickness;
        invalidate();
    }

    /**
     * Get whether the week view should fling horizontally.
     *
     * @return True if the week view has horizontal fling enabled.
     */
    public boolean isHorizontalFlingEnabled() {
        return config.mHorizontalFlingEnabled;
    }

    /**
     * Set whether the week view should fling horizontally.
     */
    public void setHorizontalFlingEnabled(boolean enabled) {
        config.mHorizontalFlingEnabled = enabled;
    }

    /**
     * Get whether the week view should fling vertically.
     *
     * @return True if the week view has vertical fling enabled.
     */
    public boolean isVerticalFlingEnabled() {
        return config.mVerticalFlingEnabled;
    }

    /**
     * Set whether the week view should fling vertically.
     */
    public void setVerticalFlingEnabled(boolean enabled) {
        config.mVerticalFlingEnabled = enabled;
    }

    /**
     * Get the height of AllDay-events.
     *
     * @return Height of AllDay-events.
     */
    public int getAllDayEventHeight() {
        return config.mAllDayEventHeight;
    }

    /**
     * Set the height of AllDay-events.
     */
    public void setAllDayEventHeight(int height) {
        config.mAllDayEventHeight = height;
    }

    /**
     * Get scroll duration
     *
     * @return scroll duration
     */
    public int getScrollDuration() {
        return config.mScrollDuration;
    }

    /**
     * Set the scroll duration
     */
    public void setScrollDuration(int scrollDuration) {
        config.mScrollDuration = scrollDuration;
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to scrolling.
    //
    /////////////////////////////////////////////////////////////////

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return scrollHandler.onTouchEvent(event);
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        scrollHandler.computeScroll();
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

    /**
     * Show a specific day on the week view.
     *
     * @param date The date to show.
     */
    public void goToDate(Calendar date) {
        scrollHandler.scroller.forceFinished(true);
        scrollHandler.currentScrollDirection = scrollHandler.currentFlingDirection = Direction.NONE;

        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        if (viewState.areDimensionsInvalid) {
            viewState.scrollToDay = date;
            return;
        }

        viewState.shouldRefreshEvents = true;

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        long day = 1000L * 60L * 60L * 24L;
        long dateInMillis = date.getTimeInMillis() + date.getTimeZone().getOffset(date.getTimeInMillis());
        long todayInMillis = today.getTimeInMillis() + today.getTimeZone().getOffset(today.getTimeInMillis());
        long dateDifference = (dateInMillis / day) - (todayInMillis / day);
        drawingConfig.mCurrentOrigin.x = -dateDifference * (drawingConfig.mWidthPerDay + config.mColumnGap);
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
    public void goToHour(double hour) {
        if (viewState.areDimensionsInvalid) {
            viewState.scrollToHour = hour;
            return;
        }

        int verticalOffset = 0;
        if (hour > 24) {
            verticalOffset = config.mHourHeight * 24;
        } else if (hour > 0) {
            verticalOffset = (int) (config.mHourHeight * hour);
        }

        if (verticalOffset > config.mHourHeight * 24 - getHeight() + drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mHeaderMarginBottom) {
            verticalOffset = (int) (config.mHourHeight * 24 - getHeight() + drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mHeaderMarginBottom);
        }

        drawingConfig.mCurrentOrigin.y = -verticalOffset;
        invalidate();
    }

    /**
     * Get the first hour that is visible on the screen.
     *
     * @return The first hour that is visible.
     */
    public double getFirstVisibleHour() {
        return (drawingConfig.mCurrentOrigin.y * -1) / config.mHourHeight;
    }

}

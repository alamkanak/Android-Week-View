package com.alamkanak.weekview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import com.alamkanak.weekview.drawing.EventRect;
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

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.alamkanak.weekview.utils.WeekViewUtil.today;

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://alamkanak.github.io/
 */
public class WeekView extends View {

    public enum Direction {
        NONE, LEFT, RIGHT, VERTICAL
    }

    private final Context context;

    private GestureDetector gestureDetector;
    private OverScroller scroller;

    private Direction currentScrollDirection = Direction.NONE;
    private Direction currentFlingDirection = Direction.NONE;

    private ScaleGestureDetector scaleDetector;
    private boolean isZooming;

    private int minimumFlingVelocity = 0;
    private int scaledTouchSlop = 0;

    // Listeners
    private EventClickListener eventClickListener;
    private EventLongPressListener eventLongPressListener;
    private WeekViewLoader weekViewLoader;
    private EmptyViewClickListener emptyViewClickListener;
    private EmptyViewLongPressListener emptyViewLongPressListener;
    private ScrollListener scrollListener;

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawingConfig;

    private WeekViewData data;
    private WeekViewViewState viewState;

    private HeaderRowDrawer headerRowDrawer;
    private TimeColumnDrawer timeColumnDrawer;

    private final GestureDetector.SimpleOnGestureListener gestureListener =
            new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDown(MotionEvent e) {
                    goToNearestOrigin();
                    return true;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    // Check if view is zoomed.
                    if (isZooming)
                        return true;

                    switch (currentScrollDirection) {
                        case NONE: {
                            // Allow scrolling only in one direction.
                            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                                if (distanceX > 0) {
                                    currentScrollDirection = Direction.LEFT;
                                } else {
                                    currentScrollDirection = Direction.RIGHT;
                                }
                            } else {
                                currentScrollDirection = Direction.VERTICAL;
                            }
                            break;
                        }
                        case LEFT: {
                            // Change direction if there was enough change.
                            if (Math.abs(distanceX) > Math.abs(distanceY) && (distanceX < -scaledTouchSlop)) {
                                currentScrollDirection = Direction.RIGHT;
                            }
                            break;
                        }
                        case RIGHT: {
                            // Change direction if there was enough change.
                            if (Math.abs(distanceX) > Math.abs(distanceY) && (distanceX > scaledTouchSlop)) {
                                currentScrollDirection = Direction.LEFT;
                            }
                            break;
                        }
                    }

                    // Calculate the new origin after scroll.
                    switch (currentScrollDirection) {
                        case LEFT:
                        case RIGHT:
                            drawingConfig.mCurrentOrigin.x -= distanceX * config.mXScrollingSpeed;
                            postInvalidateOnAnimation();
                            break;
                        case VERTICAL:
                            drawingConfig.mCurrentOrigin.y -= distanceY;
                            postInvalidateOnAnimation();
                            break;
                    }
                    return true;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    if (isZooming)
                        return true;

                    if ((currentFlingDirection == Direction.LEFT && !config.mHorizontalFlingEnabled) ||
                            (currentFlingDirection == Direction.RIGHT && !config.mHorizontalFlingEnabled) ||
                            (currentFlingDirection == Direction.VERTICAL && !config.mVerticalFlingEnabled)) {
                        return true;
                    }

                    scroller.forceFinished(true);

                    currentFlingDirection = currentScrollDirection;
                    switch (currentFlingDirection) {
                        case LEFT:
                        case RIGHT:
                            scroller.fling((int) drawingConfig.mCurrentOrigin.x, (int) drawingConfig.mCurrentOrigin.y, (int) (velocityX * config.mXScrollingSpeed), 0, Integer.MIN_VALUE, Integer.MAX_VALUE, (int) -(config.mHourHeight * 24 + drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mHeaderMarginBottom + drawingConfig.mTimeTextHeight / 2 - getHeight()), 0);
                            break;
                        case VERTICAL:
                            scroller.fling((int) drawingConfig.mCurrentOrigin.x, (int) drawingConfig.mCurrentOrigin.y, 0, (int) velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, (int) -(config.mHourHeight * 24 + drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mHeaderMarginBottom + drawingConfig.mTimeTextHeight / 2 - getHeight()), 0);
                            break;
                    }

                    postInvalidateOnAnimation();
                    return true;
                }


                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    // If the tap was on an event then trigger the callback.
                    if (data.eventRects != null && eventClickListener != null) {
                        List<EventRect> reversedEventRects = data.eventRects;
                        Collections.reverse(reversedEventRects);
                        for (EventRect event : reversedEventRects) {
                            if (event.rectF != null && e.getX() > event.rectF.left && e.getX() < event.rectF.right && e.getY() > event.rectF.top && e.getY() < event.rectF.bottom) {
                                eventClickListener.onEventClick(event.originalEvent, event.rectF);
                                return super.onSingleTapConfirmed(e);
                            }
                        }
                    }

                    // If the tap was on in an empty space, then trigger the callback.
                    if (emptyViewClickListener != null && e.getX() > drawingConfig.mHeaderColumnWidth && e.getY() > (drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mHeaderMarginBottom)) {
                        Calendar selectedTime = getTimeFromPoint(e.getX(), e.getY());
                        if (selectedTime != null) {
                            emptyViewClickListener.onEmptyViewClicked(selectedTime);
                        }
                    }

                    return super.onSingleTapConfirmed(e);
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);

                    if (eventLongPressListener != null && data.eventRects != null) {
                        List<EventRect> reversedEventRects = data.eventRects;
                        Collections.reverse(reversedEventRects);
                        for (EventRect event : reversedEventRects) {
                            if (event.rectF != null && e.getX() > event.rectF.left && e.getX() < event.rectF.right && e.getY() > event.rectF.top && e.getY() < event.rectF.bottom) {
                                eventLongPressListener.onEventLongPress(event.originalEvent, event.rectF);
                                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                                return;
                            }
                        }
                    }

                    // If the tap was on in an empty space, then trigger the callback.
                    if (emptyViewLongPressListener != null && e.getX() > drawingConfig.mHeaderColumnWidth && e.getY() > (drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mHeaderMarginBottom)) {
                        Calendar selectedTime = getTimeFromPoint(e.getX(), e.getY());
                        if (selectedTime != null) {
                            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                            emptyViewLongPressListener.onEmptyViewLongPress(selectedTime);
                        }
                    }
                }
            };

    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        config = new WeekViewConfig(context, attrs);

        init();

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

        EventsDrawer eventsDrawer = new EventsDrawer(config, drawingConfig);
        timeColumnDrawer = new TimeColumnDrawer(config, drawingConfig);

        headerRowDrawer = new HeaderRowDrawer(listener, eventsDrawer, config, drawingConfig, data, viewState);

    }

    private void init() {
        gestureDetector = new GestureDetector(context, gestureListener);
        scroller = new OverScroller(context, new FastOutLinearInInterpolator());

        minimumFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                isZooming = false;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                isZooming = true;
                goToNearestOrigin();
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                drawingConfig.mNewHourHeight = Math.round(config.mHourHeight * detector.getScaleFactor());
                invalidate();
                return true;
            }
        });
    }

    // fix rotation changes
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewState.areDimensionsInvalid = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawHeaderRowAndEvents(canvas);
        drawTimeColumnAndAxes(canvas);
    }

    private void drawTimeColumnAndAxes(Canvas canvas) {
        timeColumnDrawer.draw(canvas, getHeight());
    }

    private void drawHeaderRowAndEvents(Canvas canvas) {
        headerRowDrawer.drawHeaderRowAndEvents(this, canvas);
    }

    /**
     * Get the time and date where the user clicked on.
     *
     * @param x The x position of the touch event.
     * @param y The y position of the touch event.
     * @return The time and date at the clicked position.
     */
    private Calendar getTimeFromPoint(float x, float y) {
        int leftDaysWithGaps = (int) -(Math.ceil(drawingConfig.mCurrentOrigin.x / (drawingConfig.mWidthPerDay + config.mColumnGap)));
        float startPixel = drawingConfig.mCurrentOrigin.x + (drawingConfig.mWidthPerDay + config.mColumnGap) * leftDaysWithGaps +
                drawingConfig.mHeaderColumnWidth;
        for (int dayNumber = leftDaysWithGaps + 1;
             dayNumber <= leftDaysWithGaps + config.mNumberOfVisibleDays + 1;
             dayNumber++) {
            float start = (startPixel < drawingConfig.mHeaderColumnWidth ? drawingConfig.mHeaderColumnWidth : startPixel);
            if (drawingConfig.mWidthPerDay + startPixel - start > 0 && x > start && x < startPixel + drawingConfig.mWidthPerDay) {
                Calendar day = today();
                day.add(Calendar.DATE, dayNumber - 1);
                float pixelsFromZero = y - drawingConfig.mCurrentOrigin.y - drawingConfig.mHeaderHeight
                        - config.mHeaderRowPadding * 2 - drawingConfig.mTimeTextHeight / 2 - drawingConfig.mHeaderMarginBottom;
                int hour = (int) (pixelsFromZero / config.mHourHeight);
                int minute = (int) (60 * (pixelsFromZero - hour * config.mHourHeight) / config.mHourHeight);
                day.add(Calendar.HOUR, hour);
                day.set(Calendar.MINUTE, minute);
                return day;
            }
            startPixel += drawingConfig.mWidthPerDay + config.mColumnGap;
        }
        return null;
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
        this.eventClickListener = listener;
    }

    public EventClickListener getEventClickListener() {
        return eventClickListener;
    }

    @Nullable
    public MonthLoader.MonthChangeListener getMonthChangeListener() {
        if (weekViewLoader instanceof MonthLoader) {
            return ((MonthLoader) weekViewLoader).getOnMonthChangeListener();
        }
        return null;
    }

    public void setMonthChangeListener(@Nullable MonthLoader.MonthChangeListener monthChangeListener) {
        this.weekViewLoader = new MonthLoader(monthChangeListener);
        headerRowDrawer.setWeekViewLoader(weekViewLoader);
    }

    /**
     * Get event loader in the week view. Event loaders define the  interval after which the events
     * are loaded in week view. For a MonthLoader events are loaded for every month. You can define
     * your custom event loader by extending WeekViewLoader.
     *
     * @return The event loader.
     */
    public WeekViewLoader getWeekViewLoader() {
        return weekViewLoader;
    }

    /**
     * Set event loader in the week view. For example, a MonthLoader. Event loaders define the
     * interval after which the events are loaded in week view. For a MonthLoader events are loaded
     * for every month. You can define your custom event loader by extending WeekViewLoader.
     *
     * @param weekViewLoader The event loader.
     */
    public void setWeekViewLoader(WeekViewLoader weekViewLoader) {
        this.weekViewLoader = weekViewLoader;
        headerRowDrawer.setWeekViewLoader(weekViewLoader);
    }

    public EventLongPressListener getEventLongPressListener() {
        return eventLongPressListener;
    }

    public void setEventLongPressListener(EventLongPressListener eventLongPressListener) {
        this.eventLongPressListener = eventLongPressListener;
    }

    public void setEmptyViewClickListener(EmptyViewClickListener emptyViewClickListener) {
        this.emptyViewClickListener = emptyViewClickListener;
    }

    public EmptyViewClickListener getEmptyViewClickListener() {
        return emptyViewClickListener;
    }

    public void setEmptyViewLongPressListener(EmptyViewLongPressListener emptyViewLongPressListener) {
        this.emptyViewLongPressListener = emptyViewLongPressListener;
    }

    public EmptyViewLongPressListener getEmptyViewLongPressListener() {
        return emptyViewLongPressListener;
    }

    public void setScrollListener(ScrollListener scrolledListener) {
        scrollListener = scrolledListener;
        headerRowDrawer.setScrollListener(scrolledListener);
    }

    public ScrollListener getScrollListener() {
        return scrollListener;
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
        scaleDetector.onTouchEvent(event);
        boolean val = gestureDetector.onTouchEvent(event);

        // Check after call of gestureDetector, so currentFlingDirection and currentScrollDirection are set.
        if (event.getAction() == MotionEvent.ACTION_UP && !isZooming && currentFlingDirection == Direction.NONE) {
            if (currentScrollDirection == Direction.RIGHT || currentScrollDirection == Direction.LEFT) {
                goToNearestOrigin();
            }
            currentScrollDirection = Direction.NONE;
        }

        return val;
    }

    private void goToNearestOrigin() {
        double leftDays = drawingConfig.mCurrentOrigin.x / (drawingConfig.mWidthPerDay + config.mColumnGap);

        if (currentFlingDirection != Direction.NONE) {
            // snap to nearest day
            leftDays = Math.round(leftDays);
        } else if (currentScrollDirection == Direction.LEFT) {
            // snap to last day
            leftDays = Math.floor(leftDays);
        } else if (currentScrollDirection == Direction.RIGHT) {
            // snap to next day
            leftDays = Math.ceil(leftDays);
        } else {
            // snap to nearest day
            leftDays = Math.round(leftDays);
        }

        int nearestOrigin = (int) (drawingConfig.mCurrentOrigin.x - leftDays * (drawingConfig.mWidthPerDay + config.mColumnGap));

        if (nearestOrigin != 0) {
            // Stop current animation.
            scroller.forceFinished(true);
            // Snap to date.
            scroller.startScroll((int) drawingConfig.mCurrentOrigin.x, (int) drawingConfig.mCurrentOrigin.y, -nearestOrigin, 0, (int) (Math.abs(nearestOrigin) / drawingConfig.mWidthPerDay * config.mScrollDuration));
            postInvalidateOnAnimation();
        }
        // Reset scrolling and fling direction.
        currentScrollDirection = currentFlingDirection = Direction.NONE;
    }


    @Override
    public void computeScroll() {
        super.computeScroll();

        if (scroller.isFinished()) {
            if (currentFlingDirection != Direction.NONE) {
                // Snap to day after fling is finished.
                goToNearestOrigin();
            }
        } else {
            if (currentFlingDirection != Direction.NONE && forceFinishScroll()) {
                goToNearestOrigin();
            } else if (scroller.computeScrollOffset()) {
                drawingConfig.mCurrentOrigin.y = scroller.getCurrY();
                drawingConfig.mCurrentOrigin.x = scroller.getCurrX();
                postInvalidateOnAnimation();
            }
        }
    }

    /**
     * Check if scrolling should be stopped.
     *
     * @return true if scrolling should be stopped before reaching the end of animation.
     */
    private boolean forceFinishScroll() {
        return scroller.getCurrVelocity() <= minimumFlingVelocity;
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
        scroller.forceFinished(true);
        currentScrollDirection = currentFlingDirection = Direction.NONE;

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
        if (hour > 24)
            verticalOffset = config.mHourHeight * 24;
        else if (hour > 0)
            verticalOffset = (int) (config.mHourHeight * hour);

        if (verticalOffset > config.mHourHeight * 24 - getHeight() + drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mHeaderMarginBottom)
            verticalOffset = (int) (config.mHourHeight * 24 - getHeight() + drawingConfig.mHeaderHeight + config.mHeaderRowPadding * 2 + drawingConfig.mHeaderMarginBottom);

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

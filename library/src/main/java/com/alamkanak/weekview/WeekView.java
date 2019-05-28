package com.alamkanak.weekview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
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

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;

import java.util.Calendar;
import java.util.List;

import static com.alamkanak.weekview.Constants.UNINITIALIZED;
import static com.alamkanak.weekview.DateUtils.toCalendar;
import static com.alamkanak.weekview.DateUtils.toLocalDate;
import static com.alamkanak.weekview.DateUtils.today;
import static java.lang.Math.ceil;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.util.Calendar.HOUR_OF_DAY;

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://alamkanak.github.io/
 */
public final class WeekView<T> extends View
        implements WeekViewGestureHandler.Listener, WeekViewViewState.UpdateListener {

    private static int width;
    private static int height;

    private final WeekViewConfigWrapper configWrapper;
    private final WeekViewCache<T> cache;
    private final WeekViewViewState viewState;
    private final WeekViewGestureHandler<T> gestureHandler;

    private final DrawingContext drawingContext = new DrawingContext();

    private final HeaderRowDrawer<T> headerRowDrawer;
    private final DayLabelDrawer dayLabelDrawer;
    private final EventsDrawer<T> eventsDrawer;
    private final TimeColumnDrawer timeColumnDrawer;
    private final DayBackgroundDrawer dayBackgroundDrawer;
    private final BackgroundGridDrawer backgroundGridDrawer;
    private final NowLineDrawer nowLineDrawer;
    private final EventChipsProvider<T> eventChipsProvider;

    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        AndroidThreeTen.init(context);

        WeekViewConfig config = new WeekViewConfig(context, attrs);
        configWrapper = new WeekViewConfigWrapper(context, config);

        WeekViewEventSplitter<T> eventSplitter = new WeekViewEventSplitter<>(configWrapper);
        cache = new WeekViewCache<>(eventSplitter);
        viewState = new WeekViewViewState(configWrapper);

        gestureHandler = new WeekViewGestureHandler<>(context, this, configWrapper, cache);

        eventsDrawer = new EventsDrawer<>(configWrapper);
        timeColumnDrawer = new TimeColumnDrawer(configWrapper);

        headerRowDrawer = new HeaderRowDrawer<>(configWrapper, cache, viewState);
        dayLabelDrawer = new DayLabelDrawer(configWrapper);

        dayBackgroundDrawer = new DayBackgroundDrawer(configWrapper);
        backgroundGridDrawer = new BackgroundGridDrawer(configWrapper);
        nowLineDrawer = new NowLineDrawer(configWrapper);

        eventChipsProvider = new EventChipsProvider<>(configWrapper, cache, viewState);
        eventChipsProvider.setWeekViewLoader(getWeekViewLoader());
    }

    static int getViewWidth() {
        return width;
    }

    static int getViewHeight() {
        return height;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        final int visibleDays = configWrapper.getNumberOfVisibleDays();
        final LocalDate firstDate = viewState.getFirstVisibleDay();
        return new SavedState(superState, visibleDays, firstDate);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        if (configWrapper.getRestoreNumberOfVisibleDays()) {
            configWrapper.setNumberOfVisibleDays(savedState.numberOfVisibleDays);
        }

        Calendar firstVisibleDay = toCalendar(savedState.firstVisibleDate);
        goToDate(firstVisibleDay);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        viewState.setAreDimensionsInvalid(true);

        WeekView.width = width;
        WeekView.height = height;

        if (configWrapper.getShowCompleteDay()) {
            configWrapper.updateHourHeight(height);
        }
    }

    private final Paint paint = new Paint();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final boolean isFirstDraw = viewState.isFirstDraw();

        calculateWidthPerDay();
        viewState.update(this);

        configWrapper.refreshAfterZooming();
        configWrapper.updateVerticalOrigin();

        notifyScrollListeners();
        prepareEventDrawing(canvas);

        if (viewState.isFirstDraw()) {
            configWrapper.moveCurrentOriginIfFirstDraw();
            viewState.setFirstDraw(false);
        }

        // final DrawingContext drawingContext = DrawingContext.create(configWrapper);
        drawingContext.update(configWrapper);
        if (!isInEditMode()) {
            eventChipsProvider.loadEventsIfNecessary();
        }

        List<Pair<EventChip<T>, StaticLayout>> allDayEvents =
                eventsDrawer.prepareDrawAllDayEvents(cache.getAllDayEventChips(), drawingContext);

        dayBackgroundDrawer.draw(drawingContext, canvas);
        backgroundGridDrawer.draw(drawingContext, canvas);

        eventsDrawer.drawSingleEvents(cache.getNormalEventChips(), drawingContext, canvas, paint);

        nowLineDrawer.draw(drawingContext, canvas);
        headerRowDrawer.draw(drawingContext, canvas, paint);
        dayLabelDrawer.draw(drawingContext, canvas);

        eventsDrawer.drawAllDayEvents(allDayEvents, canvas, paint);
        timeColumnDrawer.drawTimeColumn(canvas);

        if (isFirstDraw) {
            // Temporary workaround to make sure that the events are actually being displayed
            invalidate();
        }

        if (viewState.getRequiresPostInvalidateOnAnimation()) {
            viewState.setRequiresPostInvalidateOnAnimation(false);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void notifyScrollListeners() {
        final LocalDate oldFirstVisibleDay = viewState.getFirstVisibleDay();

        final float totalDayWidth = configWrapper.getTotalDayWidth();
        final int visibleDays = configWrapper.getNumberOfVisibleDays();
        final int delta = (int) round(ceil(configWrapper.getCurrentOrigin().x / totalDayWidth)) * -1;

        LocalDate firstVisibleDay = today().plusDays(delta);
        LocalDate lastVisibleDay = firstVisibleDay.plusDays(visibleDays - 1);

        viewState.setFirstVisibleDay(firstVisibleDay);
        viewState.setLastVisibleDay(lastVisibleDay);

        final boolean hasFirstVisibleDayChanged = !firstVisibleDay.equals(oldFirstVisibleDay);
        if (hasFirstVisibleDayChanged && getScrollListener() != null) {
            notifyVisibleDayChanged(firstVisibleDay, oldFirstVisibleDay);
        }
    }

    private void notifyVisibleDayChanged(LocalDate newLocalDate, @Nullable LocalDate oldLocalDate) {
        Calendar newDate = toCalendar(newLocalDate);
        Calendar oldDate = null;

        if (oldLocalDate != null) {
            oldDate = toCalendar(oldLocalDate);
        }

        getScrollListener().onFirstVisibleDayChanged(newDate, oldDate);
    }

    private void prepareEventDrawing(Canvas canvas) {
        cache.clearEventChipsCache();
        canvas.save();
        clipEventsRect(canvas);
    }

    private void calculateWidthPerDay() {
        if (configWrapper.getTimeColumnWidth() == UNINITIALIZED) {
            configWrapper.calculateTimeColumnWidth();
        }

        configWrapper.calculateWidthPerDay(getWidth());
    }

    private void clipEventsRect(Canvas canvas) {
        final int width = WeekView.getViewWidth();
        final int height = WeekView.getViewHeight();
        canvas.clipRect(configWrapper.getTimeColumnWidth(), configWrapper.getHeaderHeight(), width, height);
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
        return configWrapper.getFirstDayOfWeek();
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
        configWrapper.setFirstDayOfWeek(firstDayOfWeek);
        invalidate();
    }

    /**
     * Get the number of visible days in a week.
     *
     * @return The number of visible days in a week.
     */
    public int getNumberOfVisibleDays() {
        return configWrapper.getNumberOfVisibleDays();
    }

    /**
     * Set the number of visible days in a week.
     *
     * @param numberOfVisibleDays The number of visible days in a week.
     */
    public void setNumberOfVisibleDays(int numberOfVisibleDays) {
        configWrapper.setNumberOfVisibleDays(numberOfVisibleDays);

        DateTimeInterpreter interpreter = getDateTimeInterpreter();
        if (interpreter instanceof DefaultDateTimeInterpreter) {
            DefaultDateTimeInterpreter defaultInterpreter =
                    (DefaultDateTimeInterpreter) getDateTimeInterpreter();
            defaultInterpreter.setNumberOfDays(numberOfVisibleDays);
        }

        LocalDate firstVisibleDay = viewState.getFirstVisibleDay();
        if (firstVisibleDay != null) {
            viewState.setScrollToDay(firstVisibleDay);
        }

        Integer hour = viewState.getScrollToHour();
        if (hour != null) {
            viewState.setScrollToHour(hour);
        }

        invalidate();
    }

    public boolean isShowFirstDayOfWeekFirst() {
        return configWrapper.getShowFirstDayOfWeekFirst();
    }

    public void setShowFirstDayOfWeekFirst(boolean show) {
        configWrapper.setShowFirstDayOfWeekFirst(show);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Header bottom line
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean getShowHeaderRowBottomLine() {
        return configWrapper.getShowHeaderRowBottomLine();
    }

    public void setShowHeaderRowBottomLine(boolean show) {
        configWrapper.setShowHeaderRowBottomLine(show);
        invalidate();
    }

    public int getHeaderRowBottomLineColor() {
        return configWrapper.getHeaderRowBottomLinePaint().getColor();
    }

    public void setHeaderRowBottomLineColor(int color) {
        configWrapper.getHeaderRowBottomLinePaint().setColor(color);
        invalidate();
    }

    public int getHeaderRowBottomLineWidth() {
        return (int) configWrapper.getHeaderRowBottomLinePaint().getStrokeWidth();
    }

    public void setHeaderRowBottomLineWidth(int width) {
        configWrapper.getHeaderRowBottomLinePaint().setStrokeWidth(width);
        invalidate();
    }

    public int getTodayHeaderTextColor() {
        return configWrapper.getTodayHeaderTextColor();
    }

    public void setTodayHeaderTextColor(int color) {
        configWrapper.setTodayHeaderTextColor(color);
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Time column
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public int getTimeColumnPadding() {
        return configWrapper.getTimeColumnPadding();
    }

    public void setTimeColumnPadding(int timeColumnPadding) {
        configWrapper.setTimeColumnPadding(timeColumnPadding);
        invalidate();
    }

    public int getTimeColumnTextColor() {
        return configWrapper.getTimeColumnTextColor();
    }

    public void setTimeColumnTextColor(int color) {
        configWrapper.setTimeColumnTextColor(color);
        invalidate();
    }

    public int getTimeColumnBackgroundColor() {
        return configWrapper.getTimeColumnBackgroundColor();
    }

    public void setTimeColumnBackgroundColor(int color) {
        configWrapper.setTimeColumnBackgroundColor(color);
        invalidate();
    }

    public int getTimeColumnTextSize() {
        return configWrapper.getTimeColumnTextSize();
    }

    public void setTimeColumnTextSize(int textSize) {
        configWrapper.setTimeColumnTextSize(textSize);
        invalidate();
    }

    public boolean isShowMidnightHour() {
        return configWrapper.getShowMidnightHour();
    }

    public void setShowMidnightHour(boolean show) {
        configWrapper.setShowMidnightHour(show);
        invalidate();
    }

    public boolean showTimeColumnHourSeparator() {
        return configWrapper.getShowTimeColumnHourSeparator();
    }

    public void setShowTimeColumnHourSeparator(boolean show) {
        configWrapper.setShowTimeColumnHourSeparator(show);
        invalidate();
    }

    public int getTimeColumnHoursInterval() {
        return configWrapper.getTimeColumnHoursInterval();
    }

    public void setTimeColumnHoursInterval(int interval) {
        configWrapper.setTimeColumnHoursInterval(interval);
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Time column separator
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean getShowTimeColumnSeparator() {
        return configWrapper.getShowTimeColumnSeparator();
    }

    public void setShowTimeColumnSeparator(boolean show) {
        configWrapper.setShowTimeColumnSeparator(show);
        invalidate();
    }

    public int getTimeColumnSeparatorColor() {
        return configWrapper.getTimeColumnSeparatorColor();
    }

    public void setTimeColumnSeparatorColor(int color) {
        configWrapper.setTimeColumnSeparatorColor(color);
        invalidate();
    }

    public int getTimeColumnSeparatorWidth() {
        return configWrapper.getTimeColumnSeparatorStrokeWidth();
    }

    public void setTimeColumnSeparatorWidth(int width) {
        configWrapper.setTimeColumnSeparatorStrokeWidth(width);
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Header row
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public int getHeaderRowPadding() {
        return configWrapper.getHeaderRowPadding();
    }

    public void setHeaderRowPadding(int padding) {
        configWrapper.setHeaderRowPadding(padding);
        invalidate();
    }

    public int getHeaderRowBackgroundColor() {
        return configWrapper.getHeaderRowBackgroundColor();
    }

    public void setHeaderRowBackgroundColor(int color) {
        configWrapper.setHeaderRowBackgroundColor(color);
        invalidate();
    }

    public int getHeaderRowTextColor() {
        return configWrapper.getHeaderRowTextColor();
    }

    public void setHeaderRowTextColor(int color) {
        configWrapper.setHeaderRowTextColor(color);
        invalidate();
    }

    public int getHeaderRowTextSize() {
        return configWrapper.getHeaderRowTextSize();
    }

    public void setHeaderRowTextSize(int textSize) {
        configWrapper.setHeaderRowTextSize(textSize);
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Event chips
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public int getEventCornerRadius() {
        return configWrapper.getEventCornerRadius();
    }

    /**
     * Set corner radius for event rect.
     *
     * @param radius the radius in px.
     */
    public void setEventCornerRadius(int radius) {
        configWrapper.setEventCornerRadius(radius);
    }

    public int getEventTextSize() {
        return (int) configWrapper.getEventTextPaint().getTextSize();
    }

    public int getAllDayEventTextSize() {
        return (int) configWrapper.getAllDayEventTextPaint().getTextSize();
    }

    public void setEventTextSize(int size) {
        configWrapper.getEventTextPaint().setTextSize(size);
        //config.setEventTextSize(eventTextSize);
        invalidate();
    }

    public void setAllDayEventTextSize(int size) {
        configWrapper.getAllDayEventTextPaint().setTextSize(size);
        //config.setAllDayEventTextSize(allDayEventTextSize);
        invalidate();
    }

    public int getEventTextColor() {
        return configWrapper.getEventTextPaint().getColor();
    }

    public void setEventTextColor(int color) {
        configWrapper.getEventTextPaint().setColor(color);
        invalidate();
    }

    public int getEventPadding() {
        return configWrapper.getEventPadding();
    }

    public void setEventPadding(int padding) {
        configWrapper.setEventPadding(padding);
        invalidate();
    }

    public int getDefaultEventColor() {
        return configWrapper.getDefaultEventColor();
    }

    public void setDefaultEventColor(int color) {
        configWrapper.setDefaultEventColor(color);
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Event margins
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public int getColumnGap() {
        return configWrapper.getColumnGap();
    }

    public void setColumnGap(int gap) {
        configWrapper.setColumnGap(gap);
        invalidate();
    }

    public int getOverlappingEventGap() {
        return configWrapper.getOverlappingEventGap();
    }

    /**
     * Set the gap between overlapping events.
     *
     * @param gap The gap between overlapping events.
     */
    public void setOverlappingEventGap(int gap) {
        configWrapper.setOverlappingEventGap(gap);
        invalidate();
    }

    public int getEventMarginVertical() {
        return configWrapper.getEventMarginVertical();
    }

    /**
     * Set the top and bottom margin of the event. The event will release this margin from the top
     * and bottom edge. This margin is useful for differentiation consecutive events.
     *
     * @param margin The top and bottom margin.
     */
    public void setEventMarginVertical(int margin) {
        configWrapper.setEventMarginVertical(margin);
        invalidate();
    }

    /**
     * Set the start and end margin of the event. The event will release this margin from the start
     * and end edge.
     *
     * @param margin The start and end margin.
     */
    public void setEventMarginHorizontal(int margin) {
        configWrapper.setEventMarginHorizontal(margin);
        invalidate();
    }

    public int getEventMarginHorizontal() {
        return configWrapper.getEventMarginHorizontal();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Colors
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public int getDayBackgroundColor() {
        return configWrapper.getDayBackgroundPaint().getColor();
    }

    public void setDayBackgroundColor(int color) {
        configWrapper.getDayBackgroundPaint().setColor(color);
        invalidate();
    }

    public int getTodayBackgroundColor() {
        return configWrapper.getTodayBackgroundPaint().getColor();
    }

    public void setTodayBackgroundColor(int color) {
        configWrapper.getTodayBackgroundPaint().setColor(color);
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
        return configWrapper.getShowDistinctWeekendColor();
    }

    /**
     * Set whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     *
     * @param show True if weekends should have different background colors.
     */
    public void setShowDistinctWeekendColor(boolean show) {
        configWrapper.setShowDistinctWeekendColor(show);
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
        return configWrapper.getShowDistinctPastFutureColor();
    }

    /**
     * Set whether weekends should have a background color different from the normal day background
     * color. The past and future day colors are defined by the attributes `futureBackgroundColor`
     * and `pastBackgroundColor`.
     *
     * @param color True if past and future should have two different
     *              background colors.
     */
    public void setShowDistinctPastFutureColor(boolean color) {
        configWrapper.setShowDistinctPastFutureColor(color);
        invalidate();
    }

    // TODO: Past & future background color, pastWeekend and futureWeekend

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Hour height
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public float getHourHeight() {
        return configWrapper.getHourHeight();
    }

    public void setHourHeight(float hourHeight) {
        configWrapper.setNewHourHeight(hourHeight);
        // config.drawingConfig.newHourHeight = hourHeight;
        invalidate();
    }

    public int getMinHourHeight() {
        return configWrapper.getMinHourHeight();
    }

    public void setMinHourHeight(int height) {
        configWrapper.setMinHourHeight(height);
        invalidate();
    }

    public int getMaxHourHeight() {
        return configWrapper.getMaxHourHeight();
    }

    public void setMaxHourHeight(int height) {
        configWrapper.setMaxHourHeight(height);
        invalidate();
    }

    public boolean isShowCompleteDay() {
        return configWrapper.getShowCompleteDay();
    }

    public void setShowCompleteDay(boolean show) {
        configWrapper.setShowCompleteDay(show);
        invalidate();
    }

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
        return configWrapper.getShowNowLine();
    }

    /**
     * Set whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineStrokeWidth`.
     *
     * @param show True if "now" line should be displayed.
     */
    public void setShowNowLine(boolean show) {
        configWrapper.setShowNowLine(show);
        invalidate();
    }

    /**
     * Get the "now" line color.
     *
     * @return The color of the "now" line.
     */
    public int getNowLineColor() {
        return configWrapper.getNowLinePaint().getColor();
    }

    /**
     * Set the "now" line color.
     *
     * @param color The color of the "now" line.
     */
    public void setNowLineColor(int color) {
        configWrapper.getNowLinePaint().setColor(color);
        invalidate();
    }

    /**
     * Get the "now" line thickness.
     *
     * @return The thickness of the "now" line.
     */
    public int getNowLineStrokeWidth() {
        return (int) configWrapper.getNowLinePaint().getStrokeWidth();
    }

    /**
     * Set the "now" line thickness.
     *
     * @param width The thickness of the "now" line.
     */
    public void setNowLineStrokeWidth(int width) {
        configWrapper.getNowLinePaint().setStrokeWidth(width);
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
        return configWrapper.getShowNowLineDot();
    }

    /**
     * Set whether the dot on the left-hand side of the "now" line should be displayed
     *
     * @param show True if "now" line dot should be displayed.
     */
    public void setShowNowLineDot(boolean show) {
        configWrapper.setShowNowLineDot(show);
        invalidate();
    }

    /**
     * Get the color of the dot on the left-hand side of the "now" line.
     *
     * @return The color of the "now" line dot.
     */
    public int getNowLineDotColor() {
        return configWrapper.getNowDotPaint().getColor();
    }

    /**
     * Set the color of the dot on the left-hand side of the "now" line.
     *
     * @param color The color of the "now" line dot.
     */
    public void setNowLineDotColor(int color) {
        configWrapper.getNowDotPaint().setColor(color);
        invalidate();
    }

    /**
     * Get the radius of the dot on the left-hand side of the "now" line.
     *
     * @return The radius of the "now" line dot.
     */
    public int getNowLineDotRadius() {
        return (int) configWrapper.getNowDotPaint().getStrokeWidth();
    }

    /**
     * Set the radius of the dot on the left-hand side of the "now" line.
     *
     * @param radius The radius of the "now" line dot.
     */
    public void setNowLineDotRadius(int radius) {
        configWrapper.getNowDotPaint().setStrokeWidth(radius);
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Hour separators
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean showHourSeparators() {
        return configWrapper.getShowHourSeparators();
    }

    public void setShowHourSeparators(boolean show) {
        configWrapper.setShowHourSeparators(show);
        invalidate();
    }

    public int getHourSeparatorColor() {
        return configWrapper.getHourSeparatorPaint().getColor();
    }

    public void setHourSeparatorColor(int color) {
        configWrapper.getHourSeparatorPaint().setColor(color);
        invalidate();
    }

    public int getHourSeparatorStrokeWidth() {
        return (int) configWrapper.getHourSeparatorPaint().getStrokeWidth();
    }

    public void setHourSeparatorStrokeWidth(int width) {
        configWrapper.getHourSeparatorPaint().setStrokeWidth(width);
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Day separators
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean showDaySeparators() {
        return configWrapper.getShowDaySeparators();
    }

    public void setShowDaySeparators(boolean show) {
        configWrapper.setShowDaySeparators(show);
        invalidate();
    }

    public int getDaySeparatorColor() {
        return configWrapper.getDaySeparatorPaint().getColor();
    }

    public void setDaySeparatorColor(int color) {
        configWrapper.getDaySeparatorPaint().setColor(color);
        invalidate();
    }

    public int getDaySeparatorStrokeWidth() {
        return (int) configWrapper.getDaySeparatorPaint().getStrokeWidth();
    }

    public void setDaySeparatorStrokeWidth(int width) {
        configWrapper.getDaySeparatorPaint().setStrokeWidth(width);
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Date range
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Nullable
    public Calendar getMinDate() {
        if (configWrapper.getMinDate() != null) {
            return toCalendar(configWrapper.getMinDate());
        } else {
            return null;
        }
    }

    public void setMinDate(Calendar minDate) {
        LocalDate minLocalDate = toLocalDate(minDate);
        if (configWrapper.getMaxDate() != null && configWrapper.getMaxDate().isBefore(minLocalDate)) {
            throw new IllegalArgumentException("Can't set a minDate that's after maxDate");
        }

        configWrapper.setMinDate(minLocalDate);
        invalidate();
    }

    @Nullable
    public Calendar getMaxDate() {
        if (configWrapper.getMaxDate() != null) {
            return toCalendar(configWrapper.getMaxDate());
        } else {
            return null;
        }
    }

    public void setMaxDate(Calendar maxDate) {
        LocalDate maxLocalDate = toLocalDate(maxDate);
        if (configWrapper.getMinDate() != null && configWrapper.getMinDate().isAfter(maxLocalDate)) {
            throw new IllegalArgumentException("Can't set a maxDate that's before minDate");
        }

        configWrapper.setMaxDate(maxLocalDate);
        invalidate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Time range
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public int getMinHour() {
        return configWrapper.getMinHour();
    }

    public void setMinHour(int minHour) {
        if (minHour < 0 || minHour > configWrapper.getMaxHour()) {
            throw new IllegalArgumentException("minHour must be larger than 0 and smaller than maxHour.");
        }

        configWrapper.setMinHour(minHour);
        invalidate();
    }

    public int getMaxHour() {
        return configWrapper.getMaxHour();
    }

    public void setMaxHour(int maxHour) {
        if (maxHour > 24 || maxHour < configWrapper.getMinHour()) {
            throw new IllegalArgumentException("maxHour must be smaller than 24 and larger than minHour.");
        }

        configWrapper.setMaxHour(maxHour);
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
        return configWrapper.getXScrollingSpeed();
    }

    /**
     * Sets the speed for horizontal scrolling.
     *
     * @param speed The new horizontal scrolling speed.
     */
    public void setXScrollingSpeed(float speed) {
        configWrapper.setXScrollingSpeed(speed);
    }

    /**
     * Get whether the week view should fling horizontally.
     *
     * @return True if the week view has horizontal fling enabled.
     */
    public boolean isHorizontalFlingEnabled() {
        return configWrapper.getHorizontalFlingEnabled();
    }

    /**
     * Set whether the week view should fling horizontally.
     */
    public void setHorizontalFlingEnabled(boolean enabled) {
        configWrapper.setHorizontalFlingEnabled(enabled);
    }

    /**
     * Returns whether the user can scroll horizontally. If not, the user can
     * only scroll vertically.
     *
     * @return True if horizontal scrolling is enabled. Default is true.
     */
    public boolean isHorizontalScrollingEnabled() {
        return configWrapper.getHorizontalScrollingEnabled();
    }

    /**
     * Sets whether the user can scroll horizontally.
     */
    public void setHorizontalScrollingEnabled(boolean enabled) {
        configWrapper.setHorizontalScrollingEnabled(enabled);
    }

    /**
     * Get whether the week view should fling vertically.
     *
     * @return True if the week view has vertical fling enabled.
     */
    public boolean isVerticalFlingEnabled() {
        return configWrapper.getVerticalFlingEnabled();
    }

    /**
     * Set whether the week view should fling vertically.
     */
    public void setVerticalFlingEnabled(boolean enabled) {
        configWrapper.setVerticalFlingEnabled(enabled);
    }

    /**
     * Get scroll duration
     *
     * @return scroll duration
     */
    public int getScrollDuration() {
        return configWrapper.getScrollDuration();
    }

    /**
     * Set the scroll duration
     */
    public void setScrollDuration(int duration) {
        configWrapper.setScrollDuration(duration);
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
        LocalDate firstVisibleDay = viewState.getFirstVisibleDay();
        if (firstVisibleDay != null) {
            return toCalendar(firstVisibleDay);
        } else {
            return null;
        }
    }

    /**
     * Returns the last visible day in the week view.
     *
     * @return The last visible day in the week view.
     */
    public Calendar getLastVisibleDay() {
        LocalDate lastVisibleDay = viewState.getLastVisibleDay();
        if (lastVisibleDay != null) {
            return toCalendar(lastVisibleDay);
        } else {
            return null;
        }
    }

    /**
     * Show today on the week view.
     */
    public void goToToday() {
        goToDate(toCalendar(today()));
    }

    public void goToCurrentTime() {
        final Calendar now = Calendar.getInstance();
        final int hour = now.get(HOUR_OF_DAY);
        goToDate(now);
        goToHour(hour);
    }

    /**
     * Show a specific day on the week view.
     *
     * @param date The date to show.
     */
    public void goToDate(@NonNull Calendar date) {
        LocalDate modifiedDate = toLocalDate(date);

        final LocalDate minDate = configWrapper.getMinDate();
        final LocalDate maxDate = configWrapper.getMaxDate();

        final int numberOfVisibleDays = configWrapper.getNumberOfVisibleDays();
        final boolean showFirstDayOfWeekFirst = configWrapper.getShowFirstDayOfWeekFirst();

        // If a minimum or maximum date is set, don't allow to go beyond them.
        if (minDate != null && modifiedDate.isBefore(minDate)) {
            modifiedDate = minDate;
        } else if (maxDate != null && modifiedDate.isAfter(maxDate)) {
            modifiedDate = maxDate.plusDays(1 - numberOfVisibleDays);
        } else if (numberOfVisibleDays >= 7 && showFirstDayOfWeekFirst) {
            final int diff = configWrapper.computeDifferenceWithFirstDayOfWeek(modifiedDate);
            modifiedDate = modifiedDate.minusDays(diff);
        }

        gestureHandler.forceScrollFinished();

        if (viewState.getAreDimensionsInvalid()) {
            viewState.setScrollToDay(modifiedDate);
            return;
        }

        viewState.setShouldRefreshEvents(true);

        final int diff = DateUtils.getDaysFromToday(modifiedDate);

        configWrapper.getCurrentOrigin().x = diff * (-1) * configWrapper.getTotalDayWidth();
        viewState.setRequiresPostInvalidateOnAnimation(true);
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
        if (viewState.getAreDimensionsInvalid()) {
            viewState.setScrollToHour(hour);
            return;
        }

        hour = min(hour, configWrapper.getHoursPerDay());
        float verticalOffset = configWrapper.getHourHeight() * hour;

        final float dayHeight = configWrapper.getTotalDayHeight();
        final double viewHeight = getHeight();

        final double desiredOffset = dayHeight - viewHeight;
        verticalOffset = min((float) desiredOffset, verticalOffset);

        configWrapper.getCurrentOrigin().y = -verticalOffset;
        invalidate();
    }

    /**
     * Get the first hour that is visible on the screen.
     *
     * @return The first hour that is visible.
     */
    public double getFirstVisibleHour() {
        return (configWrapper.getCurrentOrigin().y * -1) / configWrapper.getHourHeight();
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
        return configWrapper.getDateTimeInterpreter(); // getDateTimeInterpreter(getContext());
    }

    /**
     * Set the interpreter which provides the text to show in the header column and the header row.
     *
     * @param dateTimeInterpreter The date, time interpreter.
     */
    public void setDateTimeInterpreter(DateTimeInterpreter dateTimeInterpreter) {
        configWrapper.setDateTimeInterpreter(dateTimeInterpreter);
    }

    protected static class SavedState extends BaseSavedState {

        private final int numberOfVisibleDays;
        private final LocalDate firstVisibleDate;

        private SavedState(Parcelable superState,
                           int numberOfVisibleDays, LocalDate firstVisibleDate) {
            super(superState);
            this.numberOfVisibleDays = numberOfVisibleDays;
            this.firstVisibleDate = firstVisibleDate;
        }

        private SavedState(Parcel in) {
            super(in);
            numberOfVisibleDays = in.readInt();
            firstVisibleDate = (LocalDate) in.readSerializable();
        }

        @Override
        public void writeToParcel(Parcel destination, int flags) {
            super.writeToParcel(destination, flags);
            destination.writeInt(numberOfVisibleDays);
            destination.writeSerializable(firstVisibleDate);
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

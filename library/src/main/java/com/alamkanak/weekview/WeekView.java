package com.alamkanak.weekview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;

public abstract class WeekView<T> extends View {

    public WeekView(Context context) {
        super(context);
    }

    public WeekView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WeekView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WeekView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Calendar configuration
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract int getFirstDayOfWeek();

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
    public abstract void setFirstDayOfWeek(int firstDayOfWeek);

    /**
     * Get the number of visible days in a week.
     *
     * @return The number of visible days in a week.
     */
    public abstract int getNumberOfVisibleDays();

    /**
     * Set the number of visible days in a week.
     *
     * @param numberOfVisibleDays The number of visible days in a week.
     */
    public abstract void setNumberOfVisibleDays(int numberOfVisibleDays);

    public abstract boolean isShowFirstDayOfWeekFirst();

    public abstract void setShowFirstDayOfWeekFirst(boolean show);

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Header bottom line
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract boolean getShowHeaderRowBottomLine();

    public abstract void setShowHeaderRowBottomLine(boolean showHeaderRowBottomLine);

    public abstract int getHeaderRowBottomLineColor();

    public abstract void setHeaderRowBottomLineColor(int headerRowBottomLineColor);

    public abstract int getHeaderRowBottomLineWidth();

    public abstract void setHeaderRowBottomLineWidth(int headerRowBottomLineWidth);

    public abstract int getTodayHeaderTextColor();

    public abstract void setTodayHeaderTextColor(int todayHeaderTextColor);

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Time column
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract int getTimeColumnPadding();

    public abstract void setTimeColumnPadding(int timeColumnPadding);

    public abstract int getTimeColumnTextColor();

    public abstract void setTimeColumnTextColor(int timeColumnTextColor);

    public abstract int getTimeColumnBackgroundColor();

    public abstract void setTimeColumnBackgroundColor(int timeColumnBackgroundColor);

    public abstract int getTimeColumTextSize();

    public abstract void setTimeColumnTextSize(int textSize);

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Time column separator
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract boolean getShowTimeColumnSeparator();

    public abstract void setShowTimeColumnSeparator(boolean showTimeColumnSeparator);

    public abstract int getTimeColumnSeparatorColor();

    public abstract void setTimeColumnSeparatorColor(int timeColumnSeparatorColor);

    public abstract int getTimeColumnSeparatorWidth();

    public abstract void setTimeColumnSeparatorWidth(int timeColumnSeparatorStrokeWidth);

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Header row
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract int getHeaderRowPadding();

    public abstract void setHeaderRowPadding(int headerRowPadding);

    public abstract int getHeaderRowBackgroundColor();

    public abstract void setHeaderRowBackgroundColor(int headerRowBackgroundColor);

    public abstract int getHeaderRowTextColor();

    public abstract void setHeaderRowTextColor(int headerRowTextColor);

    public abstract int getHeaderRowTextSize();

    public abstract void setHeaderRownTextSize(int textSize);

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Event chips
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get the height of all-day events.
     *
     * @return Height of all-day events.
     */
    public abstract int getAllDayEventHeight();

    /**
     * Set the height of AllDay-events.
     */
    public abstract void setAllDayEventHeight(int height);

    public abstract int getEventCornerRadius();

    /**
     * Set corner radius for event rect.
     *
     * @param eventCornerRadius the radius in px.
     */
    public abstract void setEventCornerRadius(int eventCornerRadius);

    public abstract int getEventTextSize();

    public abstract void setEventTextSize(int eventTextSize);

    public abstract int getEventTextColor();

    public abstract void setEventTextColor(int eventTextColor);

    public abstract int getEventPadding();

    public abstract void setEventPadding(int eventPadding);

    public abstract int getDefaultEventColor();

    public abstract void setDefaultEventColor(int defaultEventColor);

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Event margins
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract int getColumnGap();

    public abstract void setColumnGap(int columnGap);

    public abstract int getOverlappingEventGap();

    /**
     * Set the gap between overlapping events.
     *
     * @param overlappingEventGap The gap between overlapping events.
     */
    public abstract void setOverlappingEventGap(int overlappingEventGap);

    public abstract int getEventMarginVertical();

    /**
     * Set the top and bottom margin of the event. The event will release this margin from the top
     * and bottom edge. This margin is useful for differentiation consecutive events.
     *
     * @param eventMarginVertical The top and bottom margin.
     */
    public abstract void setEventMarginVertical(int eventMarginVertical);

    /**
     * Set the start and end margin of the event. The event will release this margin from the start
     * and end edge.
     *
     * @param eventMarginHorizontal The start and end margin.
     */
    public abstract void setEventMarginHorizontal(int eventMarginHorizontal);

    public abstract int getEventMarginHorizontal();

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Colors
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract int getDayBackgroundColor();

    public abstract void setDayBackgroundColor(int dayBackgroundColor);

    public abstract int getTodayBackgroundColor();

    public abstract void setTodayBackgroundColor(int todayBackgroundColor);

    /**
     * Whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     *
     * @return True if weekends should have different background colors.
     */
    public abstract boolean isShowDistinctWeekendColor();

    /**
     * Set whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     *
     * @param showDistinctWeekendColor True if weekends should have different background colors.
     */
    public abstract void setShowDistinctWeekendColor(boolean showDistinctWeekendColor);

    /**
     * Whether past and future days should have two different background colors. The past and
     * future day colors are defined by the attributes `futureBackgroundColor` and
     * `pastBackgroundColor`.
     *
     * @return True if past and future days should have two different background colors.
     */
    public abstract boolean isShowDistinctPastFutureColor();

    /**
     * Set whether weekends should have a background color different from the normal day background
     * color. The past and future day colors are defined by the attributes `futureBackgroundColor`
     * and `pastBackgroundColor`.
     *
     * @param showDistinctPastFutureColor True if past and future should have two different
     *                                    background colors.
     */
    public abstract void setShowDistinctPastFutureColor(boolean showDistinctPastFutureColor);

    // TODO: Past & future background color, pastWeekend and futureWeekend

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Hour height
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract int getHourHeight();

    public abstract void setHourHeight(int hourHeight);

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
    public abstract boolean isShowNowLine();

    /**
     * Set whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineStrokeWidth`.
     *
     * @param showNowLine True if "now" line should be displayed.
     */
    public abstract void setShowNowLine(boolean showNowLine);

    /**
     * Get the "now" line color.
     *
     * @return The color of the "now" line.
     */
    public abstract int getNowLineColor();

    /**
     * Set the "now" line color.
     *
     * @param nowLineColor The color of the "now" line.
     */
    public abstract void setNowLineColor(int nowLineColor);

    /**
     * Get the "now" line thickness.
     *
     * @return The thickness of the "now" line.
     */
    public abstract int getNowLineStrokeWidth();

    /**
     * Set the "now" line thickness.
     *
     * @param nowLineStrokeWidth The thickness of the "now" line.
     */
    public abstract void setNowLineStrokeWidth(int nowLineStrokeWidth) ;

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
    public abstract boolean isShowNowLineDot();

    /**
     * Set whether the dot on the left-hand side of the "now" line should be displayed
     *
     * @param showNowLineDot True if "now" line dot should be displayed.
     */
    public abstract void setShowNowLineDot(boolean showNowLineDot);

    /**
     * Get the color of the dot on the left-hand side of the "now" line.
     *
     * @return The color of the "now" line dot.
     */
    public abstract int getNowLineDotColor();

    /**
     * Set the color of the dot on the left-hand side of the "now" line.
     *
     * @param nowLineDotColor The color of the "now" line dot.
     */
    public abstract void setNowLineDotColor(int nowLineDotColor);

    /**
     * Get the radius of the dot on the left-hand side of the "now" line.
     *
     * @return The radius of the "now" line dot.
     */
    public abstract int getNowLineDotRadius();

    /**
     * Set the radius of the dot on the left-hand side of the "now" line.
     *
     * @param nowLineDotRadius The radius of the "now" line dot.
     */
    public abstract void setNowLineDotRadius(int nowLineDotRadius);

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Hour separators
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract boolean showHourSeparators();

    public abstract void setShowHourSeparators(boolean showHourSeparators);

    public abstract int getHourSeparatorColor();

    public abstract void setHourSeparatorColor(int hourSeparatorColor);

    public abstract int getHourSeparatorStrokeWidth();

    public abstract void setHourSeparatorStrokeWidth(int hourSeparatorWidth);

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Day separators
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract boolean showDaySeparators();

    public abstract void setShowDaySeparators(boolean showDaySeparators);

    public abstract int getDaySeparatorColor();

    public abstract void setDaySeparatorColor(int daySeparatorColor);

    public abstract int getDaySeparatorStrokeWidth();

    public abstract void setDaySeparatorStrokeWidth(int daySeparatorWidth);

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
    public abstract float getXScrollingSpeed();

    /**
     * Sets the speed for horizontal scrolling.
     *
     * @param xScrollingSpeed The new horizontal scrolling speed.
     */
    public abstract void setXScrollingSpeed(float xScrollingSpeed);

    /**
     * Get whether the week view should fling horizontally.
     *
     * @return True if the week view has horizontal fling enabled.
     */
    public abstract boolean isHorizontalFlingEnabled();

    /**
     * Set whether the week view should fling horizontally.
     */
    public abstract void setHorizontalFlingEnabled(boolean enabled);

    /**
     * Returns whether the user can scroll horizontally. If not, the user can
     * only scroll vertically.
     *
     * @return True if horizontal scrolling is enabled. Default is true.
     */
    public abstract boolean isHorizontalScrollingEnabled();

    /**
     * Sets whether the user can scroll horizontally.
     */
    public abstract void setHorizontalScrollingEnabled(boolean enabled);

    /**
     * Get whether the week view should fling vertically.
     *
     * @return True if the week view has vertical fling enabled.
     */
    public abstract boolean isVerticalFlingEnabled();

    /**
     * Set whether the week view should fling vertically.
     */
    public abstract void setVerticalFlingEnabled(boolean enabled);

    /**
     * Get scroll duration
     *
     * @return scroll duration
     */
    public abstract int getScrollDuration();

    /**
     * Set the scroll duration
     */
    public abstract void setScrollDuration(int scrollDuration);


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
    public abstract Calendar getFirstVisibleDay();

    /**
     * Returns the last visible day in the week view.
     *
     * @return The last visible day in the week view.
     */
    public abstract Calendar getLastVisibleDay();

    /**
     * Show today on the week view.
     */
    public abstract void goToToday();

    public abstract void goToCurrentTime();

    /**
     * Show a specific day on the week view.
     *
     * @param date The date to show.
     */
    public abstract void goToDate(Calendar date);

    /**
     * Refreshes the view and loads the events again.
     */
    public abstract void notifyDataSetChanged();

    /**
     * Vertically scroll to a specific hour in the week view.
     *
     * @param hour The hour to scroll to in 24-hour format. Supported values are 0-24.
     */
    public abstract void goToHour(int hour);

    /**
     * Get the first hour that is visible on the screen.
     *
     * @return The first hour that is visible.
     */
    public abstract double getFirstVisibleHour();

    /////////////////////////////////////////////////////////////////
    //
    //  Listeners
    //
    /////////////////////////////////////////////////////////////////

    public abstract void setOnEventClickListener(EventClickListener<T> listener);

    public abstract EventClickListener getEventClickListener();

    @Nullable
    public abstract MonthLoader.MonthChangeListener getMonthChangeListener();

    public abstract void setMonthChangeListener(@Nullable MonthLoader.MonthChangeListener<T> monthChangeListener);

    /**
     * Get event loader in the week view. Event loaders define the  interval after which the events
     * are loaded in week view. For a MonthLoader events are loaded for every month. You can define
     * your custom event loader by extending WeekViewLoader.
     *
     * @return The event loader.
     */
    public abstract WeekViewLoader<T> getWeekViewLoader();

    /**
     * Set event loader in the week view. For example, a MonthLoader. Event loaders define the
     * interval after which the events are loaded in week view. For a MonthLoader events are loaded
     * for every month. You can define your custom event loader by extending WeekViewLoader.
     *
     * @param weekViewLoader The event loader.
     */
    public abstract void setWeekViewLoader(WeekViewLoader<T> weekViewLoader);

    public abstract EventLongPressListener getEventLongPressListener();

    public abstract void setEventLongPressListener(EventLongPressListener<T> eventLongPressListener);

    public abstract void setEmptyViewClickListener(EmptyViewClickListener emptyViewClickListener);

    public abstract EmptyViewClickListener getEmptyViewClickListener();

    public abstract void setEmptyViewLongPressListener(EmptyViewLongPressListener emptyViewLongPressListener);

    public abstract EmptyViewLongPressListener getEmptyViewLongPressListener();

    public abstract void setScrollListener(ScrollListener scrollListener);

    public abstract ScrollListener getScrollListener();

    /**
     * Get the interpreter which provides the text to show in the header column and the header row.
     *
     * @return The date, time interpreter.
     */
    public abstract DateTimeInterpreter getDateTimeInterpreter();

    /**
     * Set the interpreter which provides the text to show in the header column and the header row.
     *
     * @param dateTimeInterpreter The date, time interpreter.
     */
    public abstract void setDateTimeInterpreter(DateTimeInterpreter dateTimeInterpreter);

}

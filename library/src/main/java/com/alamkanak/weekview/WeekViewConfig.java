package com.alamkanak.weekview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.util.Calendar;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

public class WeekViewConfig {

    WeekViewDrawingConfig drawingConfig;

    // Calendar configuration
    int firstDayOfWeek;
    int numberOfVisibleDays;
    boolean showFirstDayOfWeekFirst;
    boolean showCurrentTimeFirst;

    // Header bottom line
    boolean showHeaderRowBottomLine;
    int headerRowBottomLineColor;
    int headerRowBottomLineWidth;

    // Time column
    int timeColumnTextColor;
    int timeColumnBackgroundColor;
    int timeColumnPadding;
    int timeColumnTextSize;
    boolean showMidnightHour;
    boolean showTimeColumnHourSeparator;
    int timeColumnHoursInterval;

    // Time column separator
    boolean showTimeColumnSeparator;
    int timeColumnSeparatorColor;
    int timeColumnSeparatorStrokeWidth;

    // Header row
    int headerRowTextColor;
    int headerRowBackgroundColor;
    int headerRowTextSize;
    int headerRowPadding;
    int todayHeaderTextColor;
    boolean singleLineHeader;

    // Event chips
    int eventCornerRadius;
    int eventTextSize;
    int eventTextColor;
    int eventPadding;
    int defaultEventColor;
    int allDayEventTextSize;

    // Event margins
    int columnGap;
    int overlappingEventGap;
    int eventMarginVertical;
    int eventMarginHorizontal;

    // Colors
    int dayBackgroundColor;
    int todayBackgroundColor;
    boolean showDistinctWeekendColor;
    boolean showDistinctPastFutureColor;
    int pastBackgroundColor;
    int futureBackgroundColor;
    int pastWeekendBackgroundColor;
    int futureWeekendBackgroundColor;

    // Hour height
    float hourHeight;
    int minHourHeight;
    int maxHourHeight;
    int effectiveMinHourHeight;
    boolean showCompleteDay;

    // Now line
    boolean showNowLine;
    int nowLineColor;
    int nowLineStrokeWidth;

    // Now line dot
    boolean showNowLineDot;
    int nowLineDotColor;
    int nowLineDotRadius;

    // Hour separators
    boolean showHourSeparator;
    int hourSeparatorColor;
    int hourSeparatorStrokeWidth;

    // Day separators
    boolean showDaySeparator;
    int daySeparatorColor;
    int daySeparatorStrokeWidth;

    // Scrolling
    float xScrollingSpeed;
    boolean verticalFlingEnabled;
    boolean horizontalFlingEnabled;
    boolean horizontalScrollingEnabled;
    int scrollDuration;

    // Date range
    Calendar minDate;
    Calendar maxDate;

    // Time range
    int minHour;
    int maxHour;

    WeekViewConfig(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0);
        try {
            // Calendar configuration
            firstDayOfWeek = a.getInteger(R.styleable.WeekView_firstDayOfWeek, Calendar.MONDAY);
            numberOfVisibleDays = a.getInteger(R.styleable.WeekView_numberOfVisibleDays, 3);
            showFirstDayOfWeekFirst = a.getBoolean(R.styleable.WeekView_showFirstDayOfWeekFirst, false);
            showCurrentTimeFirst = a.getBoolean(R.styleable.WeekView_showCurrentTimeFirst, false);

            // Header bottom line
            showHeaderRowBottomLine = a.getBoolean(R.styleable.WeekView_showHeaderRowBottomLine, false);
            headerRowBottomLineColor = a.getColor(R.styleable.WeekView_headerRowBottomLineColor, Defaults.GRID_COLOR);
            headerRowBottomLineWidth = a.getDimensionPixelSize(R.styleable.WeekView_headerRowBottomLineWidth, 1);

            // Time column
            timeColumnTextColor = a.getColor(R.styleable.WeekView_timeColumnTextColor, Color.BLACK);
            timeColumnBackgroundColor = a.getColor(R.styleable.WeekView_timeColumnBackgroundColor, Color.WHITE);
            timeColumnPadding = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnPadding, 10);
            timeColumnTextSize = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnTextSize, Defaults.textSize(context));
            showMidnightHour = a.getBoolean(R.styleable.WeekView_showMidnightHour, false);
            showTimeColumnHourSeparator = a.getBoolean(R.styleable.WeekView_showTimeColumnHourSeparator, false);
            timeColumnHoursInterval = a.getInteger(R.styleable.WeekView_timeColumnHoursInterval, 1);

            // Time column separator
            showTimeColumnSeparator = a.getBoolean(R.styleable.WeekView_showTimeColumnSeparator, false);
            timeColumnSeparatorColor = a.getColor(R.styleable.WeekView_timeColumnSeparatorColor, Defaults.GRID_COLOR);
            timeColumnSeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnSeparatorStrokeWidth, 1);

            // Time range
            minHour = a.getInt(R.styleable.WeekView_minHour, 0);
            maxHour = a.getInt(R.styleable.WeekView_maxHour, 24);

            // Header row
            headerRowTextColor = a.getColor(R.styleable.WeekView_headerRowTextColor, Color.BLACK);
            headerRowBackgroundColor = a.getColor(R.styleable.WeekView_headerRowBackgroundColor, Color.WHITE);
            headerRowTextSize = a.getDimensionPixelSize(R.styleable.WeekView_headerRowTextSize, Defaults.textSize(context));
            headerRowPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerRowPadding, 10);
            todayHeaderTextColor = a.getColor(R.styleable.WeekView_todayHeaderTextColor, Defaults.HIGHLIGHT_COLOR);
            singleLineHeader = a.getBoolean(R.styleable.WeekView_singleLineHeader, true);

            // Event chips
            eventCornerRadius = a.getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, 0);
            eventTextSize = a.getDimensionPixelSize(R.styleable.WeekView_eventTextSize, Defaults.textSize(context));
            eventTextColor = a.getColor(R.styleable.WeekView_eventTextColor, Color.BLACK);
            eventPadding = a.getDimensionPixelSize(R.styleable.WeekView_eventPadding, 8);
            defaultEventColor = a.getColor(R.styleable.WeekView_defaultEventColor, Defaults.EVENT_COLOR);
            allDayEventTextSize = a.getDimensionPixelSize(R.styleable.WeekView_allDayEventTextSize, eventTextSize);

            // Event margins
            columnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, 10);
            overlappingEventGap = a.getDimensionPixelSize(R.styleable.WeekView_overlappingEventGap, 0);
            eventMarginVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventMarginVertical, 3);
            eventMarginHorizontal = a.getDimensionPixelSize(R.styleable.WeekView_singleDayHorizontalMargin, 0);

            // Colors
            dayBackgroundColor = a.getColor(R.styleable.WeekView_dayBackgroundColor, Defaults.BACKGROUND_COLOR);
            todayBackgroundColor = a.getColor(R.styleable.WeekView_todayBackgroundColor, Defaults.BACKGROUND_COLOR);
            showDistinctPastFutureColor = a.getBoolean(R.styleable.WeekView_showDistinctPastFutureColor, false);
            showDistinctWeekendColor = a.getBoolean(R.styleable.WeekView_showDistinctWeekendColor, false);
            pastBackgroundColor = a.getColor(R.styleable.WeekView_pastBackgroundColor, Defaults.PAST_BACKGROUND_COLOR);
            futureBackgroundColor = a.getColor(R.styleable.WeekView_futureBackgroundColor, Defaults.FUTURE_BACKGROUND_COLOR);
            pastWeekendBackgroundColor = a.getColor(R.styleable.WeekView_pastWeekendBackgroundColor, pastBackgroundColor);
            futureWeekendBackgroundColor = a.getColor(R.styleable.WeekView_futureWeekendBackgroundColor, futureBackgroundColor);

            // Hour height
            hourHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourHeight, 50);
            minHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_minHourHeight, 0);
            maxHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_maxHourHeight, 250);
            effectiveMinHourHeight = minHourHeight;
            showCompleteDay = a.getBoolean(R.styleable.WeekView_showCompleteDay, false);

            // Now line
            showNowLine = a.getBoolean(R.styleable.WeekView_showNowLine, false);
            nowLineColor = a.getColor(R.styleable.WeekView_nowLineColor, Defaults.NOW_COLOR);
            nowLineStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_nowLineStrokeWidth, 5);

            // Now line dot
            showNowLineDot = a.getBoolean(R.styleable.WeekView_showNowLineDot, false);
            nowLineDotColor = a.getColor(R.styleable.WeekView_nowLineDotColor, Defaults.NOW_COLOR);
            nowLineDotRadius = a.getDimensionPixelSize(R.styleable.WeekView_nowLineDotRadius, 16);

            // Hour separators
            showHourSeparator = a.getBoolean(R.styleable.WeekView_showHourSeparator, true);
            hourSeparatorColor = a.getColor(R.styleable.WeekView_hourSeparatorColor, Defaults.SEPARATOR_COLOR);
            hourSeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_hourSeparatorStrokeWidth, 2);

            // Day separators
            showDaySeparator = a.getBoolean(R.styleable.WeekView_showDaySeparator, true);
            daySeparatorColor = a.getColor(R.styleable.WeekView_daySeparatorColor, Defaults.SEPARATOR_COLOR);
            daySeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_daySeparatorStrokeWidth, 2);

            // Scrolling
            xScrollingSpeed = a.getFloat(R.styleable.WeekView_xScrollingSpeed, 1f);
            horizontalFlingEnabled = a.getBoolean(R.styleable.WeekView_horizontalFlingEnabled, true);
            horizontalScrollingEnabled = a.getBoolean(R.styleable.WeekView_horizontalScrollingEnabled, true);
            verticalFlingEnabled = a.getBoolean(R.styleable.WeekView_verticalFlingEnabled, true);
            scrollDuration = a.getInt(R.styleable.WeekView_scrollDuration, 250);
        } finally {
            a.recycle();
        }
    }

    void setNumberOfVisibleDays(int numberOfVisibleDays) {
        this.numberOfVisibleDays = numberOfVisibleDays;
    }

    void setTimeColumnTextSize(int timeColumnTextSize) {
        this.timeColumnTextSize = timeColumnTextSize;
        drawingConfig.setTextSize(timeColumnTextSize);
    }

    void setTimeColumnTextColor(int textColor) {
        this.timeColumnTextColor = textColor;
        drawingConfig.setTimeColumnTextColor(textColor);
    }

    void setHeaderRowBackgroundColor(int headerRowBackgroundColor) {
        this.headerRowBackgroundColor = headerRowBackgroundColor;
        drawingConfig.headerBackgroundPaint.setColor(headerRowBackgroundColor);
    }

    void setHeaderRowTextColor(int color) {
        headerRowTextColor = color;
        drawingConfig.setHeaderRowTextColor(color);
    }

    void setHeaderRowTextSize(int size) {
        headerRowTextSize = size;
        drawingConfig.setHeaderRowTextSize(size);
    }

    void setDayBackgroundColor(int dayBackgroundColor) {
        this.dayBackgroundColor = dayBackgroundColor;
        drawingConfig.dayBackgroundPaint.setColor(dayBackgroundColor);
    }

    void setTodayBackgroundColor(int todayBackgroundColor) {
        this.todayBackgroundColor = todayBackgroundColor;
        drawingConfig.todayBackgroundPaint.setColor(todayBackgroundColor);
    }

    void setHourSeparatorStrokeWidth(int hourSeparatorWidth) {
        this.hourSeparatorStrokeWidth = hourSeparatorWidth;
        drawingConfig.hourSeparatorPaint.setStrokeWidth(hourSeparatorWidth);
    }

    void setTodayHeaderTextColor(int todayHeaderTextColor) {
        this.todayHeaderTextColor = todayHeaderTextColor;
        drawingConfig.todayHeaderTextPaint.setColor(todayHeaderTextColor);
    }

    void setEventTextSize(int eventTextSize) {
        this.eventTextSize = eventTextSize;
        drawingConfig.eventTextPaint.setTextSize(eventTextSize);
    }

    void setAllDayEventTextSize(int allDayEventTextSize) {
        this.allDayEventTextSize = allDayEventTextSize;
        drawingConfig.allDayEventTextPaint.setTextSize(allDayEventTextSize);
    }

    void setEventTextColor(int eventTextColor) {
        this.eventTextColor = eventTextColor;
        drawingConfig.eventTextPaint.setColor(eventTextColor);
    }

    void setTimeColumnBackgroundColor(int timeColumnBackgroundColor) {
        this.timeColumnBackgroundColor = timeColumnBackgroundColor;
        drawingConfig.timeColumnBackgroundPaint.setColor(timeColumnBackgroundColor);
    }

    public void setMinDate(Calendar minDate) {
        this.minDate = minDate;
    }

    public void setMaxDate(Calendar maxDate) {
        this.maxDate = maxDate;
    }

    float getMinX() {
        if (maxDate != null) {
            Calendar date = (Calendar) maxDate.clone();
            date.add(Calendar.DAY_OF_YEAR,1-numberOfVisibleDays);
            return getXOriginForDate(date);
        }
        return Float.NEGATIVE_INFINITY;
    }

    float getMaxX() {
        if (minDate != null) {
            return getXOriginForDate(minDate);
        }
        return Float.POSITIVE_INFINITY;
    }

    float getTotalDayWidth() {
        return drawingConfig.widthPerDay + columnGap;
    }

    float getTotalDayHeight() {
        float dayHeight = hourHeight * getHoursPerDay();
        return dayHeight + drawingConfig.headerHeight;
    }

    boolean isSingleDay() {
        return numberOfVisibleDays == 1;
    }

    int getStartHour() {
        return (showMidnightHour && showTimeColumnHourSeparator) ? minHour : timeColumnHoursInterval;
    }

    int getHoursPerDay() {
        return maxHour - minHour;
    }

    int getMinutesPerDay() {
        return getHoursPerDay() * Constants.MINUTES_PER_HOUR;
    }

    private float getXOriginForDate(Calendar date) {
        return (-1f) * DateUtils.getDaysUntilDate(date) * getTotalDayWidth();
    }

    static class Defaults {

        static final int BACKGROUND_COLOR = Color.WHITE;
        static final int PAST_BACKGROUND_COLOR = Color.rgb(227, 227, 227);
        static final int FUTURE_BACKGROUND_COLOR = Color.rgb(245, 245, 245);
        static final int EVENT_COLOR = Color.rgb(159, 198, 231);
        static final int GRID_COLOR = Color.rgb(102, 102, 102);
        static final int NOW_COLOR = Color.BLACK;
        static final int SEPARATOR_COLOR = Color.rgb(230, 230, 230);
        static final int HIGHLIGHT_COLOR = Color.rgb(39, 137, 228);

        static int textSize(Context context) {
            return convertTextDimension(context, 12);
        }

        private static int convertTextDimension(Context context, int textSize) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(COMPLEX_UNIT_SP, textSize, displayMetrics);
        }

    }

}

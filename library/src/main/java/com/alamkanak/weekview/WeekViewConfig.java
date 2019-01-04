package com.alamkanak.weekview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;

import java.util.Calendar;

final class WeekViewConfig {

    WeekViewDrawingConfig drawingConfig;

    // Calendar configuration
    int firstDayOfWeek = Calendar.MONDAY;
    int numberOfVisibleDays = 3;
    boolean showFirstDayOfWeekFirst = false;

    // Header bottom line
    boolean showHeaderRowBottomLine = false;
    int headerRowBottomLineColor = Color.rgb(102, 102, 102);
    int headerRowBottomLineWidth = 1;

    // Time column
    int timeColumnTextColor = Color.BLACK;
    int timeColumnBackgroundColor = Color.WHITE;
    int timeColumnPadding = 10;
    int timeColumnTextSize = 12;

    // Time column separator
    boolean showTimeColumnSeparator = false;
    int timeColumnSeparatorColor = Color.rgb(102, 102, 102);
    int timeColumnSeparatorStrokeWidth = 1;

    // Header row
    int headerRowTextSize = 12;
    int headerRowTextColor = Color.BLACK;
    int headerRowBackgroundColor = Color.WHITE;
    int headerRowPadding = 10;
    int todayHeaderTextColor = Color.rgb(39, 137, 228);

    // Event chips
    int allDayEventHeight = 100;
    int eventCornerRadius = 0;
    int eventTextSize = 12;
    int eventTextColor = Color.BLACK;
    int eventPadding = 8;
    int defaultEventColor = Color.parseColor("#9fc6e7");

    // Event margins
    int columnGap = 10;
    int overlappingEventGap = 0;
    int eventMarginVertical = 3;
    int eventMarginHorizontal = 0;

    // Colors
    int dayBackgroundColor = Color.rgb(255, 255, 255);
    int todayBackgroundColor = Color.rgb(255, 255, 255);
    boolean showDistinctWeekendColor = false;
    boolean showDistinctPastFutureColor = false;
    int pastBackgroundColor = Color.rgb(227, 227, 227);
    int futureBackgroundColor = Color.rgb(245, 245, 245);
    int pastWeekendBackgroundColor = 0;
    int futureWeekendBackgroundColor = 0;

    // Hour height
    int hourHeight = 50;
    int minHourHeight = 0; // no minimum specified (will be dynamic, based on screen)
    int maxHourHeight = 250;
    int effectiveMinHourHeight = minHourHeight; // compensates for the fact that you can't keep zooming out.

    // Now line
    boolean showNowLine = false;
    int nowLineColor = Color.rgb(102, 102, 102);
    int nowLineStrokeWidth = 5;

    // Now line dot
    boolean showNowLineDot = false;
    int nowLineDotColor = Color.rgb(102, 102, 102);
    int nowLineDotRadius = 16;

    // Hour separators
    boolean showHourSeparator = true;
    int hourSeparatorColor = Color.rgb(230, 230, 230);
    int hourSeparatorStrokeWidth = 2;

    // Day separators
    boolean showDaySeparator = true;
    int daySeparatorColor = Color.rgb(230, 230, 230);
    int daySeparatorStrokeWidth = 2;

    // Scrolling
    float xScrollingSpeed = 1f;
    boolean verticalFlingEnabled = true;
    boolean horizontalFlingEnabled = true;
    boolean horizontalScrollingEnabled = true;
    int scrollDuration = 250;

    WeekViewConfig(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0);
        try {
            // Calendar configuration
            firstDayOfWeek = a.getInteger(R.styleable.WeekView_firstDayOfWeek, firstDayOfWeek);
            numberOfVisibleDays = a.getInteger(R.styleable.WeekView_numberOfVisibleDays, numberOfVisibleDays);
            showFirstDayOfWeekFirst = a.getBoolean(R.styleable.WeekView_showFirstDayOfWeekFirst, showFirstDayOfWeekFirst);

            // Header bottom line
            showHeaderRowBottomLine = a.getBoolean(R.styleable.WeekView_showHeaderRowBottomLine, showHeaderRowBottomLine);
            headerRowBottomLineColor = a.getColor(R.styleable.WeekView_headerRowBottomLineColor, headerRowBottomLineColor);
            headerRowBottomLineWidth = a.getDimensionPixelSize(R.styleable.WeekView_headerRowBottomLineWidth, headerRowBottomLineWidth);

            // Time column
            timeColumnTextColor = a.getColor(R.styleable.WeekView_timeColumnTextColor, timeColumnTextColor);
            timeColumnTextSize = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnTextSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, timeColumnTextSize, context.getResources().getDisplayMetrics()));
            timeColumnPadding = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnPadding, timeColumnPadding);
            timeColumnBackgroundColor = a.getColor(R.styleable.WeekView_timeColumnBackgroundColor, timeColumnBackgroundColor);

            // Time column separator
            showTimeColumnSeparator = a.getBoolean(R.styleable.WeekView_showTimeColumnSeparator, showTimeColumnSeparator);
            timeColumnSeparatorColor = a.getColor(R.styleable.WeekView_timeColumnSeparatorColor, timeColumnSeparatorColor);
            timeColumnSeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_timeColumnSeparatorStrokeWidth, timeColumnSeparatorStrokeWidth);

            // Header row
            headerRowTextColor = a.getColor(R.styleable.WeekView_headerRowTextColor, headerRowTextColor);
            headerRowBackgroundColor = a.getColor(R.styleable.WeekView_headerRowBackgroundColor, headerRowBackgroundColor);
            headerRowTextSize = a.getDimensionPixelSize(R.styleable.WeekView_headerRowTextSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, headerRowTextSize, context.getResources().getDisplayMetrics()));
            headerRowPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerRowPadding, headerRowPadding);
            todayHeaderTextColor = a.getColor(R.styleable.WeekView_todayHeaderTextColor, todayHeaderTextColor);

            // Event chips
            allDayEventHeight = a.getDimensionPixelSize(R.styleable.WeekView_allDayEventHeight, allDayEventHeight);
            eventCornerRadius = a.getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, eventCornerRadius);
            eventTextSize = a.getDimensionPixelSize(R.styleable.WeekView_eventTextSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, eventTextSize, context.getResources().getDisplayMetrics()));
            eventTextColor = a.getColor(R.styleable.WeekView_eventTextColor, eventTextColor);
            eventPadding = a.getDimensionPixelSize(R.styleable.WeekView_eventPadding, eventPadding);
            defaultEventColor = a.getColor(R.styleable.WeekView_defaultEventColor, defaultEventColor);

            // Event margins
            columnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, columnGap);
            overlappingEventGap = a.getDimensionPixelSize(R.styleable.WeekView_overlappingEventGap, overlappingEventGap);
            eventMarginVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventMarginVertical, eventMarginVertical);
            eventMarginHorizontal = a.getDimensionPixelSize(R.styleable.WeekView_singleDayHorizontalMargin, eventMarginHorizontal);

            // Colors
            dayBackgroundColor = a.getColor(R.styleable.WeekView_dayBackgroundColor, dayBackgroundColor);
            todayBackgroundColor = a.getColor(R.styleable.WeekView_todayBackgroundColor, todayBackgroundColor);
            showDistinctPastFutureColor = a.getBoolean(R.styleable.WeekView_showDistinctPastFutureColor, showDistinctPastFutureColor);
            showDistinctWeekendColor = a.getBoolean(R.styleable.WeekView_showDistinctWeekendColor, showDistinctWeekendColor);
            pastBackgroundColor = a.getColor(R.styleable.WeekView_pastBackgroundColor, pastBackgroundColor);
            futureBackgroundColor = a.getColor(R.styleable.WeekView_futureBackgroundColor, futureBackgroundColor);
            pastWeekendBackgroundColor = a.getColor(R.styleable.WeekView_pastWeekendBackgroundColor, pastBackgroundColor);
            futureWeekendBackgroundColor = a.getColor(R.styleable.WeekView_futureWeekendBackgroundColor, futureBackgroundColor); // If not set, use the same color as in the week

            // Hour height
            hourHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourHeight, hourHeight);
            minHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_minHourHeight, minHourHeight);
            maxHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_maxHourHeight, maxHourHeight);
            effectiveMinHourHeight = minHourHeight;

            // Now line
            showNowLine = a.getBoolean(R.styleable.WeekView_showNowLine, showNowLine);
            nowLineColor = a.getColor(R.styleable.WeekView_nowLineColor, nowLineColor);
            nowLineStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_nowLineStrokeWidth, nowLineStrokeWidth);

            // Now line dot
            showNowLineDot = a.getBoolean(R.styleable.WeekView_showNowLineDot, showNowLineDot);
            nowLineDotColor = a.getColor(R.styleable.WeekView_nowLineDotColor, nowLineDotColor);
            nowLineDotRadius = a.getDimensionPixelSize(R.styleable.WeekView_nowLineDotRadius, nowLineDotRadius);

            // Hour separators
            showHourSeparator = a.getBoolean(R.styleable.WeekView_showHourSeparator, showHourSeparator);
            hourSeparatorColor = a.getColor(R.styleable.WeekView_hourSeparatorColor, hourSeparatorColor);
            hourSeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_hourSeparatorStrokeWidth, hourSeparatorStrokeWidth);

            // Day separators
            showDaySeparator = a.getBoolean(R.styleable.WeekView_showDaySeparator, showDaySeparator);
            daySeparatorColor = a.getColor(R.styleable.WeekView_daySeparatorColor, daySeparatorColor);
            daySeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_daySeparatorStrokeWidth, daySeparatorStrokeWidth);

            // Scrolling
            xScrollingSpeed = a.getFloat(R.styleable.WeekView_xScrollingSpeed, xScrollingSpeed);
            horizontalFlingEnabled = a.getBoolean(R.styleable.WeekView_horizontalFlingEnabled, horizontalFlingEnabled);
            horizontalScrollingEnabled = a.getBoolean(R.styleable.WeekView_horizontalScrollingEnabled, horizontalScrollingEnabled);
            verticalFlingEnabled = a.getBoolean(R.styleable.WeekView_verticalFlingEnabled, verticalFlingEnabled);
            scrollDuration = a.getInt(R.styleable.WeekView_scrollDuration, scrollDuration);
        } finally {
            a.recycle();
        }
    }

    void setNumberOfVisibleDays(int numberOfVisibleDays) {
        this.numberOfVisibleDays = numberOfVisibleDays;
        drawingConfig.resetOrigin();
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

    void setEventTextColor(int eventTextColor) {
        this.eventTextColor = eventTextColor;
        drawingConfig.eventTextPaint.setColor(eventTextColor);
    }

    void setTimeColumnBackgroundColor(int timeColumnBackgroundColor) {
        this.timeColumnBackgroundColor = timeColumnBackgroundColor;
        drawingConfig.timeColumnBackgroundPaint.setColor(timeColumnBackgroundColor);
    }

    float getTotalDayWidth() {
        return drawingConfig.widthPerDay + columnGap;
    }

    float getTotalDayHeight() {
        float dayHeight = hourHeight * Constants.HOURS_PER_DAY;
        float headerHeight = drawingConfig.headerHeight;
        float totalHeaderPadding = headerRowPadding * 2;
        float headerBottomMargin = drawingConfig.headerMarginBottom;
        return dayHeight + headerHeight + totalHeaderPadding + headerBottomMargin;
    }

    boolean isSingleDay() {
        return numberOfVisibleDays == 1;
    }

}

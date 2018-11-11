package com.alamkanak.weekview.model;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.alamkanak.weekview.R;
import com.alamkanak.weekview.drawing.WeekViewDrawingConfig;

import java.util.Calendar;

import static com.alamkanak.weekview.utils.Constants.HOURS_PER_DAY;

public class WeekViewConfig {

    public WeekViewDrawingConfig drawingConfig;

    // Calendar configuration
    public int firstDayOfWeek = Calendar.MONDAY;
    public int numberOfVisibleDays = 3;
    public boolean showFirstDayOfWeekFirst = false;

    // Header bottom line
    public boolean showHeaderRowBottomLine = false;
    public int headerRowBottomLineColor = Color.rgb(102, 102, 102);
    public int headerRowBottomLineWidth = 1;

    // Time column
    public int timeColumnTextColor = Color.BLACK;
    public int timeColumnBackgroundColor = Color.WHITE;
    public int timeColumnPadding = 10;
    public int timeColumnTextSize = 12;

    // Time column separator
    public boolean showTimeColumnSeparator = false;
    public int timeColumnSeparatorColor = Color.rgb(102, 102, 102);
    public int timeColumnSeparatorStrokeWidth = 1;

    // Header row
    public int headerRowTextColor = Color.BLACK;
    public int headerRowBackgroundColor = Color.WHITE;
    public int headerRowPadding = 10;
    public int headerRowTextSize = 12;
    public int todayHeaderTextColor = Color.rgb(39, 137, 228);

    // Event chips
    public int allDayEventHeight = 100;
    public int eventCornerRadius = 0;
    public int eventTextSize = 12;
    public int eventTextColor = Color.BLACK;
    public int eventPadding = 8;

    // Event margins
    public int columnGap = 10;
    public int overlappingEventGap = 0;
    public int eventMarginVertical = 3;
    public int eventMarginHorizontal = 0;

    // Colors
    public int dayBackgroundColor = Color.rgb(255, 255, 255);
    public int todayBackgroundColor = Color.rgb(255, 255, 255);
    public boolean showDistinctWeekendColor = false;
    public boolean showDistinctPastFutureColor = false;
    public int pastBackgroundColor = Color.rgb(227, 227, 227);
    public int futureBackgroundColor = Color.rgb(245, 245, 245);
    public int pastWeekendBackgroundColor = 0;
    public int futureWeekendBackgroundColor = 0;

    // Hour height
    public int hourHeight = 50;
    public int minHourHeight = 0; // no minimum specified (will be dynamic, based on screen)
    public int maxHourHeight = 250;
    public int effectiveMinHourHeight = minHourHeight; // compensates for the fact that you can't keep zooming out.

    // Now line
    public boolean showNowLine = false;
    public int nowLineColor = Color.rgb(102, 102, 102);
    public int nowLineStrokeWidth = 5;

    // Now line dot
    public boolean showNowLineDot = false;
    public int nowLineDotColor = Color.rgb(102, 102, 102);
    public int nowLineDotRadius = 16;

    // Hour separators
    public boolean showHourSeparator = true;
    public int hourSeparatorColor = Color.rgb(230, 230, 230);
    public int hourSeparatorStrokeWidth = 2;

    // Day separators
    public boolean showDaySeparator = true;
    public int daySeparatorColor = Color.rgb(230, 230, 230);
    public int daySeparatorStrokeWidth = 2;

    // Scrolling
    public float xScrollingSpeed = 1f;
    public boolean verticalFlingEnabled = true;
    public boolean horizontalFlingEnabled = true;
    public boolean horizontalScrollingEnabled = true;
    public int scrollDuration = 250;

    public WeekViewConfig(Context context, AttributeSet attrs) {
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
            timeColumnBackgroundColor = a.getColor(R.styleable.WeekView_timeColumnBackground, timeColumnBackgroundColor);

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
            showDaySeparator = a.getBoolean(R.styleable.WeekView_showHourSeparator, showDaySeparator);
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

    public void setNumberOfVisibleDays(int numberOfVisibleDays) {
        this.numberOfVisibleDays = numberOfVisibleDays;
        drawingConfig.resetOrigin();
    }

    public void setTimeColumnTextSize(int timeColumnTextSize) {
        this.timeColumnTextSize = timeColumnTextSize;
        drawingConfig.setTextSize(timeColumnTextSize);
    }

    public void setTimeColumnTextColor(int textColor) {
        timeColumnTextColor = textColor;
        drawingConfig.setHeaderColumnTextColor(timeColumnTextColor);
    }

    public void setHeaderRowBackgroundColor(int headerRowBackgroundColor) {
        this.headerRowBackgroundColor = headerRowBackgroundColor;
        drawingConfig.headerBackgroundPaint.setColor(headerRowBackgroundColor);
    }

    public void setDayBackgroundColor(int dayBackgroundColor) {
        this.dayBackgroundColor = dayBackgroundColor;
        drawingConfig.dayBackgroundPaint.setColor(dayBackgroundColor);
    }

    public void setTodayBackgroundColor(int todayBackgroundColor) {
        this.todayBackgroundColor = todayBackgroundColor;
        drawingConfig.todayBackgroundPaint.setColor(todayBackgroundColor);
    }

    public void setHourSeparatorStrokeWidth(int hourSeparatorWidth) {
        this.hourSeparatorStrokeWidth = hourSeparatorWidth;
        drawingConfig.hourSeparatorPaint.setStrokeWidth(hourSeparatorWidth);
    }

    public void setTodayHeaderTextColor(int todayHeaderTextColor) {
        this.todayHeaderTextColor = todayHeaderTextColor;
        drawingConfig.todayHeaderTextPaint.setColor(todayHeaderTextColor);
    }

    public void setEventTextSize(int eventTextSize) {
        this.eventTextSize = eventTextSize;
        drawingConfig.eventTextPaint.setTextSize(eventTextSize);
    }

    public void setEventTextColor(int eventTextColor) {
        this.eventTextColor = eventTextColor;
        drawingConfig.eventTextPaint.setColor(eventTextColor);
    }

    public void setTimeColumnBackgroundColor(int timeColumnBackgroundColor) {
        this.timeColumnBackgroundColor = timeColumnBackgroundColor;
        drawingConfig.headerColumnBackgroundPaint.setColor(timeColumnBackgroundColor);
    }

    public float getTotalDayWidth() {
        return drawingConfig.widthPerDay + columnGap;
    }

    public float getTotalDayHeight() {
        float dayHeight = hourHeight * HOURS_PER_DAY;
        float headerHeight = drawingConfig.headerHeight;
        float totalHeaderPadding = headerRowPadding * 2;
        float headerBottomMargin = drawingConfig.headerMarginBottom;
        return dayHeight + headerHeight + totalHeaderPadding + headerBottomMargin;
    }

    public boolean isSingleDay() {
        return numberOfVisibleDays == 1;
    }

    public boolean isWeek() {
        return numberOfVisibleDays == 7;
    }

}

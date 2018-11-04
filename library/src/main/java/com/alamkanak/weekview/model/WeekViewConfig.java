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

    public boolean showFirstDayOfWeekFirst = false;

    public int hourHeight = 50;
    public int minHourHeight = 0; //no minimum specified (will be dynamic, based on screen)
    public int effectiveMinHourHeight = minHourHeight; //compensates for the fact that you can't keep zooming out.
    public int maxHourHeight = 250;
    public int columnGap = 10;
    public int firstDayOfWeek = Calendar.MONDAY;
    public int textSize = 12;
    public int headerColumnPadding = 10;
    public int headerColumnTextColor = Color.BLACK;
    public int numberOfVisibleDays = 3;
    public int headerRowPadding = 10;
    public int headerRowBackgroundColor = Color.WHITE;
    public int dayBackgroundColor = Color.rgb(255, 255, 255);
    public int pastBackgroundColor = Color.rgb(227, 227, 227);
    public int futureBackgroundColor = Color.rgb(245, 245, 245);
    public int pastWeekendBackgroundColor = 0;
    public int futureWeekendBackgroundColor = 0;
    public int nowLineColor = Color.rgb(102, 102, 102);
    public int nowLineThickness = 5;
    public int hourSeparatorColor = Color.rgb(230, 230, 230);
    public int todayBackgroundColor = Color.rgb(255, 255, 255);
    public int hourSeparatorStrokeWidth = 2;
    public int todayHeaderTextColor = Color.rgb(39, 137, 228);
    public int eventTextSize = 12;
    public int eventTextColor = Color.BLACK;
    public int eventPadding = 8;
    public int headerColumnBackgroundColor = Color.WHITE;

    public int overlappingEventGap = 0;
    public int eventMarginVertical = 3;
    public int eventMarginHorizontal = 0;
    public float xScrollingSpeed = 1f;
    public int eventCornerRadius = 0;
    public boolean showDistinctWeekendColor = false;
    public boolean showNowLine = false;
    public boolean showDistinctPastFutureColor = false;
    public boolean horizontalFlingEnabled = true;
    public boolean verticalFlingEnabled = true;
    public int allDayEventHeight = 100;
    public int scrollDuration = 250;

    public WeekViewConfig(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0);
        try {
            firstDayOfWeek = a.getInteger(R.styleable.WeekView_firstDayOfWeek, firstDayOfWeek);
            hourHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourHeight, hourHeight);
            minHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_minHourHeight, minHourHeight);
            effectiveMinHourHeight = minHourHeight;
            maxHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_maxHourHeight, maxHourHeight);
            textSize = a.getDimensionPixelSize(R.styleable.WeekView_textSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, context.getResources().getDisplayMetrics()));
            headerColumnPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerColumnPadding, headerColumnPadding);
            columnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, columnGap);
            headerColumnTextColor = a.getColor(R.styleable.WeekView_headerColumnTextColor, headerColumnTextColor);
            numberOfVisibleDays = a.getInteger(R.styleable.WeekView_noOfVisibleDays, numberOfVisibleDays);
            showFirstDayOfWeekFirst = a.getBoolean(R.styleable.WeekView_showFirstDayOfWeekFirst, showFirstDayOfWeekFirst);
            headerRowPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerRowPadding, headerRowPadding);
            headerRowBackgroundColor = a.getColor(R.styleable.WeekView_headerRowBackgroundColor, headerRowBackgroundColor);
            dayBackgroundColor = a.getColor(R.styleable.WeekView_dayBackgroundColor, dayBackgroundColor);
            futureBackgroundColor = a.getColor(R.styleable.WeekView_futureBackgroundColor, futureBackgroundColor);
            pastBackgroundColor = a.getColor(R.styleable.WeekView_pastBackgroundColor, pastBackgroundColor);
            futureWeekendBackgroundColor = a.getColor(R.styleable.WeekView_futureWeekendBackgroundColor, futureBackgroundColor); // If not set, use the same color as in the week
            pastWeekendBackgroundColor = a.getColor(R.styleable.WeekView_pastWeekendBackgroundColor, pastBackgroundColor);
            nowLineColor = a.getColor(R.styleable.WeekView_nowLineColor, nowLineColor);
            nowLineThickness = a.getDimensionPixelSize(R.styleable.WeekView_nowLineThickness, nowLineThickness);
            hourSeparatorColor = a.getColor(R.styleable.WeekView_hourSeparatorColor, hourSeparatorColor);
            todayBackgroundColor = a.getColor(R.styleable.WeekView_todayBackgroundColor, todayBackgroundColor);
            hourSeparatorStrokeWidth = a.getDimensionPixelSize(R.styleable.WeekView_hourSeparatorHeight, hourSeparatorStrokeWidth);
            todayHeaderTextColor = a.getColor(R.styleable.WeekView_todayHeaderTextColor, todayHeaderTextColor);
            eventTextSize = a.getDimensionPixelSize(R.styleable.WeekView_eventTextSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, eventTextSize, context.getResources().getDisplayMetrics()));
            eventTextColor = a.getColor(R.styleable.WeekView_eventTextColor, eventTextColor);
            eventPadding = a.getDimensionPixelSize(R.styleable.WeekView_eventPadding, eventPadding);
            headerColumnBackgroundColor = a.getColor(R.styleable.WeekView_headerColumnBackground, headerColumnBackgroundColor);
            overlappingEventGap = a.getDimensionPixelSize(R.styleable.WeekView_overlappingEventGap, overlappingEventGap);
            eventMarginVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventMarginVertical, eventMarginVertical);
            eventMarginHorizontal = a.getDimensionPixelSize(R.styleable.WeekView_singleDayHorizontalMargin, eventMarginHorizontal);
            xScrollingSpeed = a.getFloat(R.styleable.WeekView_xScrollingSpeed, xScrollingSpeed);
            eventCornerRadius = a.getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, eventCornerRadius);
            showDistinctPastFutureColor = a.getBoolean(R.styleable.WeekView_showDistinctPastFutureColor, showDistinctPastFutureColor);
            showDistinctWeekendColor = a.getBoolean(R.styleable.WeekView_showDistinctWeekendColor, showDistinctWeekendColor);
            showNowLine = a.getBoolean(R.styleable.WeekView_showNowLine, showNowLine);
            horizontalFlingEnabled = a.getBoolean(R.styleable.WeekView_horizontalFlingEnabled, horizontalFlingEnabled);
            verticalFlingEnabled = a.getBoolean(R.styleable.WeekView_verticalFlingEnabled, verticalFlingEnabled);
            allDayEventHeight = a.getDimensionPixelSize(R.styleable.WeekView_allDayEventHeight, allDayEventHeight);
            scrollDuration = a.getInt(R.styleable.WeekView_scrollDuration, scrollDuration);
        } finally {
            a.recycle();
        }
    }

    public void setNumberOfVisibleDays(int numberOfVisibleDays) {
        this.numberOfVisibleDays = numberOfVisibleDays;
        drawingConfig.resetOrigin();
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        drawingConfig.setTextSize(textSize);
    }

    public void setHeaderColumnTextColor(int textColor) {
        headerColumnTextColor = textColor;
        drawingConfig.setHeaderColumnTextColor(headerColumnTextColor);
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

    public void setHeaderColumnBackgroundColor(int headerColumnBackgroundColor) {
        this.headerColumnBackgroundColor = headerColumnBackgroundColor;
        drawingConfig.headerColumnBackgroundPaint.setColor(headerColumnBackgroundColor);
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

}

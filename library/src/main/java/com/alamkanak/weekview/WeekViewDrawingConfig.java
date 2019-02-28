package com.alamkanak.weekview;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.TextPaint;

import java.util.Calendar;

import static com.alamkanak.weekview.DateUtils.today;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;

class WeekViewDrawingConfig {

    Paint timeTextPaint;
    float timeTextWidth;
    float timeTextHeight;

    Paint headerTextPaint;
    float headerTextHeight;
    float headerHeight;
    Paint todayHeaderTextPaint;
    private int currentAllDayEventHeight;


    /**
     * dates in the past have origin.x > 0
     * dates in the future have origin.x < 0
     * relative to today()
     */
    PointF currentOrigin = new PointF(0f, 0f);
    Paint headerBackgroundPaint;
    float widthPerDay;
    Paint dayBackgroundPaint;
    Paint hourSeparatorPaint;
    Paint daySeparatorPaint;

    Paint todayBackgroundPaint;
    private Paint futureBackgroundPaint;
    private Paint pastBackgroundPaint;
    private Paint futureWeekendBackgroundPaint;
    private Paint pastWeekendBackgroundPaint;

    Paint timeColumnSeparatorPaint;

    Paint nowLinePaint;
    Paint nowDotPaint;

    float timeColumnWidth;
    TextPaint eventTextPaint;
    TextPaint allDayEventTextPaint;
    Paint timeColumnBackgroundPaint;
    boolean hasEventInHeader;

    float newHourHeight = -1;

    DateTimeInterpreter dateTimeInterpreter;

    WeekViewDrawingConfig(Context context, WeekViewConfig config) {
        // Measure settings for time column.
        timeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timeTextPaint.setTextAlign(Paint.Align.RIGHT);
        timeTextPaint.setTextSize(config.timeColumnTextSize);
        timeTextPaint.setColor(config.timeColumnTextColor);

        final Rect rect = new Rect();
        timeTextPaint.getTextBounds("00 PM", 0, "00 PM".length(), rect);
        timeTextHeight = rect.height();
        initTextTimeWidth(context, config);

        // Measure settings for header row.
        headerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerTextPaint.setColor(config.headerRowTextColor);
        headerTextPaint.setTextAlign(Paint.Align.CENTER);
        headerTextPaint.setTextSize(config.headerRowTextSize);
        headerTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        headerTextHeight = headerTextPaint.descent() - headerTextPaint.ascent();

        // Prepare header background paint.
        headerBackgroundPaint = new Paint();
        headerBackgroundPaint.setColor(config.headerRowBackgroundColor);

        // Prepare day background color paint.
        dayBackgroundPaint = new Paint();
        dayBackgroundPaint.setColor(config.dayBackgroundColor);
        futureBackgroundPaint = new Paint();
        futureBackgroundPaint.setColor(config.futureBackgroundColor);
        pastBackgroundPaint = new Paint();
        pastBackgroundPaint.setColor(config.pastBackgroundColor);
        futureWeekendBackgroundPaint = new Paint();
        futureWeekendBackgroundPaint.setColor(config.futureWeekendBackgroundColor);
        pastWeekendBackgroundPaint = new Paint();
        pastWeekendBackgroundPaint.setColor(config.pastWeekendBackgroundColor);

        // Prepare time column separator.
        timeColumnSeparatorPaint = new Paint();
        timeColumnSeparatorPaint.setColor(config.timeColumnSeparatorColor);
        timeColumnSeparatorPaint.setStrokeWidth(config.timeColumnSeparatorStrokeWidth);

        // Prepare hour separator color paint.
        hourSeparatorPaint = new Paint();
        hourSeparatorPaint.setStyle(Paint.Style.STROKE);
        hourSeparatorPaint.setStrokeWidth(config.hourSeparatorStrokeWidth);
        hourSeparatorPaint.setColor(config.hourSeparatorColor);

        // Prepare day separator color paint.
        daySeparatorPaint = new Paint();
        daySeparatorPaint.setStyle(Paint.Style.STROKE);
        daySeparatorPaint.setStrokeWidth(config.daySeparatorStrokeWidth);
        daySeparatorPaint.setColor(config.daySeparatorColor);

        // Prepare the "now" line color paint
        nowLinePaint = new Paint();
        nowLinePaint.setStrokeWidth(config.nowLineStrokeWidth);
        nowLinePaint.setColor(config.nowLineColor);

        // Prepare the "now" dot paint
        nowDotPaint = new Paint();
        nowDotPaint.setStyle(Paint.Style.FILL);
        nowDotPaint.setStrokeWidth(config.nowLineDotRadius);
        nowDotPaint.setColor(config.nowLineDotColor);

        // Prepare today background color paint.
        todayBackgroundPaint = new Paint();
        todayBackgroundPaint.setColor(config.todayBackgroundColor);

        // Prepare today header text color paint.
        todayHeaderTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        todayHeaderTextPaint.setTextAlign(Paint.Align.CENTER);
        todayHeaderTextPaint.setTextSize(config.headerRowTextSize);
        todayHeaderTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        todayHeaderTextPaint.setColor(config.todayHeaderTextColor);

        // Prepare header column background color.
        timeColumnBackgroundPaint = new Paint();
        timeColumnBackgroundPaint.setColor(config.timeColumnBackgroundColor);

        // Prepare event text size and color.
        eventTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        eventTextPaint.setStyle(Paint.Style.FILL);
        eventTextPaint.setColor(config.eventTextColor);
        eventTextPaint.setTextSize(config.eventTextSize);

        // Prepare event text size and color.
        allDayEventTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        allDayEventTextPaint.setStyle(Paint.Style.FILL);
        allDayEventTextPaint.setColor(config.eventTextColor);
        allDayEventTextPaint.setTextSize(config.allDayEventTextSize);

        currentAllDayEventHeight = 0;
        hasEventInHeader = false;
        refreshHeaderHeight(config);
    }

  void refreshHeaderHeight(WeekViewConfig config) {
      headerHeight = config.headerRowPadding * 2 + headerTextHeight;
      if (config.showHeaderRowBottomLine) {
          headerHeight += config.headerRowBottomLineWidth;
      }
      if (hasEventInHeader) {
          headerHeight += currentAllDayEventHeight;
      }

      if (config.showCompleteDay) {
          config.hourHeight = (WeekView.getViewHeight() - headerHeight) / config.getHoursPerDay();
          newHourHeight = config.hourHeight;
      }
  }

  void setCurrentAllDayEventHeight(int height, WeekViewConfig config) {
      currentAllDayEventHeight = height;
      refreshHeaderHeight(config);
  }

    int getCurrentAllDayEventHeight() {
        return currentAllDayEventHeight;
    }

    void moveCurrentOriginIfFirstDraw(WeekViewConfig config) {
        // If the week view is being drawn for the first time, then consider the first day of the week.
        final Calendar today = today();
        final boolean isWeekView = config.numberOfVisibleDays >= 7;
        final boolean currentDayIsNotToday = today.get(DAY_OF_WEEK) != config.firstDayOfWeek;

        if (isWeekView && currentDayIsNotToday && config.showFirstDayOfWeekFirst) {
            int difference = computeDifferenceWithFirstDayOfWeek(config, today);
            currentOrigin.x += (widthPerDay + config.columnGap) * difference;
        }

        if (config.showCurrentTimeFirst) {
            computeDifferenceWithCurrentTime(config);
        }

        // Overwrites the origin when today is out of date range
        float minX = config.getMinX();
        float maxX = config.getMaxX();
        currentOrigin.x = Math.min(currentOrigin.x,maxX);
        currentOrigin.x = Math.max(currentOrigin.x,minX);
    }

    private void computeDifferenceWithCurrentTime(WeekViewConfig config) {
        final Calendar desired = Calendar.getInstance();

        if (desired.get(HOUR_OF_DAY) > 0) {
            // Add some padding above the current time (and thus: the now line)
            desired.add(HOUR_OF_DAY, -1);
        }

        final int hour = desired.get(HOUR_OF_DAY);
        final int minutes = desired.get(MINUTE);
        final float fraction = (float) minutes / Constants.MINUTES_PER_HOUR;

        float verticalOffset = config.hourHeight * (hour + fraction);

        final float dayHeight = config.getTotalDayHeight();
        final double viewHeight = WeekView.getViewHeight();

        final double desiredOffset = dayHeight - viewHeight;
        verticalOffset = min((float) desiredOffset, verticalOffset);

        config.drawingConfig.currentOrigin.y = verticalOffset * (-1);
    }

    int computeDifferenceWithFirstDayOfWeek(@NonNull WeekViewConfig config ,@NonNull Calendar date) {
        return (date.get(DAY_OF_WEEK) + 7 - config.firstDayOfWeek) % 7;
    }

    void refreshAfterZooming(WeekViewConfig config) {
        if (newHourHeight > 0 && !config.showCompleteDay) {
            newHourHeight = Math.max(newHourHeight, config.effectiveMinHourHeight);
            newHourHeight = Math.min(newHourHeight, config.maxHourHeight);

            // potentialMinHourHeight
            // the minimal height of an hour when zoomed completely out
            // needed to suppress the zooming below 24:00
            final int height = WeekView.getViewHeight();
            float potentialMinHourHeight = (height - headerHeight) / config.getHoursPerDay();
            newHourHeight = Math.max(newHourHeight, potentialMinHourHeight);

            currentOrigin.y = (currentOrigin.y / config.hourHeight) * newHourHeight;
            config.hourHeight = newHourHeight;
            newHourHeight = -1;
        }
    }

    void updateVerticalOrigin(WeekViewConfig config) {
        final int height = WeekView.getViewHeight();

        // If the new currentOrigin.y is invalid, make it valid.
        final float dayHeight = config.hourHeight * config.getHoursPerDay();

        final float potentialNewVerticalOrigin = height - (dayHeight + headerHeight);

        currentOrigin.y = max(currentOrigin.y, potentialNewVerticalOrigin);
        currentOrigin.y = min(currentOrigin.y, 0);
    }

    float getHeaderBottomPosition(WeekViewConfig config) {
        return currentOrigin.y + getTotalHeaderHeight(config);
    }

    float getTotalHeaderHeight(WeekViewConfig config) {
        return headerHeight + (config.headerRowPadding * 2f);
    }

    float getTotalTimeColumnWidth(WeekViewConfig config) {
        return timeTextWidth + config.timeColumnPadding * 2;
    }

    void resetOrigin() {
        currentOrigin = new PointF(0, 0);
    }

    void setTextSize(int textSize) {
        todayHeaderTextPaint.setTextSize(textSize);
        headerTextPaint.setTextSize(textSize);
        timeTextPaint.setTextSize(textSize);
    }

    void setHeaderRowTextColor(int headerRowTextColor) {
        headerTextPaint.setColor(headerRowTextColor);
    }

    void setTimeColumnTextColor(int timeColumnTextColor) {
        timeTextPaint.setColor(timeColumnTextColor);
    }

    void setDateTimeInterpreter(DateTimeInterpreter dateTimeInterpreter,
                                Context context, WeekViewConfig config) {
        this.dateTimeInterpreter = dateTimeInterpreter;
        initTextTimeWidth(context, config);
    }

    Paint getPastBackgroundPaint(boolean useWeekendColor) {
        return useWeekendColor ? pastWeekendBackgroundPaint : pastBackgroundPaint;
    }

    Paint getFutureBackgroundPaint(boolean useWeekendColor) {
        return useWeekendColor ? futureWeekendBackgroundPaint : futureBackgroundPaint;
    }

    Paint getTodayBackgroundPaint(boolean isToday) {
        return isToday ? todayBackgroundPaint : dayBackgroundPaint;
    }

    /**
     * Initialize time column width. Calculate value with all possible hours (supposed widest text).
     */
    private void initTextTimeWidth(Context context, WeekViewConfig config) {
        final DateTimeInterpreter interpreter = getDateTimeInterpreter(context, config);
        timeTextWidth = 0;

        for (int i = 0; i < HOUR_OF_DAY; i++) {
            final String time = interpreter.interpretTime(i);
            timeTextWidth = max(timeTextWidth, timeTextPaint.measureText(time));
        }
    }

    DateTimeInterpreter getDateTimeInterpreter(Context context, WeekViewConfig config) {
        if (dateTimeInterpreter == null) {
            dateTimeInterpreter = new DefaultDateTimeInterpreter(context, config.numberOfVisibleDays);
        }

        return dateTimeInterpreter;
    }

}

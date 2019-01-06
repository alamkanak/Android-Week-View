package com.alamkanak.weekview;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.TextPaint;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.alamkanak.weekview.DateUtils.today;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MONDAY;
import static java.util.Calendar.SUNDAY;

class WeekViewDrawingConfig {

    Paint timeTextPaint;
    float timeTextWidth;
    float timeTextHeight;

    Paint headerTextPaint;
    float headerTextHeight;
    float headerHeight;
    Paint todayHeaderTextPaint;

    PointF currentOrigin = new PointF(0f, 0f);
    Paint headerBackgroundPaint;
    float widthPerDay;
    Paint dayBackgroundPaint;
    Paint hourSeparatorPaint;
    Paint daySeparatorPaint;
    float headerMarginBottom;

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
    Paint timeColumnBackgroundPaint;

    int newHourHeight = -1;

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
        initTextTimeWidth(context);

        // Measure settings for header row.
        headerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerTextPaint.setColor(config.headerRowTextColor);
        headerTextPaint.setTextAlign(Paint.Align.CENTER);
        headerTextPaint.setTextSize(config.headerRowTextSize);
        headerTextPaint.getTextBounds("00 PM", 0, "00 PM".length(), rect);
        headerTextHeight = rect.height();
        headerTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

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

        // Prepare event background color.
        //eventBackgroundPaint = new Paint();
        //eventBackgroundPaint.setColor(Color.rgb(174, 208, 238));

        // Prepare header column background color.
        timeColumnBackgroundPaint = new Paint();
        timeColumnBackgroundPaint.setColor(config.timeColumnBackgroundColor);

        // Prepare event text size and color.
        eventTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        eventTextPaint.setStyle(Paint.Style.FILL);
        eventTextPaint.setColor(config.eventTextColor);
        eventTextPaint.setTextSize(config.eventTextSize);
    }

    void moveCurrentOriginIfFirstDraw(WeekViewConfig config) {
        // If the week view is being drawn for the first time, then consider the first day of the week.
        final Calendar today = today();
        final boolean isWeekView = config.numberOfVisibleDays >= 7;
        final boolean currentDayIsNotToday = today.get(DAY_OF_WEEK) != config.firstDayOfWeek;

        if (isWeekView && currentDayIsNotToday && config.showFirstDayOfWeekFirst) {
            int currentDay = today.get(DAY_OF_WEEK);
            int difference;

            if (config.firstDayOfWeek == MONDAY && currentDay == SUNDAY) {
                // Special case, because Sunday (1) has a lower index than Monday (2)
                difference = 6;
            } else {
                difference = today.get(DAY_OF_WEEK) - config.firstDayOfWeek;
            }

            currentOrigin.x += (widthPerDay + config.columnGap) * difference;
        }
    }

    void refreshAfterZooming(WeekViewConfig config) {
        if (newHourHeight > 0) {
            if (newHourHeight < config.effectiveMinHourHeight) {
                newHourHeight = config.effectiveMinHourHeight;
            } else if (newHourHeight > config.maxHourHeight) {
                newHourHeight = config.maxHourHeight;
            }

            currentOrigin.y = (currentOrigin.y / config.hourHeight) * newHourHeight;
            config.hourHeight = newHourHeight;
            newHourHeight = -1;
        }
    }

    void updateVerticalOrigin(WeekViewConfig config) {
        final int height = WeekView.getViewHeight();

        // If the new currentOrigin.y is invalid, make it valid.
        final float dayHeight = config.hourHeight * 24;
        final float headerHeight = this.headerHeight + config.headerRowPadding * 2 + headerMarginBottom;

        final float potentialNewVerticalOrigin = height - (dayHeight + headerHeight);

        currentOrigin.y = max(currentOrigin.y, potentialNewVerticalOrigin);
        currentOrigin.y = min(currentOrigin.y, 0);
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

    void setDateTimeInterpreter(DateTimeInterpreter dateTimeInterpreter, Context context) {
        this.dateTimeInterpreter = dateTimeInterpreter;
        initTextTimeWidth(context);
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
    private void initTextTimeWidth(Context context) {
        final DateTimeInterpreter interpreter = getDateTimeInterpreter(context);
        timeTextWidth = 0;

        for (int i = 0; i < HOUR_OF_DAY; i++) {
            final String time = interpreter.interpretTime(i);
            timeTextWidth = max(timeTextWidth, timeTextPaint.measureText(time));
        }
    }

    DateTimeInterpreter getDateTimeInterpreter(Context context) {
        if (dateTimeInterpreter == null) {
            dateTimeInterpreter = buildDefaultDateTimeInterpreter(context);
        }

        return dateTimeInterpreter;
    }

    private DateTimeInterpreter buildDefaultDateTimeInterpreter(final Context context) {
        return new DateTimeInterpreter() {

            private SimpleDateFormat sdfDate = DateUtils.getDateFormat();
            private SimpleDateFormat sdfTime = DateUtils.getTimeFormat(context);
            private Calendar calendar = Calendar.getInstance();

            @NonNull
            @Override
            public String interpretDate(@NonNull Calendar date) {
                try {
                    return sdfDate.format(date.getTime()).toUpperCase();
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            }

            @NonNull
            @Override
            public String interpretTime(int hour) {
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, 0);

                try {
                    return sdfTime.format(calendar.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            }
        };
    }

}

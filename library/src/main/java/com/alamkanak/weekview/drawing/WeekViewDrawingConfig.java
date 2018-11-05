package com.alamkanak.weekview.drawing;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;

import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.ui.WeekView;
import com.alamkanak.weekview.utils.DateTimeInterpreter;
import com.alamkanak.weekview.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.alamkanak.weekview.utils.DateUtils.today;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.HOUR_OF_DAY;

public class WeekViewDrawingConfig {
    
    Paint timeTextPaint;
    float timeTextWidth;
    public float timeTextHeight;

    Paint headerTextPaint;
    float headerTextHeight;
    public float headerHeight;

    public PointF currentOrigin = new PointF(0f, 0f);
    public Paint headerBackgroundPaint;
    public float widthPerDay;
    public Paint dayBackgroundPaint;
    public Paint hourSeparatorPaint;
    public float headerMarginBottom;

    public Paint todayBackgroundPaint;
    private Paint futureBackgroundPaint;
    private Paint pastBackgroundPaint;
    private Paint futureWeekendBackgroundPaint;
    private Paint pastWeekendBackgroundPaint;

    Paint nowLinePaint;
    Paint nowDotPaint;
    public Paint todayHeaderTextPaint;
    public float headerColumnWidth;
    public TextPaint eventTextPaint;
    public Paint headerColumnBackgroundPaint;
    public int defaultEventColor;

    public int newHourHeight = -1;

    DateTimeInterpreter dateTimeInterpreter;
    
    public WeekViewDrawingConfig(Context context, WeekViewConfig config) {
        // Measure settings for time column.
        timeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timeTextPaint.setTextAlign(Paint.Align.RIGHT);
        timeTextPaint.setTextSize(config.textSize);
        timeTextPaint.setColor(config.headerColumnTextColor);

        Rect rect = new Rect();
        timeTextPaint.getTextBounds("00 PM", 0, "00 PM".length(), rect);
        timeTextHeight = rect.height();
        headerMarginBottom = 0; // TODO timeTextHeight / 2;
        // TODO: Now padding in header row is missing
        initTextTimeWidth(context);

        // Measure settings for header row.
        headerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerTextPaint.setColor(config.headerColumnTextColor);
        headerTextPaint.setTextAlign(Paint.Align.CENTER);
        headerTextPaint.setTextSize(config.textSize);
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

        // Prepare hour separator color paint.
        hourSeparatorPaint = new Paint();
        hourSeparatorPaint.setStyle(Paint.Style.STROKE);
        hourSeparatorPaint.setStrokeWidth(config.hourSeparatorStrokeWidth);
        hourSeparatorPaint.setColor(config.hourSeparatorColor);

        // Prepare the "now" line color paint
        nowLinePaint = new Paint();
        nowLinePaint.setStrokeWidth(config.nowLineThickness);
        nowLinePaint.setColor(config.nowLineColor);

        // Prepare the "now" dot paint
        nowDotPaint = new Paint();
        nowDotPaint.setStyle(Paint.Style.FILL);
        nowDotPaint.setStrokeWidth(config.nowLineThickness * 5);
        nowDotPaint.setColor(config.nowLineColor);

        // Prepare today background color paint.
        todayBackgroundPaint = new Paint();
        todayBackgroundPaint.setColor(config.todayBackgroundColor);

        // Prepare today header text color paint.
        todayHeaderTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        todayHeaderTextPaint.setTextAlign(Paint.Align.CENTER);
        todayHeaderTextPaint.setTextSize(config.textSize);
        todayHeaderTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        todayHeaderTextPaint.setColor(config.todayHeaderTextColor);

        // Prepare event background color.
        //eventBackgroundPaint = new Paint();
        //eventBackgroundPaint.setColor(Color.rgb(174, 208, 238));

        // Prepare header column background color.
        headerColumnBackgroundPaint = new Paint();
        headerColumnBackgroundPaint.setColor(config.headerColumnBackgroundColor);

        // Prepare event text size and color.
        eventTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        eventTextPaint.setStyle(Paint.Style.FILL);
        eventTextPaint.setColor(config.eventTextColor);
        eventTextPaint.setTextSize(config.eventTextSize);

        // Set default event color.
        defaultEventColor = Color.parseColor("#9fc6e7");
    }

    public void moveCurrentOriginIfFirstDraw(WeekViewConfig config) {
        // If the week view is being drawn for the first time, then consider the first day of the week.
        Calendar today = today();
        boolean isWeekView = config.numberOfVisibleDays >= 7;
        boolean currentDayIsNotToday = today.get(DAY_OF_WEEK) != config.firstDayOfWeek;
        if (isWeekView && currentDayIsNotToday && config.showFirstDayOfWeekFirst) {
            int difference = today.get(DAY_OF_WEEK) - config.firstDayOfWeek;
            currentOrigin.x += (widthPerDay + config.columnGap) * difference;
        }
    }

    public void refreshAfterZooming(WeekViewConfig config) {
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

    public void updateVerticalOrigin(WeekViewConfig config) {
        final int height = WeekView.getViewHeight();

        // If the new currentOrigin.y is invalid, make it valid.
        final float dayHeight = config.hourHeight * 24;
        final float headerHeight = this.headerHeight + config.headerRowPadding * 2 + headerMarginBottom;
        final float halfTextHeight = timeTextHeight / 2;

        final float potentialNewVerticalOrigin = height - (dayHeight + headerHeight + halfTextHeight);

        currentOrigin.y = max(currentOrigin.y, potentialNewVerticalOrigin);
        currentOrigin.y = min(currentOrigin.y, 0);
    }

    public void resetOrigin() {
        currentOrigin = new PointF(0, 0);
    }

    public void setTextSize(int textSize) {
        todayHeaderTextPaint.setTextSize(textSize);
        headerTextPaint.setTextSize(textSize);
        timeTextPaint.setTextSize(textSize);
    }

    public void setHeaderColumnTextColor(int headerColumnTextColor) {
        headerTextPaint.setColor(headerColumnTextColor);
        timeTextPaint.setColor(headerColumnTextColor);
    }

    public void setDateTimeInterpreter(DateTimeInterpreter dateTimeInterpreter, Context context) {
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
        DateTimeInterpreter interpreter = getDateTimeInterpreter(context);
        timeTextWidth = 0;
        for (int i = 0; i < HOUR_OF_DAY; i++) {
            String time = interpreter.interpretTime(i);
            if (time == null) {
                throw new IllegalStateException("A DateTimeInterpreter must not return null time");
            }
            timeTextWidth = Math.max(timeTextWidth, timeTextPaint.measureText(time));
        }
    }

    public DateTimeInterpreter getDateTimeInterpreter(Context context) {
        if (dateTimeInterpreter == null) {
            dateTimeInterpreter = buildDateTimeInterpreter(context);
        }

        return dateTimeInterpreter;
    }

    private DateTimeInterpreter buildDateTimeInterpreter(final Context context) {
        return new DateTimeInterpreter() {
            private SimpleDateFormat sdfDate = DateUtils.getDateFormat();
            private SimpleDateFormat sdfTime = DateUtils.getTimeFormat(context);
            private Calendar calendar = Calendar.getInstance();

            @Override
            public String interpretDate(Calendar date) {
                try {
                    return sdfDate.format(date.getTime()).toUpperCase();
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            }

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

package com.alamkanak.weekview.drawing;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.format.DateFormat;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.model.WeekViewConfig;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WeekViewDrawingConfig {
    
    public Paint mTimeTextPaint;
    public float mTimeTextWidth;
    public float mTimeTextHeight;
    public Paint mHeaderTextPaint;
    public float mHeaderTextHeight;
    public float mHeaderHeight;
    public PointF mCurrentOrigin = new PointF(0f, 0f);
    public Paint mHeaderBackgroundPaint;
    public float mWidthPerDay;
    public Paint mDayBackgroundPaint;
    public Paint mHourSeparatorPaint;
    public float mHeaderMarginBottom;
    public Paint mTodayBackgroundPaint;
    public Paint mFutureBackgroundPaint;
    public Paint mPastBackgroundPaint;
    public Paint mFutureWeekendBackgroundPaint;
    public Paint mPastWeekendBackgroundPaint;
    public Paint mNowLinePaint;
    public Paint mTodayHeaderTextPaint;
    public Paint mEventBackgroundPaint;
    public float mHeaderColumnWidth;
    public TextPaint mEventTextPaint;
    public Paint mHeaderColumnBackgroundPaint;
    public int mDefaultEventColor;

    public int mNewHourHeight = -1;

    public DateTimeInterpreter mDateTimeInterpreter;
    
    public WeekViewDrawingConfig(Context context, WeekViewConfig config) {
        // Measure settings for time column.
        mTimeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTimeTextPaint.setTextAlign(Paint.Align.RIGHT);
        mTimeTextPaint.setTextSize(config.mTextSize);
        mTimeTextPaint.setColor(config.mHeaderColumnTextColor);
        Rect rect = new Rect();
        mTimeTextPaint.getTextBounds("00 PM", 0, "00 PM".length(), rect);
        mTimeTextHeight = rect.height();
        mHeaderMarginBottom = mTimeTextHeight / 2;
        initTextTimeWidth(context);

        // Measure settings for header row.
        mHeaderTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHeaderTextPaint.setColor(config.mHeaderColumnTextColor);
        mHeaderTextPaint.setTextAlign(Paint.Align.CENTER);
        mHeaderTextPaint.setTextSize(config.mTextSize);
        mHeaderTextPaint.getTextBounds("00 PM", 0, "00 PM".length(), rect);
        mHeaderTextHeight = rect.height();
        mHeaderTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        // Prepare header background paint.
        mHeaderBackgroundPaint = new Paint();
        mHeaderBackgroundPaint.setColor(config.mHeaderRowBackgroundColor);

        // Prepare day background color paint.
        mDayBackgroundPaint = new Paint();
        mDayBackgroundPaint.setColor(config.mDayBackgroundColor);
        mFutureBackgroundPaint = new Paint();
        mFutureBackgroundPaint.setColor(config.mFutureBackgroundColor);
        mPastBackgroundPaint = new Paint();
        mPastBackgroundPaint.setColor(config.mPastBackgroundColor);
        mFutureWeekendBackgroundPaint = new Paint();
        mFutureWeekendBackgroundPaint.setColor(config.mFutureWeekendBackgroundColor);
        mPastWeekendBackgroundPaint = new Paint();
        mPastWeekendBackgroundPaint.setColor(config.mPastWeekendBackgroundColor);

        // Prepare hour separator color paint.
        mHourSeparatorPaint = new Paint();
        mHourSeparatorPaint.setStyle(Paint.Style.STROKE);
        mHourSeparatorPaint.setStrokeWidth(config.mHourSeparatorStrokeWidth);
        mHourSeparatorPaint.setColor(config.mHourSeparatorColor);

        // Prepare the "now" line color paint
        mNowLinePaint = new Paint();
        mNowLinePaint.setStrokeWidth(config.mNowLineThickness);
        mNowLinePaint.setColor(config.mNowLineColor);

        // Prepare today background color paint.
        mTodayBackgroundPaint = new Paint();
        mTodayBackgroundPaint.setColor(config.mTodayBackgroundColor);

        // Prepare today header text color paint.
        mTodayHeaderTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTodayHeaderTextPaint.setTextAlign(Paint.Align.CENTER);
        mTodayHeaderTextPaint.setTextSize(config.mTextSize);
        mTodayHeaderTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTodayHeaderTextPaint.setColor(config.mTodayHeaderTextColor);

        // Prepare event background color.
        mEventBackgroundPaint = new Paint();
        mEventBackgroundPaint.setColor(Color.rgb(174, 208, 238));

        // Prepare header column background color.
        mHeaderColumnBackgroundPaint = new Paint();
        mHeaderColumnBackgroundPaint.setColor(config.mHeaderColumnBackgroundColor);

        // Prepare event text size and color.
        mEventTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        mEventTextPaint.setStyle(Paint.Style.FILL);
        mEventTextPaint.setColor(config.mEventTextColor);
        mEventTextPaint.setTextSize(config.mEventTextSize);

        // Set default event color.
        mDefaultEventColor = Color.parseColor("#9fc6e7");
    }

    /**
     * Initialize time column width. Calculate value with all possible hours (supposed widest text).
     */
    public void initTextTimeWidth(Context context) {
        mTimeTextWidth = 0;
        for (int i = 0; i < 24; i++) {
            // Measure time string and get max width.
            String time = getDateTimeInterpreter(context).interpretTime(i);
            if (time == null)
                throw new IllegalStateException("A DateTimeInterpreter must not return null time");
            mTimeTextWidth = Math.max(mTimeTextWidth, mTimeTextPaint.measureText(time));
        }
    }

    public DateTimeInterpreter getDateTimeInterpreter(final Context context) {
        if (mDateTimeInterpreter == null) {
            mDateTimeInterpreter = new DateTimeInterpreter() {
                @Override
                public String interpretDate(Calendar date) {
                    try {
                        SimpleDateFormat sdf = true // TODO
                                ? new SimpleDateFormat("EEEEE M/dd", Locale.getDefault())
                                : new SimpleDateFormat("EEE M/dd", Locale.getDefault());
                        return sdf.format(date.getTime()).toUpperCase();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                    }
                }

                @Override
                public String interpretTime(int hour) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, 0);

                    try {
                        SimpleDateFormat sdf = DateFormat.is24HourFormat(context)
                                ? new SimpleDateFormat("HH:mm", Locale.getDefault())
                                : new SimpleDateFormat("hh a", Locale.getDefault());
                        return sdf.format(calendar.getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                    }
                }
            };
        }
        return mDateTimeInterpreter;
    }
    
}

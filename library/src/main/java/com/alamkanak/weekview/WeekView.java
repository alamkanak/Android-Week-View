package com.alamkanak.weekview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.OverScroller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://alamkanak.github.io/
 */
public class WeekView extends View {

    @Deprecated
    public static final int LENGTH_SHORT = 1;
    @Deprecated
    public static final int LENGTH_LONG = 2;
    private final Context mContext;
    private Paint mTimeTextPaint;
    private float mTimeTextWidth;
    private float mTimeTextHeight;
    private Paint mHeaderTextPaint;
    private float mHeaderTextHeight;
    private GestureDetectorCompat mGestureDetector;
    private OverScroller mScroller;
    private PointF mCurrentOrigin = new PointF(0f, 0f);
    private Direction mCurrentScrollDirection = Direction.NONE;
    private Paint mHeaderBackgroundPaint;
    private float mWidthPerDay;
    private Paint mDayBackgroundPaint;
    private Paint mHourSeparatorPaint;
    private float mHeaderMarginBottom;
    private Paint mTodayBackgroundPaint;
    private Paint mFutureBackgroundPaint;
    private Paint mPastBackgroundPaint;
    private Paint mFutureWeekendBackgroundPaint;
    private Paint mPastWeekendBackgroundPaint;
    private Paint mNowLinePaint;
    private Paint mTodayHeaderTextPaint;
    private Paint mEventBackgroundPaint;
    private float mHeaderColumnWidth;
    private List<EventRect> mEventRects;
    private List<WeekViewEvent> mPreviousPeriodEvents;
    private List<WeekViewEvent> mCurrentPeriodEvents;
    private List<WeekViewEvent> mNextPeriodEvents;
    private TextPaint mEventTextPaint;
    private Paint mHeaderColumnBackgroundPaint;
    private int mFetchedPeriod = -1; // the middle period the calendar has fetched.
    private boolean mRefreshEvents = false;
    private Direction mCurrentFlingDirection = Direction.NONE;

    // Attributes and their default values.
    private int mHourHeight = 50;
    private int mNewHourHeight = -1;
    private int mMinHourHeight = 0; //no minimum specified (will be dynamic, based on screen)
    private int mEffectiveMinHourHeight = mMinHourHeight; //compensates for the fact that you can't keep zooming out.
    private int mMaxHourHeight = 250;
    private int mColumnGap = 10;
    private int mFirstDayOfWeek = Calendar.MONDAY;
    private int mTextSize = 12;
    private int mHeaderColumnPadding = 10;
    private int mHeaderColumnTextColor = Color.BLACK;
    private int mNumberOfVisibleDays = 3;
    private int mHeaderRowPadding = 10;
    private int mHeaderRowBackgroundColor = Color.WHITE;
    private int mDayBackgroundColor = Color.rgb(245, 245, 245);
    private int mPastBackgroundColor = Color.rgb(227, 227, 227);
    private int mFutureBackgroundColor = Color.rgb(245, 245, 245);
    private int mPastWeekendBackgroundColor = 0;
    private int mFutureWeekendBackgroundColor = 0;
    private int mNowLineColor = Color.rgb(102, 102, 102);
    private int mNowLineThickness = 5;
    private boolean mUseNewColoring = false;
    private int mHourSeparatorColor = Color.rgb(230, 230, 230);
    private int mTodayBackgroundColor = Color.rgb(239, 247, 254);
    private int mHourSeparatorHeight = 2;
    private int mTodayHeaderTextColor = Color.rgb(39, 137, 228);
    private int mEventTextSize = 12;
    private int mEventTextColor = Color.BLACK;
    private int mEventPadding = 8;
    private int mHeaderColumnBackgroundColor = Color.WHITE;
    private int mDefaultEventColor;
    private boolean mIsFirstDraw = true;
    private boolean mAreDimensionsInvalid = true;
    @Deprecated private int mDayNameLength = LENGTH_LONG;
    private int mOverlappingEventGap = 0;
    private int mEventMarginVertical = 0;
    private float mXScrollingSpeed = 1f;
    private Calendar mFirstVisibleDay;
    private Calendar mLastVisibleDay;
    private Calendar mScrollToDay = null;
    private double mScrollToHour = -1;
    private ScaleGestureDetector mScaleDetector;
    private boolean mIsZooming;
    private int mEventCornerRadius = 0;

    // Listeners.
    private EventClickListener mEventClickListener;
    private EventLongPressListener mEventLongPressListener;
    private WeekViewLoader mWeekViewLoader;
    private EmptyViewClickListener mEmptyViewClickListener;
    private EmptyViewLongPressListener mEmptyViewLongPressListener;
    private DateTimeInterpreter mDateTimeInterpreter;
    private ScrollListener mScrollListener;

    private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            goToNearestOrigin();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(mIsZooming)
                return true;
            if (mCurrentScrollDirection == Direction.NONE) {
                // allow scrolling only in one direction
                if (Math.abs(distanceX) > Math.abs(distanceY)){
                    mCurrentScrollDirection = Direction.HORIZONTAL;
                }
                else {
                    mCurrentScrollDirection = Direction.VERTICAL;
                }
            }

            switch (mCurrentScrollDirection) {
                case HORIZONTAL:
                    mCurrentOrigin.x -= distanceX * mXScrollingSpeed;
                    ViewCompat.postInvalidateOnAnimation(WeekView.this);

                    break;
                case VERTICAL:
                    mCurrentOrigin.y -= distanceY;
                    ViewCompat.postInvalidateOnAnimation(WeekView.this);

                    break;
            }

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mScroller.forceFinished(true);

            mCurrentFlingDirection = mCurrentScrollDirection;
            if (mCurrentFlingDirection == Direction.HORIZONTAL){
                mScroller.fling((int) mCurrentOrigin.x, (int) mCurrentOrigin.y, (int) (velocityX * mXScrollingSpeed), 0, Integer.MIN_VALUE, Integer.MAX_VALUE, (int) -(mHourHeight * 24 + mHeaderTextHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom + mTimeTextHeight/2 - getHeight()), 0);
            }
            else if (mCurrentFlingDirection == Direction.VERTICAL){
                mScroller.fling((int) mCurrentOrigin.x, (int) mCurrentOrigin.y, 0, (int) velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, (int) -(mHourHeight * 24 + mHeaderTextHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom + mTimeTextHeight/2 - getHeight()), 0);
            }

            ViewCompat.postInvalidateOnAnimation(WeekView.this);
            return true;
        }


        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // If the tap was on an event then trigger the callback.
            if (mEventRects != null && mEventClickListener != null) {
                List<EventRect> reversedEventRects = mEventRects;
                Collections.reverse(reversedEventRects);
                for (EventRect event : reversedEventRects) {
                    if (event.rectF != null && e.getX() > event.rectF.left && e.getX() < event.rectF.right && e.getY() > event.rectF.top && e.getY() < event.rectF.bottom) {
                        mEventClickListener.onEventClick(event.originalEvent, event.rectF);
                        playSoundEffect(SoundEffectConstants.CLICK);
                        return super.onSingleTapConfirmed(e);
                    }
                }
            }

            // If the tap was on in an empty space, then trigger the callback.
            if (mEmptyViewClickListener != null && e.getX() > mHeaderColumnWidth && e.getY() > (mHeaderTextHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom)) {
                Calendar selectedTime = getTimeFromPoint(e.getX(), e.getY());
                if (selectedTime != null) {
                    playSoundEffect(SoundEffectConstants.CLICK);
                    mEmptyViewClickListener.onEmptyViewClicked(selectedTime);
                }
            }

            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);

            if (mEventLongPressListener != null && mEventRects != null) {
                List<EventRect> reversedEventRects = mEventRects;
                Collections.reverse(reversedEventRects);
                for (EventRect event : reversedEventRects) {
                    if (event.rectF != null && e.getX() > event.rectF.left && e.getX() < event.rectF.right && e.getY() > event.rectF.top && e.getY() < event.rectF.bottom) {
                        mEventLongPressListener.onEventLongPress(event.originalEvent, event.rectF);
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        return;
                    }
                }
            }

            // If the tap was on in an empty space, then trigger the callback.
            if (mEmptyViewLongPressListener != null && e.getX() > mHeaderColumnWidth && e.getY() > (mHeaderTextHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom)) {
                Calendar selectedTime = getTimeFromPoint(e.getX(), e.getY());
                if (selectedTime != null) {
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    mEmptyViewLongPressListener.onEmptyViewLongPress(selectedTime);
                }
            }
        }
    };

    private enum Direction {
        NONE, HORIZONTAL, VERTICAL
    }

    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Hold references.
        mContext = context;

        // Get the attribute values (if any).
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0);
        try {
            mFirstDayOfWeek = a.getInteger(R.styleable.WeekView_firstDayOfWeek, mFirstDayOfWeek);
            mHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourHeight, mHourHeight);
            mMinHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_minHourHeight, mMinHourHeight);
            mEffectiveMinHourHeight = mMinHourHeight;
            mMaxHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_maxHourHeight, mMaxHourHeight);
            mTextSize = a.getDimensionPixelSize(R.styleable.WeekView_textSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize, context.getResources().getDisplayMetrics()));
            mHeaderColumnPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerColumnPadding, mHeaderColumnPadding);
            mColumnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, mColumnGap);
            mHeaderColumnTextColor = a.getColor(R.styleable.WeekView_headerColumnTextColor, mHeaderColumnTextColor);
            mNumberOfVisibleDays = a.getInteger(R.styleable.WeekView_noOfVisibleDays, mNumberOfVisibleDays);
            mHeaderRowPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerRowPadding, mHeaderRowPadding);
            mHeaderRowBackgroundColor = a.getColor(R.styleable.WeekView_headerRowBackgroundColor, mHeaderRowBackgroundColor);
            mDayBackgroundColor = a.getColor(R.styleable.WeekView_dayBackgroundColor, mDayBackgroundColor);
            mFutureBackgroundColor = a.getColor(R.styleable.WeekView_futureBackgroundColor, mFutureBackgroundColor);
            mPastBackgroundColor = a.getColor(R.styleable.WeekView_pastBackgroundColor, mPastBackgroundColor);
            mFutureWeekendBackgroundColor = a.getColor(R.styleable.WeekView_futureWeekendBackgroundColor, mFutureBackgroundColor); // If not set, use the same color as in the week
            mPastWeekendBackgroundColor = a.getColor(R.styleable.WeekView_pastWeekendBackgroundColor, mPastBackgroundColor);
            mNowLineColor = a.getColor(R.styleable.WeekView_nowLineColor, mNowLineColor);
            mNowLineThickness = a.getDimensionPixelSize(R.styleable.WeekView_nowLineThickness, mNowLineThickness);
            mUseNewColoring = a.getBoolean(R.styleable.WeekView_useNewColoringStyle, mUseNewColoring);
            mHourSeparatorColor = a.getColor(R.styleable.WeekView_hourSeparatorColor, mHourSeparatorColor);
            mTodayBackgroundColor = a.getColor(R.styleable.WeekView_todayBackgroundColor, mTodayBackgroundColor);
            mHourSeparatorHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourSeparatorHeight, mHourSeparatorHeight);
            mTodayHeaderTextColor = a.getColor(R.styleable.WeekView_todayHeaderTextColor, mTodayHeaderTextColor);
            mEventTextSize = a.getDimensionPixelSize(R.styleable.WeekView_eventTextSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mEventTextSize, context.getResources().getDisplayMetrics()));
            mEventTextColor = a.getColor(R.styleable.WeekView_eventTextColor, mEventTextColor);
            mEventPadding = a.getDimensionPixelSize(R.styleable.WeekView_hourSeparatorHeight, mEventPadding);
            mHeaderColumnBackgroundColor = a.getColor(R.styleable.WeekView_headerColumnBackground, mHeaderColumnBackgroundColor);
            mDayNameLength = a.getInteger(R.styleable.WeekView_dayNameLength, mDayNameLength);
            mOverlappingEventGap = a.getDimensionPixelSize(R.styleable.WeekView_overlappingEventGap, mOverlappingEventGap);
            mEventMarginVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventMarginVertical, mEventMarginVertical);
            mXScrollingSpeed = a.getFloat(R.styleable.WeekView_xScrollingSpeed, mXScrollingSpeed);
            mEventCornerRadius = a.getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, mEventCornerRadius);
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        // Scrolling initialization.
        mGestureDetector = new GestureDetectorCompat(mContext, mGestureListener);
        mScroller = new OverScroller(mContext);

        // Measure settings for time column.
        mTimeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTimeTextPaint.setTextAlign(Paint.Align.RIGHT);
        mTimeTextPaint.setTextSize(mTextSize);
        mTimeTextPaint.setColor(mHeaderColumnTextColor);
        Rect rect = new Rect();
        mTimeTextPaint.getTextBounds("00 PM", 0, "00 PM".length(), rect);
        mTimeTextHeight = rect.height();
        mHeaderMarginBottom = mTimeTextHeight / 2;
        initTextTimeWidth();

        // Measure settings for header row.
        mHeaderTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHeaderTextPaint.setColor(mHeaderColumnTextColor);
        mHeaderTextPaint.setTextAlign(Paint.Align.CENTER);
        mHeaderTextPaint.setTextSize(mTextSize);
        mHeaderTextPaint.getTextBounds("00 PM", 0, "00 PM".length(), rect);
        mHeaderTextHeight = rect.height();
        mHeaderTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        // Prepare header background paint.
        mHeaderBackgroundPaint = new Paint();
        mHeaderBackgroundPaint.setColor(mHeaderRowBackgroundColor);

        // Prepare day background color paint.
        mDayBackgroundPaint = new Paint();
        mDayBackgroundPaint.setColor(mDayBackgroundColor);
        mFutureBackgroundPaint = new Paint();
        mFutureBackgroundPaint.setColor(mFutureBackgroundColor);
        mPastBackgroundPaint = new Paint();
        mPastBackgroundPaint.setColor(mPastBackgroundColor);
        mFutureWeekendBackgroundPaint = new Paint();
        mFutureWeekendBackgroundPaint.setColor(mFutureWeekendBackgroundColor);
        mPastWeekendBackgroundPaint = new Paint();
        mPastWeekendBackgroundPaint.setColor(mPastWeekendBackgroundColor);

        // Prepare hour separator color paint.
        mHourSeparatorPaint = new Paint();
        mHourSeparatorPaint.setStyle(Paint.Style.STROKE);
        mHourSeparatorPaint.setStrokeWidth(mHourSeparatorHeight);
        mHourSeparatorPaint.setColor(mHourSeparatorColor);

        // Prepare the "now" line color paint
        mNowLinePaint = new Paint();
        mNowLinePaint.setStrokeWidth(mNowLineThickness);
        mNowLinePaint.setColor(mNowLineColor);

        // Prepare today background color paint.
        mTodayBackgroundPaint = new Paint();
        mTodayBackgroundPaint.setColor(mTodayBackgroundColor);

        // Prepare today header text color paint.
        mTodayHeaderTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTodayHeaderTextPaint.setTextAlign(Paint.Align.CENTER);
        mTodayHeaderTextPaint.setTextSize(mTextSize);
        mTodayHeaderTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTodayHeaderTextPaint.setColor(mTodayHeaderTextColor);

        // Prepare event background color.
        mEventBackgroundPaint = new Paint();
        mEventBackgroundPaint.setColor(Color.rgb(174, 208, 238));

        // Prepare header column background color.
        mHeaderColumnBackgroundPaint = new Paint();
        mHeaderColumnBackgroundPaint.setColor(mHeaderColumnBackgroundColor);

        // Prepare event text size and color.
        mEventTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        mEventTextPaint.setStyle(Paint.Style.FILL);
        mEventTextPaint.setColor(mEventTextColor);
        mEventTextPaint.setTextSize(mEventTextSize);

        // Set default event color.
        mDefaultEventColor = Color.parseColor("#9fc6e7");

        mScaleDetector = new ScaleGestureDetector(mContext, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                mIsZooming = false;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                mIsZooming = true;
                goToNearestOrigin();
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                mNewHourHeight = Math.round(mHourHeight * detector.getScaleFactor());
                invalidate();
                return true;
            }
        });
    }

    // fix rotation changes
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mAreDimensionsInvalid = true;
    }

    /**
     * Initialize time column width. Calculate value with all possible hours (supposed widest text)
     */
    private void initTextTimeWidth() {
        mTimeTextWidth = 0;
        for (int i = 0; i < 24; i++) {
            // measure time string and get max width
            String time = getDateTimeInterpreter().interpretTime(i);
            if (time == null)
                throw new IllegalStateException("A DateTimeInterpreter must not return null time");
            mTimeTextWidth = Math.max(mTimeTextWidth, mTimeTextPaint.measureText(time));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the header row.
        drawHeaderRowAndEvents(canvas);

        // Draw the time column and all the axes/separators.
        drawTimeColumnAndAxes(canvas);

        // Hide everything in the first cell (top left corner).
        canvas.drawRect(0, 0, mTimeTextWidth + mHeaderColumnPadding * 2, mHeaderTextHeight + mHeaderRowPadding * 2, mHeaderBackgroundPaint);

        // Hide anything that is in the bottom margin of the header row.
        canvas.drawRect(mHeaderColumnWidth, mHeaderTextHeight + mHeaderRowPadding * 2, getWidth(), mHeaderRowPadding * 2 + mHeaderTextHeight + mHeaderMarginBottom + mTimeTextHeight/2, mHeaderColumnBackgroundPaint);
    }

    private void drawTimeColumnAndAxes(Canvas canvas) {
        // Draw the background color for the header column.
        canvas.drawRect(0, mHeaderTextHeight + mHeaderRowPadding * 2, mHeaderColumnWidth, getHeight(), mHeaderColumnBackgroundPaint);

        for (int i = 0; i < 24; i++) {
            float top = mHeaderTextHeight + mHeaderRowPadding * 2 + mCurrentOrigin.y + mHourHeight * i + mHeaderMarginBottom;

            // Draw the text if its y position is not outside of the visible area. The pivot point of the text is the point at the bottom-right corner.
            String time = getDateTimeInterpreter().interpretTime(i);
            if (time == null)
                throw new IllegalStateException("A DateTimeInterpreter must not return null time");
            if (top < getHeight()) canvas.drawText(time, mTimeTextWidth + mHeaderColumnPadding, top + mTimeTextHeight, mTimeTextPaint);
        }
    }

    private void drawHeaderRowAndEvents(Canvas canvas) {
        // Calculate the available width for each day.
        mHeaderColumnWidth = mTimeTextWidth + mHeaderColumnPadding *2;
        mWidthPerDay = getWidth() - mHeaderColumnWidth - mColumnGap * (mNumberOfVisibleDays - 1);
        mWidthPerDay = mWidthPerDay/mNumberOfVisibleDays;

        Calendar today = today();

        if (mAreDimensionsInvalid) {
            mEffectiveMinHourHeight= Math.max(mMinHourHeight, (int) ((getHeight() - mHeaderTextHeight - mHeaderRowPadding * 2 - mHeaderMarginBottom) / 24));

            mAreDimensionsInvalid = false;
            if(mScrollToDay != null)
                goToDate(mScrollToDay);

            mAreDimensionsInvalid = false;
            if(mScrollToHour >= 0)
                goToHour(mScrollToHour);

            mScrollToDay = null;
            mScrollToHour = -1;
            mAreDimensionsInvalid = false;
        }
        if (mIsFirstDraw){
            mIsFirstDraw = false;

            // If the week view is being drawn for the first time, then consider the first day of the week.
            if(mNumberOfVisibleDays >= 7 && today.get(Calendar.DAY_OF_WEEK) != mFirstDayOfWeek) {
                int difference = 7 + (today.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek);
                mCurrentOrigin.x += (mWidthPerDay + mColumnGap) * difference;
            }
        }

        //Calculate the new height due to the zooming.
        if (mNewHourHeight > 0){
            if(mNewHourHeight < mEffectiveMinHourHeight)
                mNewHourHeight = mEffectiveMinHourHeight;
            else if(mNewHourHeight > mMaxHourHeight)
                mNewHourHeight = mMaxHourHeight;

            mCurrentOrigin.y = (mCurrentOrigin.y/mHourHeight)*mNewHourHeight;
            mHourHeight = mNewHourHeight;
            mNewHourHeight = -1;
        }

        //if the new mCurrentOrigin.y is invalid, make it valid.
        if (mCurrentOrigin.y < getHeight() - mHourHeight * 24 - mHeaderTextHeight - mHeaderRowPadding * 2 - mHeaderMarginBottom - mTimeTextHeight/2)
            mCurrentOrigin.y = getHeight() - mHourHeight * 24 - mHeaderTextHeight - mHeaderRowPadding * 2 - mHeaderMarginBottom - mTimeTextHeight/2;
        //Don't put an else if because it will trigger a glitch when completly zoomed out and scrolling vertically.
        if(mCurrentOrigin.y > 0) {
            mCurrentOrigin.y = 0;
        }

        // Consider scroll offset.
        int leftDaysWithGaps = (int) -(Math.ceil(mCurrentOrigin.x / (mWidthPerDay + mColumnGap)));
        float startFromPixel = mCurrentOrigin.x + (mWidthPerDay + mColumnGap) * leftDaysWithGaps +
                mHeaderColumnWidth;
        float startPixel = startFromPixel;

        // Prepare to iterate for each day.
        Calendar day = (Calendar) today.clone();
        day.add(Calendar.HOUR, 6);

        // Prepare to iterate for each hour to draw the hour lines.
        int lineCount = (int) ((getHeight() - mHeaderTextHeight - mHeaderRowPadding * 2 -
                mHeaderMarginBottom) / mHourHeight) + 1;
        lineCount = (lineCount) * (mNumberOfVisibleDays+1);
        float[] hourLines = new float[lineCount * 4];

        // Clear the cache for event rectangles.
        if (mEventRects != null) {
            for (EventRect eventRect: mEventRects) {
                eventRect.rectF = null;
            }
        }

        // Iterate through each day.
        Calendar oldFirstVisibleDay = mFirstVisibleDay;
        mFirstVisibleDay = (Calendar) today.clone();
        mFirstVisibleDay.add(Calendar.DATE, -(Math.round(mCurrentOrigin.x / (mWidthPerDay + mColumnGap))));
        if(!mFirstVisibleDay.equals(oldFirstVisibleDay) && mScrollListener != null){
            mScrollListener.onFirstVisibleDayChanged(mFirstVisibleDay, oldFirstVisibleDay);
        }
        for (int dayNumber = leftDaysWithGaps + 1;
             dayNumber <= leftDaysWithGaps + mNumberOfVisibleDays + 1;
             dayNumber++) {

            // Check if the day is today.
            day = (Calendar) today.clone();
            mLastVisibleDay = (Calendar) day.clone();
            day.add(Calendar.DATE, dayNumber - 1);
            mLastVisibleDay.add(Calendar.DATE, dayNumber - 2);
            boolean sameDay = isSameDay(day, today);

            // Get more events if necessary. We want to store the events 3 months beforehand. Get
            // events only when it is the first iteration of the loop.
            if (mEventRects == null || mRefreshEvents ||
                    (dayNumber == leftDaysWithGaps + 1 && mFetchedPeriod != (int) mWeekViewLoader.toWeekViewPeriodIndex(day) &&
                            Math.abs(mFetchedPeriod - mWeekViewLoader.toWeekViewPeriodIndex(day)) > 0.5)) {
                getMoreEvents(day);
                mRefreshEvents = false;
            }

            // Draw background color for each day.
            float start =  (startPixel < mHeaderColumnWidth ? mHeaderColumnWidth : startPixel);
            if (mWidthPerDay + startPixel - start> 0){
                if(mUseNewColoring){
                    boolean isWeekend = (day.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || day.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
                    Paint pastPaint = isWeekend ? mPastWeekendBackgroundPaint : mPastBackgroundPaint;
                    Paint futurePaint = isWeekend ? mFutureWeekendBackgroundPaint : mFutureBackgroundPaint;
                    float startY = mHeaderTextHeight + mHeaderRowPadding * 2 + mTimeTextHeight/2 + mHeaderMarginBottom + mCurrentOrigin.y;

                    if(sameDay){
                        Calendar now = Calendar.getInstance();
                        float beforeNow = (now.get(Calendar.HOUR_OF_DAY)+now.get(Calendar.MINUTE)/60.0f)*mHourHeight;
                        canvas.drawRect(start, startY, startPixel + mWidthPerDay, startY+beforeNow, pastPaint);
                        canvas.drawRect(start, startY+beforeNow, startPixel + mWidthPerDay, getHeight(), futurePaint);
                    } else if(day.before(today)) {
                        canvas.drawRect(start, startY, startPixel + mWidthPerDay, getHeight(), pastPaint);
                    } else {
                        canvas.drawRect(start, startY, startPixel + mWidthPerDay, getHeight(), futurePaint);
                    }
                }else{
                    canvas.drawRect(start, mHeaderTextHeight + mHeaderRowPadding * 2 + mTimeTextHeight/2 + mHeaderMarginBottom, startPixel + mWidthPerDay, getHeight(), sameDay ? mTodayBackgroundPaint : mDayBackgroundPaint);
                }
            }

            // Prepare the separator lines for hours.
            int i = 0;
            for (int hourNumber = 0; hourNumber < 24; hourNumber++) {
                float top = mHeaderTextHeight + mHeaderRowPadding * 2 + mCurrentOrigin.y + mHourHeight * hourNumber + mTimeTextHeight/2 + mHeaderMarginBottom;
                if (top > mHeaderTextHeight + mHeaderRowPadding * 2 + mTimeTextHeight/2 + mHeaderMarginBottom - mHourSeparatorHeight && top < getHeight() && startPixel + mWidthPerDay - start > 0){
                    hourLines[i * 4] = start;
                    hourLines[i * 4 + 1] = top;
                    hourLines[i * 4 + 2] = startPixel + mWidthPerDay;
                    hourLines[i * 4 + 3] = top;
                    i++;
                }
            }

            // Draw the lines for hours.
            canvas.drawLines(hourLines, mHourSeparatorPaint);

            // Draw the events.
            drawEvents(day, startPixel, canvas);

            //Draw the line at the current time
            if(mUseNewColoring && sameDay){
                float startY = mHeaderTextHeight + mHeaderRowPadding * 2 + mTimeTextHeight/2 + mHeaderMarginBottom + mCurrentOrigin.y;
                Calendar now = Calendar.getInstance();
                float beforeNow = (now.get(Calendar.HOUR_OF_DAY)+now.get(Calendar.MINUTE)/60.0f)*mHourHeight;
                canvas.drawLine(start, startY+beforeNow, startPixel + mWidthPerDay, startY+beforeNow, mNowLinePaint);
            }

            // In the next iteration, start from the next day.
            startPixel += mWidthPerDay + mColumnGap;
        }

        // Draw the header background.
        canvas.drawRect(0, 0, getWidth(), mHeaderTextHeight + mHeaderRowPadding * 2, mHeaderBackgroundPaint);

        // Draw the header row texts.
        startPixel = startFromPixel;
        for (int dayNumber=leftDaysWithGaps+1; dayNumber <= leftDaysWithGaps + mNumberOfVisibleDays + 1; dayNumber++) {
            // Check if the day is today.
            day = (Calendar) today.clone();
            day.add(Calendar.DATE, dayNumber - 1);
            boolean sameDay = isSameDay(day, today);

            // Draw the day labels.
            String dayLabel = getDateTimeInterpreter().interpretDate(day);
            if (dayLabel == null)
                throw new IllegalStateException("A DateTimeInterpreter must not return null date");
            canvas.drawText(dayLabel, startPixel + mWidthPerDay / 2, mHeaderTextHeight + mHeaderRowPadding, sameDay ? mTodayHeaderTextPaint : mHeaderTextPaint);
            startPixel += mWidthPerDay + mColumnGap;
        }

    }

    /**
     * Get the time and date where the user clicked on.
     * @param x The x position of the touch event.
     * @param y The y position of the touch event.
     * @return The time and date at the clicked position.
     */
    private Calendar getTimeFromPoint(float x, float y){
        int leftDaysWithGaps = (int) -(Math.ceil(mCurrentOrigin.x / (mWidthPerDay + mColumnGap)));
        float startPixel = mCurrentOrigin.x + (mWidthPerDay + mColumnGap) * leftDaysWithGaps +
                mHeaderColumnWidth;
        for (int dayNumber = leftDaysWithGaps + 1;
             dayNumber <= leftDaysWithGaps + mNumberOfVisibleDays + 1;
             dayNumber++) {
            float start =  (startPixel < mHeaderColumnWidth ? mHeaderColumnWidth : startPixel);
            if (mWidthPerDay + startPixel - start> 0
                    && x>start && x<startPixel + mWidthPerDay){
                Calendar day = today();
                day.add(Calendar.DATE, dayNumber - 1);
                float pixelsFromZero = y - mCurrentOrigin.y - mHeaderTextHeight
                        - mHeaderRowPadding * 2 - mTimeTextHeight/2 - mHeaderMarginBottom;
                int hour = (int)(pixelsFromZero / mHourHeight);
                int minute = (int) (60 * (pixelsFromZero - hour * mHourHeight) / mHourHeight);
                day.add(Calendar.HOUR, hour);
                day.set(Calendar.MINUTE, minute);
                return day;
            }
            startPixel += mWidthPerDay + mColumnGap;
        }
        return null;
    }

    /**
     * Draw all the events of a particular day.
     * @param date The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas The canvas to draw upon.
     */
    private void drawEvents(Calendar date, float startFromPixel, Canvas canvas) {
        if (mEventRects != null && mEventRects.size() > 0) {
            for (int i = 0; i < mEventRects.size(); i++) {
                if (isSameDay(mEventRects.get(i).event.getStartTime(), date)) {

                    // Calculate top.
                    float top = mHourHeight * 24 * mEventRects.get(i).top / 1440 + mCurrentOrigin.y + mHeaderTextHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom + mTimeTextHeight/2 + mEventMarginVertical;

                    // Calculate bottom.
                    float bottom = mEventRects.get(i).bottom;
                    bottom = mHourHeight * 24 * bottom / 1440 + mCurrentOrigin.y + mHeaderTextHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom + mTimeTextHeight/2 - mEventMarginVertical;

                    // Calculate left and right.
                    float left = startFromPixel + mEventRects.get(i).left * mWidthPerDay;
                    if (left < startFromPixel)
                        left += mOverlappingEventGap;
                    float right = left + mEventRects.get(i).width * mWidthPerDay;
                    if (right < startFromPixel + mWidthPerDay)
                        right -= mOverlappingEventGap;

                    // Draw the event and the event name on top of it.
                    RectF eventRectF = new RectF(left, top, right, bottom);
                    if (bottom > mHeaderTextHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom + mTimeTextHeight/2 && left < right &&
                            eventRectF.right > mHeaderColumnWidth &&
                            eventRectF.left < getWidth() &&
                            eventRectF.bottom > mHeaderTextHeight + mHeaderRowPadding * 2 + mTimeTextHeight / 2 + mHeaderMarginBottom &&
                            eventRectF.top < getHeight() &&
                            left < right
                            ) {
                        mEventRects.get(i).rectF = eventRectF;
                        mEventBackgroundPaint.setColor(mEventRects.get(i).event.getColor() == 0 ? mDefaultEventColor : mEventRects.get(i).event.getColor());
                        canvas.drawRoundRect(mEventRects.get(i).rectF, mEventCornerRadius, mEventCornerRadius, mEventBackgroundPaint);
                        drawEventTitle(mEventRects.get(i).event, mEventRects.get(i).rectF, canvas, top, left);
                    }
                    else
                        mEventRects.get(i).rectF = null;
                }
            }
        }
    }


    /**
     * Draw the name of the event on top of the event rectangle.
     * @param event The event of which the title (and location) should be drawn.
     * @param rect The rectangle on which the text is to be drawn.
     * @param canvas The canvas to draw upon.
     * @param originalTop The original top position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     * @param originalLeft The original left position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     */
    private void drawEventTitle(WeekViewEvent event, RectF rect, Canvas canvas, float originalTop, float originalLeft) {
        if (rect.right - rect.left - mEventPadding * 2 < 0) return;
        if (rect.bottom - rect.top - mEventPadding * 2 < 0) return;

        SpannableStringBuilder bob = new SpannableStringBuilder();
        if (event.getName() != null) {
            bob.append(event.getName());
            bob.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),0,bob.length(),0);
            bob.append(' ');
        }
        if (event.getLocation() != null) {
            bob.append(event.getLocation());
        }

        int availableHeight = (int) (rect.bottom - originalTop - mEventPadding * 2);
        int availableWidth = (int) (rect.right - originalLeft - mEventPadding * 2);

        // Get text dimensions
        StaticLayout textLayout = new StaticLayout(bob, mEventTextPaint, availableWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        int lineHeight = textLayout.getHeight() / textLayout.getLineCount();

        if (availableHeight >= lineHeight) {
            // calculate available lines
            int availableLineCount = availableHeight / lineHeight;
            do {
                // ellipsize text to fit into event rect
                textLayout = new StaticLayout(TextUtils.ellipsize(bob, mEventTextPaint, availableLineCount * availableWidth, TextUtils.TruncateAt.END), mEventTextPaint, (int) (rect.right - originalLeft - mEventPadding * 2), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                // reduce line count
                availableLineCount--;
                // and repeat until text is short enough
            } while (textLayout.getHeight() > availableHeight);

            // Draw text
            canvas.save();
            canvas.translate(originalLeft + mEventPadding, originalTop + mEventPadding);
            textLayout.draw(canvas);
            canvas.restore();
        }
    }


    /**
     * A class to hold reference to the events and their visual representation. An EventRect is
     * actually the rectangle that is drawn on the calendar for a given event. There may be more
     * than one rectangle for a single event (an event that expands more than one day). In that
     * case two instances of the EventRect will be used for a single event. The given event will be
     * stored in "originalEvent". But the event that corresponds to rectangle the rectangle
     * instance will be stored in "event".
     */
    private class EventRect {
        public WeekViewEvent event;
        public WeekViewEvent originalEvent;
        public RectF rectF;
        public float left;
        public float width;
        public float top;
        public float bottom;

        /**
         * Create a new instance of event rect. An EventRect is actually the rectangle that is drawn
         * on the calendar for a given event. There may be more than one rectangle for a single
         * event (an event that expands more than one day). In that case two instances of the
         * EventRect will be used for a single event. The given event will be stored in
         * "originalEvent". But the event that corresponds to rectangle the rectangle instance will
         * be stored in "event".
         * @param event Represents the event which this instance of rectangle represents.
         * @param originalEvent The original event that was passed by the user.
         * @param rectF The rectangle.
         */
        public EventRect(WeekViewEvent event, WeekViewEvent originalEvent, RectF rectF) {
            this.event = event;
            this.rectF = rectF;
            this.originalEvent = originalEvent;
        }
    }


    /**
     * Gets more events of one/more month(s) if necessary. This method is called when the user is
     * scrolling the week view. The week view stores the events of three months: the visible month,
     * the previous month, the next month.
     * @param day The day where the user is currently is.
     */
    private void getMoreEvents(Calendar day) {
        // Get more events if the month is changed.
        if (mEventRects == null)
            mEventRects = new ArrayList<EventRect>();
        if (mWeekViewLoader == null && !isInEditMode())
            throw new IllegalStateException("You must provide a MonthChangeListener");

        // If a refresh was requested then reset some variables.
        if (mRefreshEvents) {
            mEventRects.clear();
            mPreviousPeriodEvents = null;
            mCurrentPeriodEvents = null;
            mNextPeriodEvents = null;
            mFetchedPeriod = -1;
        }

        if(mWeekViewLoader != null){
            int periodToFetch = (int) mWeekViewLoader.toWeekViewPeriodIndex(day);
            if (!isInEditMode() && (mFetchedPeriod < 0 || mFetchedPeriod != periodToFetch || mRefreshEvents)) {
                List<WeekViewEvent> previousPeriodEvents = null;
                List<WeekViewEvent> currentPeriodEvents = null;
                List<WeekViewEvent> nextPeriodEvents = null;

                if(mPreviousPeriodEvents != null && mCurrentPeriodEvents != null && mNextPeriodEvents != null){
                    if(periodToFetch == mFetchedPeriod-1){
                        currentPeriodEvents = mPreviousPeriodEvents;
                        nextPeriodEvents = mCurrentPeriodEvents;
                    }else if(periodToFetch == mFetchedPeriod){
                        previousPeriodEvents = mPreviousPeriodEvents;
                        currentPeriodEvents = mCurrentPeriodEvents;
                        nextPeriodEvents = mNextPeriodEvents;
                    }else if(periodToFetch == mFetchedPeriod+1){
                        previousPeriodEvents = mCurrentPeriodEvents;
                        currentPeriodEvents = mNextPeriodEvents;
                    }
                }
                if(currentPeriodEvents == null)
                    currentPeriodEvents = mWeekViewLoader.onLoad(periodToFetch);
                if(previousPeriodEvents == null)
                    previousPeriodEvents = mWeekViewLoader.onLoad(periodToFetch-1);
                if(nextPeriodEvents == null)
                    nextPeriodEvents = mWeekViewLoader.onLoad(periodToFetch+1);


                //clear events
                mEventRects.clear();
                sortAndCacheEvents(previousPeriodEvents);
                sortAndCacheEvents(currentPeriodEvents);
                sortAndCacheEvents(nextPeriodEvents);

                mPreviousPeriodEvents = previousPeriodEvents;
                mCurrentPeriodEvents = currentPeriodEvents;
                mNextPeriodEvents = nextPeriodEvents;
                mFetchedPeriod = periodToFetch;
            }
        }

        // Prepare to calculate positions of each events.
        List<EventRect> tempEvents = mEventRects;
        mEventRects = new ArrayList<EventRect>();

        // Iterate through each day with events to calculate the position of the events.
        while (tempEvents.size() > 0) {
            ArrayList<EventRect> eventRects = new ArrayList<>(tempEvents.size());

            // get first event for a day
            EventRect eventRect1 = tempEvents.remove(0);
            eventRects.add(eventRect1);

            int i = 0;
            while (i < tempEvents.size()) {
                // collect all other events for same day
                EventRect eventRect2 = tempEvents.get(i);
                if (isSameDay(eventRect1.event.getStartTime(), eventRect2.event.getStartTime())) {
                    tempEvents.remove(i);
                    eventRects.add(eventRect2);
                } else {
                    i++;
                }
            }
            computePositionOfEvents(eventRects);
        }
    }

    /**
     * Cache the event for smooth scrolling functionality.
     * @param event The event to cache.
     */
    private void cacheEvent(WeekViewEvent event) {
        if (!isSameDay(event.getStartTime(), event.getEndTime())) {
            // add first day
            Calendar endTime = (Calendar) event.getStartTime().clone();
            endTime.set(Calendar.HOUR_OF_DAY, 23);
            endTime.set(Calendar.MINUTE, 59);
            WeekViewEvent event1 = new WeekViewEvent(event.getId(), event.getName(), event.getLocation(), event.getStartTime(), endTime);
            event1.setColor(event.getColor());
            mEventRects.add(new EventRect(event1, event, null));

            // add over days
            Calendar otherDay = (Calendar) event.getStartTime().clone();
            otherDay.add(Calendar.DATE, 1);
            while (!isSameDay(otherDay, event.getEndTime())) {
                Calendar overDay = (Calendar) otherDay.clone();
                overDay.set(Calendar.HOUR_OF_DAY, 0);
                overDay.set(Calendar.MINUTE, 0);
                Calendar endOfOverDay = (Calendar) overDay.clone();
                endOfOverDay.set(Calendar.HOUR_OF_DAY, 23);
                endOfOverDay.set(Calendar.MINUTE, 59);
                WeekViewEvent eventMore = new WeekViewEvent(event.getId(), event.getName(), overDay, endOfOverDay);
                eventMore.setColor(event.getColor());
                mEventRects.add(new EventRect(eventMore, event, null));

                // next day
                otherDay.add(Calendar.DATE, 1);
            }

            // add last day
            Calendar startTime = (Calendar) event.getEndTime().clone();
            startTime.set(Calendar.HOUR_OF_DAY, 0);
            startTime.set(Calendar.MINUTE, 0);
            WeekViewEvent event2 = new WeekViewEvent(event.getId(), event.getName(), event.getLocation(), startTime, event.getEndTime());
            event2.setColor(event.getColor());
            mEventRects.add(new EventRect(event2, event, null));
        }
        else
            mEventRects.add(new EventRect(event, event, null));
    }

    private void sortAndCacheEvents(List<WeekViewEvent> events) {
        sortEvents(events);
        for (WeekViewEvent event : events) {
            cacheEvent(event);
        }
    }

    /**
     * Sorts the events in ascending order.
     * @param events The events to be sorted.
     */
    private void sortEvents(List<WeekViewEvent> events) {
        Collections.sort(events, new Comparator<WeekViewEvent>() {
            @Override
            public int compare(WeekViewEvent event1, WeekViewEvent event2) {
                long start1 = event1.getStartTime().getTimeInMillis();
                long start2 = event2.getStartTime().getTimeInMillis();
                int comparator = start1 > start2 ? 1 : (start1 < start2 ? -1 : 0);
                if (comparator == 0) {
                    long end1 = event1.getEndTime().getTimeInMillis();
                    long end2 = event2.getEndTime().getTimeInMillis();
                    comparator = end1 > end2 ? 1 : (end1 < end2 ? -1 : 0);
                }
                return comparator;
            }
        });
    }

    /**
     * Calculates the left and right positions of each events. This comes handy specially if events
     * are overlapping.
     * @param eventRects The events along with their wrapper class.
     */
    private void computePositionOfEvents(List<EventRect> eventRects) {
        // Make "collision groups" for all events that collide with others.
        List<List<EventRect>> collisionGroups = new ArrayList<List<EventRect>>();
        for (EventRect eventRect : eventRects) {
            boolean isPlaced = false;
            outerLoop:
            for (List<EventRect> collisionGroup : collisionGroups) {
                for (EventRect groupEvent : collisionGroup) {
                    if (isEventsCollide(groupEvent.event, eventRect.event)) {
                        collisionGroup.add(eventRect);
                        isPlaced = true;
                        break outerLoop;
                    }
                }
            }
            if (!isPlaced) {
                List<EventRect> newGroup = new ArrayList<EventRect>();
                newGroup.add(eventRect);
                collisionGroups.add(newGroup);
            }
        }

        for (List<EventRect> collisionGroup : collisionGroups) {
            expandEventsToMaxWidth(collisionGroup);
        }
    }

    /**
     * Expands all the events to maximum possible width. The events will try to occupy maximum
     * space available horizontally.
     * @param collisionGroup The group of events which overlap with each other.
     */
    private void expandEventsToMaxWidth(List<EventRect> collisionGroup) {
        // Expand the events to maximum possible width.
        List<List<EventRect>> columns = new ArrayList<List<EventRect>>();
        columns.add(new ArrayList<EventRect>());
        for (EventRect eventRect : collisionGroup) {
            boolean isPlaced = false;
            for (List<EventRect> column : columns) {
                if (column.size() == 0) {
                    column.add(eventRect);
                    isPlaced = true;
                }
                else if (!isEventsCollide(eventRect.event, column.get(column.size()-1).event)) {
                    column.add(eventRect);
                    isPlaced = true;
                    break;
                }
            }
            if (!isPlaced) {
                List<EventRect> newColumn = new ArrayList<EventRect>();
                newColumn.add(eventRect);
                columns.add(newColumn);
            }
        }


        // Calculate left and right position for all the events.
        // Get the maxRowCount by looking in all columns.
        int maxRowCount = 0;
        for (List<EventRect> column : columns){
            maxRowCount = Math.max(maxRowCount, column.size());
        }
        for (int i = 0; i < maxRowCount; i++) {
            // Set the left and right values of the event.
            float j = 0;
            for (List<EventRect> column : columns) {
                if (column.size() >= i+1) {
                    EventRect eventRect = column.get(i);
                    eventRect.width = 1f / columns.size();
                    eventRect.left = j / columns.size();
                    eventRect.top = eventRect.event.getStartTime().get(Calendar.HOUR_OF_DAY) * 60 + eventRect.event.getStartTime().get(Calendar.MINUTE);
                    eventRect.bottom = eventRect.event.getEndTime().get(Calendar.HOUR_OF_DAY) * 60 + eventRect.event.getEndTime().get(Calendar.MINUTE);
                    mEventRects.add(eventRect);
                }
                j++;
            }
        }
    }


    /**
     * Checks if two events overlap.
     * @param event1 The first event.
     * @param event2 The second event.
     * @return true if the events overlap.
     */
    private boolean isEventsCollide(WeekViewEvent event1, WeekViewEvent event2) {
        long start1 = event1.getStartTime().getTimeInMillis();
        long end1 = event1.getEndTime().getTimeInMillis();
        long start2 = event2.getStartTime().getTimeInMillis();
        long end2 = event2.getEndTime().getTimeInMillis();
        return !((start1 >= end2) || (end1 <= start2));
    }


    /**
     * Checks if time1 occurs after (or at the same time) time2.
     * @param time1 The time to check.
     * @param time2 The time to check against.
     * @return true if time1 and time2 are equal or if time1 is after time2. Otherwise false.
     */
    private boolean isTimeAfterOrEquals(Calendar time1, Calendar time2) {
        return !(time1 == null || time2 == null) && time1.getTimeInMillis() >= time2.getTimeInMillis();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        mAreDimensionsInvalid = true;
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to setting and getting the properties.
    //
    /////////////////////////////////////////////////////////////////

    public void setOnEventClickListener (EventClickListener listener) {
        this.mEventClickListener = listener;
    }

    public EventClickListener getEventClickListener() {
        return mEventClickListener;
    }

    public MonthChangeListener getMonthChangeListener() {
        if(mWeekViewLoader instanceof MonthLoader)
            return ((MonthLoader) mWeekViewLoader).getOnMonthChangeListener();
        return null;
    }

    public void setMonthChangeListener(MonthChangeListener monthChangeListener) {
        this.mWeekViewLoader = new MonthLoader(monthChangeListener);
    }

    public WeekViewLoader getWeekViewLoader(){
        return mWeekViewLoader;
    }

    public void setWeekViewLoader(WeekViewLoader loader){
        this.mWeekViewLoader = loader;
    }

    public EventLongPressListener getEventLongPressListener() {
        return mEventLongPressListener;
    }

    public void setEventLongPressListener(EventLongPressListener eventLongPressListener) {
        this.mEventLongPressListener = eventLongPressListener;
    }

    public void setEmptyViewClickListener(EmptyViewClickListener emptyViewClickListener){
        this.mEmptyViewClickListener = emptyViewClickListener;
    }

    public EmptyViewClickListener getEmptyViewClickListener(){
        return mEmptyViewClickListener;
    }

    public void setEmptyViewLongPressListener(EmptyViewLongPressListener emptyViewLongPressListener){
        this.mEmptyViewLongPressListener = emptyViewLongPressListener;
    }

    public EmptyViewLongPressListener getEmptyViewLongPressListener(){
        return mEmptyViewLongPressListener;
    }

    public void setScrollListener(ScrollListener scrolledListener){
        this.mScrollListener = scrolledListener;
    }

    public ScrollListener getScrollListener(){
        return mScrollListener;
    }
    /**
     * Get the interpreter which provides the text to show in the header column and the header row.
     * @return The date, time interpreter.
     */
    public DateTimeInterpreter getDateTimeInterpreter() {
        if (mDateTimeInterpreter == null) {
            mDateTimeInterpreter = new DateTimeInterpreter() {
                @Override
                public String interpretDate(Calendar date) {
                    try {
                        SimpleDateFormat sdf = mDayNameLength == LENGTH_SHORT ? new SimpleDateFormat("EEEEE M/dd", Locale.getDefault()) : new SimpleDateFormat("EEE M/dd", Locale.getDefault());
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
                        SimpleDateFormat sdf = DateFormat.is24HourFormat(getContext()) ? new SimpleDateFormat("HH:mm", Locale.getDefault()) : new SimpleDateFormat("hh a", Locale.getDefault());
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

    /**
     * Set the interpreter which provides the text to show in the header column and the header row.
     * @param dateTimeInterpreter The date, time interpreter.
     */
    public void setDateTimeInterpreter(DateTimeInterpreter dateTimeInterpreter){
        this.mDateTimeInterpreter = dateTimeInterpreter;

        // refresh time column width
        initTextTimeWidth();
    }


    /**
     * Get the number of visible days in a week.
     * @return The number of visible days in a week.
     */
    public int getNumberOfVisibleDays() {
        return mNumberOfVisibleDays;
    }

    /**
     * Set the number of visible days in a week.
     * @param numberOfVisibleDays The number of visible days in a week.
     */
    public void setNumberOfVisibleDays(int numberOfVisibleDays) {
        this.mNumberOfVisibleDays = numberOfVisibleDays;
        mCurrentOrigin.x = 0;
        mCurrentOrigin.y = 0;
        invalidate();
    }

    public int getHourHeight() {
        return mHourHeight;
    }

    public void setHourHeight(int hourHeight) {
        mNewHourHeight = hourHeight;
        invalidate();
    }

    public int getColumnGap() {
        return mColumnGap;
    }

    public void setColumnGap(int columnGap) {
        mColumnGap = columnGap;
        invalidate();
    }

    public int getFirstDayOfWeek() {
        return mFirstDayOfWeek;
    }

    /**
     * Set the first day of the week. First day of the week is used only when the week view is first
     * drawn. It does not of any effect after user starts scrolling horizontally.
     * <p>
     *     <b>Note:</b> This method will only work if the week view is set to display more than 6 days at
     *     once.
     * </p>
     * @param firstDayOfWeek The supported values are {@link java.util.Calendar#SUNDAY},
     * {@link java.util.Calendar#MONDAY}, {@link java.util.Calendar#TUESDAY},
     * {@link java.util.Calendar#WEDNESDAY}, {@link java.util.Calendar#THURSDAY},
     * {@link java.util.Calendar#FRIDAY}.
     */
    public void setFirstDayOfWeek(int firstDayOfWeek) {
        mFirstDayOfWeek = firstDayOfWeek;
        invalidate();
    }

    public int getTextSize() {
        return mTextSize;
    }

    public void setTextSize(int textSize) {
        mTextSize = textSize;
        mTodayHeaderTextPaint.setTextSize(mTextSize);
        mHeaderTextPaint.setTextSize(mTextSize);
        mTimeTextPaint.setTextSize(mTextSize);
        invalidate();
    }

    public int getHeaderColumnPadding() {
        return mHeaderColumnPadding;
    }

    public void setHeaderColumnPadding(int headerColumnPadding) {
        mHeaderColumnPadding = headerColumnPadding;
        invalidate();
    }

    public int getHeaderColumnTextColor() {
        return mHeaderColumnTextColor;
    }

    public void setHeaderColumnTextColor(int headerColumnTextColor) {
        mHeaderColumnTextColor = headerColumnTextColor;
        invalidate();
    }

    public int getHeaderRowPadding() {
        return mHeaderRowPadding;
    }

    public void setHeaderRowPadding(int headerRowPadding) {
        mHeaderRowPadding = headerRowPadding;
        invalidate();
    }

    public int getHeaderRowBackgroundColor() {
        return mHeaderRowBackgroundColor;
    }

    public void setHeaderRowBackgroundColor(int headerRowBackgroundColor) {
        mHeaderRowBackgroundColor = headerRowBackgroundColor;
        invalidate();
    }

    public int getDayBackgroundColor() {
        return mDayBackgroundColor;
    }

    public void setDayBackgroundColor(int dayBackgroundColor) {
        mDayBackgroundColor = dayBackgroundColor;
        invalidate();
    }

    public int getHourSeparatorColor() {
        return mHourSeparatorColor;
    }

    public void setHourSeparatorColor(int hourSeparatorColor) {
        mHourSeparatorColor = hourSeparatorColor;
        invalidate();
    }

    public int getTodayBackgroundColor() {
        return mTodayBackgroundColor;
    }

    public void setTodayBackgroundColor(int todayBackgroundColor) {
        mTodayBackgroundColor = todayBackgroundColor;
        invalidate();
    }

    public int getHourSeparatorHeight() {
        return mHourSeparatorHeight;
    }

    public void setHourSeparatorHeight(int hourSeparatorHeight) {
        mHourSeparatorHeight = hourSeparatorHeight;
        invalidate();
    }

    public int getTodayHeaderTextColor() {
        return mTodayHeaderTextColor;
    }

    public void setTodayHeaderTextColor(int todayHeaderTextColor) {
        mTodayHeaderTextColor = todayHeaderTextColor;
        invalidate();
    }

    public int getEventTextSize() {
        return mEventTextSize;
    }

    public void setEventTextSize(int eventTextSize) {
        mEventTextSize = eventTextSize;
        mEventTextPaint.setTextSize(mEventTextSize);
        invalidate();
    }

    public int getEventTextColor() {
        return mEventTextColor;
    }

    public void setEventTextColor(int eventTextColor) {
        mEventTextColor = eventTextColor;
        invalidate();
    }

    public int getEventPadding() {
        return mEventPadding;
    }

    public void setEventPadding(int eventPadding) {
        mEventPadding = eventPadding;
        invalidate();
    }

    public int getHeaderColumnBackgroundColor() {
        return mHeaderColumnBackgroundColor;
    }

    public void setHeaderColumnBackgroundColor(int headerColumnBackgroundColor) {
        mHeaderColumnBackgroundColor = headerColumnBackgroundColor;
        invalidate();
    }

    public int getDefaultEventColor() {
        return mDefaultEventColor;
    }

    public void setDefaultEventColor(int defaultEventColor) {
        mDefaultEventColor = defaultEventColor;
        invalidate();
    }

    /**
     * <b>Note:</b> Use {@link #setDateTimeInterpreter(DateTimeInterpreter)} and
     * {@link #getDateTimeInterpreter()} instead.
     * @return Either long or short day name is being used.
     */
    @Deprecated
    public int getDayNameLength() {
        return mDayNameLength;
    }

    /**
     * Set the length of the day name displayed in the header row. Example of short day names is
     * 'M' for 'Monday' and example of long day names is 'Mon' for 'Monday'.
     * <p>
     *     <b>Note:</b> Use {@link #setDateTimeInterpreter(DateTimeInterpreter)} instead.
     * </p>
     * @param length Supported values are {@link com.alamkanak.weekview.WeekView#LENGTH_SHORT} and
     * {@link com.alamkanak.weekview.WeekView#LENGTH_LONG}.
     */
    @Deprecated
    public void setDayNameLength(int length) {
        if (length != LENGTH_LONG && length != LENGTH_SHORT) {
            throw new IllegalArgumentException("length parameter must be either LENGTH_LONG or LENGTH_SHORT");
        }
        this.mDayNameLength = length;
    }

    public int getOverlappingEventGap() {
        return mOverlappingEventGap;
    }

    /**
     * Set the gap between overlapping events.
     * @param overlappingEventGap The gap between overlapping events.
     */
    public void setOverlappingEventGap(int overlappingEventGap) {
        this.mOverlappingEventGap = overlappingEventGap;
        invalidate();
    }

    public int getEventCornerRadius() {
        return mEventCornerRadius;
    }

    /**
     * Set corner radius for event rect.
     *
     * @param eventCornerRadius the radius in px.
     */
    public void setEventCornerRadius(int eventCornerRadius) {
        mEventCornerRadius = eventCornerRadius;
    }

    public int getEventMarginVertical() {
        return mEventMarginVertical;
    }

    /**
     * Set the top and bottom margin of the event. The event will release this margin from the top
     * and bottom edge. This margin is useful for differentiation consecutive events.
     * @param eventMarginVertical The top and bottom margin.
     */
    public void setEventMarginVertical(int eventMarginVertical) {
        this.mEventMarginVertical = eventMarginVertical;
        invalidate();
    }

    /**
     * Returns the first visible day in the week view.
     * @return The first visible day in the week view.
     */
    public Calendar getFirstVisibleDay() {
        return mFirstVisibleDay;
    }

    /**
     * Returns the last visible day in the week view.
     * @return The last visible day in the week view.
     */
    public Calendar getLastVisibleDay() {
        return mLastVisibleDay;
    }

    /**
     * Get the scrolling speed factor in horizontal direction.
     * @return The speed factor in horizontal direction.
     */
    public float getXScrollingSpeed() {
        return mXScrollingSpeed;
    }

    /**
     * Sets the speed for horizontal scrolling.
     * @param xScrollingSpeed The new horizontal scrolling speed.
     */
    public void setXScrollingSpeed(float xScrollingSpeed) {
        this.mXScrollingSpeed = xScrollingSpeed;
    }
    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to scrolling.
    //
    /////////////////////////////////////////////////////////////////

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        boolean val = mGestureDetector.onTouchEvent(event);

        // check after call of mGestureDetector, so mCurrentFlingDirection and mCurrentScrollDirection are set
        if (event.getAction() == MotionEvent.ACTION_UP && !mIsZooming && mCurrentFlingDirection == Direction.NONE) {
            if (mCurrentScrollDirection == Direction.HORIZONTAL) {
                goToNearestOrigin();
            }
            mCurrentScrollDirection = Direction.NONE;
        }

        return val;
    }

    private void goToNearestOrigin(){
        float leftDays = Math.round(mCurrentOrigin.x / (mWidthPerDay + mColumnGap));

        int nearestOrigin = (int) (mCurrentOrigin.x - leftDays * (mWidthPerDay + mColumnGap));

        if (nearestOrigin != 0) {
            // stop current animation
            mScroller.forceFinished(true);
            // snap to date
            mScroller.startScroll((int) mCurrentOrigin.x, (int) mCurrentOrigin.y, -nearestOrigin, 0, 50);
            ViewCompat.postInvalidateOnAnimation(WeekView.this);
        }
        // reset scrolling and fling direction
        mCurrentScrollDirection = mCurrentFlingDirection = Direction.NONE;
    }


    @Override
    public void computeScroll() {
        super.computeScroll();

        if (mScroller.isFinished()) {
            if (mCurrentFlingDirection != Direction.NONE) {
                // snap to day after fling is finished
                goToNearestOrigin();
            }
        } else {
            if (mScroller.computeScrollOffset()) {
                mCurrentOrigin.y = mScroller.getCurrY();
                mCurrentOrigin.x = mScroller.getCurrX();

                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
    }


    /////////////////////////////////////////////////////////////////
    //
    //      Public methods.
    //
    /////////////////////////////////////////////////////////////////

    /**
     * Show today on the week view.
     */
    public void goToToday() {
        Calendar today = Calendar.getInstance();
        goToDate(today);
    }

    /**
     * Show a specific day on the week view.
     * @param date The date to show.
     */
    public void goToDate(Calendar date) {
        mScroller.forceFinished(true);
        mCurrentScrollDirection = mCurrentFlingDirection = Direction.NONE;

        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        if(mAreDimensionsInvalid) {
            mScrollToDay = date;
            return;
        }

        mRefreshEvents = true;

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        long day = 1000L * 60L * 60L * 24L;
        long dateInMillis = date.getTimeInMillis() + date.getTimeZone().getOffset(date.getTimeInMillis());
        long todayInMillis = today.getTimeInMillis() + today.getTimeZone().getOffset(today.getTimeInMillis());
        long dateDifference = (dateInMillis/day) - (todayInMillis/day);
        mCurrentOrigin.x = - dateDifference * (mWidthPerDay + mColumnGap);
        invalidate();
    }

    /**
     * Refreshes the view and loads the events again.
     */
    public void notifyDatasetChanged(){
        mRefreshEvents = true;
        invalidate();
    }

    /**
     * Vertically scroll to a specific hour in the week view.
     * @param hour The hour to scroll to in 24-hour format. Supported values are 0-24.
     */
    public void goToHour(double hour){
        if (mAreDimensionsInvalid) {
            mScrollToHour = hour;
            return;
        }

        int verticalOffset = 0;
        if (hour > 24)
            verticalOffset = mHourHeight * 24;
        else if (hour > 0)
            verticalOffset = (int) (mHourHeight * hour);

        if (verticalOffset > mHourHeight * 24 - getHeight() + mHeaderTextHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom)
            verticalOffset = (int)(mHourHeight * 24 - getHeight() + mHeaderTextHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom);

        mCurrentOrigin.y = -verticalOffset;
        invalidate();
    }

    /**
     * Get the first hour that is visible on the screen.
     * @return The first hour that is visible.
     */
    public double getFirstVisibleHour(){
        return -mCurrentOrigin.y / mHourHeight;
    }



    /////////////////////////////////////////////////////////////////
    //
    //      Interfaces.
    //
    /////////////////////////////////////////////////////////////////

    public interface EventClickListener {
        /**
         * Triggered when clicked on one existing event
         * @param event: event clicked.
         * @param eventRect: view containing the clicked event.
         */
        public void onEventClick(WeekViewEvent event, RectF eventRect);
    }

    public interface MonthChangeListener {
        /**
         * Very important interface, it's the base to load events in the calendar.
         * This method is called three times: once to load the previous month, once to load the next month and once to load the current month.<br/>
         * <strong>That's why you can have three times the same event at the same place if you mess up with the configuration</strong>
         * @param newYear: year of the events required by the view.
         * @param newMonth: month of the events required by the view <br/><strong>1 based (not like JAVA API) --> January = 1 and December = 12</strong>.
         * @return a list of the events happening <strong>during the specified month</strong>.
         */
        public List<WeekViewEvent> onMonthChange(int newYear, int newMonth);
    }

    public interface EventLongPressListener {
        /**
         * Similar to {@link com.alamkanak.weekview.WeekView.EventClickListener} but with a long press.
         * @param event: event clicked.
         * @param eventRect: view containing the clicked event.
         */
        public void onEventLongPress(WeekViewEvent event, RectF eventRect);
    }

    public interface EmptyViewClickListener {
        /**
         * Triggered when the users clicks on a empty space of the calendar.
         * @param time: {@link Calendar} object set with the date and time of the clicked position on the view.
         */
        public void onEmptyViewClicked(Calendar time);
    }

    public interface EmptyViewLongPressListener {
        /**
         * Similar to {@link com.alamkanak.weekview.WeekView.EmptyViewClickListener} but with long press.
         * @param time: {@link Calendar} object set with the date and time of the long pressed position on the view.
         */
        public void onEmptyViewLongPress(Calendar time);
    }

    public interface ScrollListener {
        /**
         * Called when the first visible day has changed.
         *
         * (this will also be called during the first draw of the weekview)
         * @param newFirstVisibleDay The new first visible day
         * @param oldFirstVisibleDay The old first visible day (is null on the first call).
         */
        public void onFirstVisibleDayChanged(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay);
    }


    /////////////////////////////////////////////////////////////////
    //
    //      Helper methods.
    //
    /////////////////////////////////////////////////////////////////

    /**
     * Checks if an integer array contains a particular value.
     * @param list The haystack.
     * @param value The needle.
     * @return True if the array contains the value. Otherwise returns false.
     */
    private boolean containsValue(int[] list, int value) {
        for (int i = 0; i < list.length; i++){
            if (list[i] == value)
                return true;
        }
        return false;
    }

    /**
     * Checks if two times are on the same day.
     * @param dayOne The first day.
     * @param dayTwo The second day.
     * @return Whether the times are on the same day.
     */
    private boolean isSameDay(Calendar dayOne, Calendar dayTwo) {
        return dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR) && dayOne.get(Calendar.DAY_OF_YEAR) == dayTwo.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Returns a calendar instance at the start of this day
     * @return the calendar instance
     */
    private Calendar today(){
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today;
    }

}

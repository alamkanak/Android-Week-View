package com.alamkanak.weekview

import android.content.Context
import android.graphics.*
import android.os.Build
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.text.*
import android.text.format.DateFormat
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.widget.OverScroller
import com.alamkanak.weekview.WeekViewUtil.isSameDay
import com.alamkanak.weekview.WeekViewUtil.today
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://alamkanak.github.io/
 */
class WeekView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private var timeTextPaint: Paint? = null
    private var timeTextWidth: Float = 0.toFloat()
    private var timeTextHeight: Float = 0.toFloat()
    private var headerTextPaint: Paint? = null
    private var headerTextHeight: Float = 0.toFloat()
    private var headerHeight: Float = 0.toFloat()
    private var gestureDetector: GestureDetectorCompat? = null
    private var scroller: OverScroller? = null
    private val currentOrigin = PointF(0f, 0f)
    private var currentScrollDirection = Direction.NONE
    private var headerBackgroundPaint: Paint? = null
    private var widthPerDay: Float = 0.toFloat()
    private var dayBackgroundPaint: Paint? = null
    private var hourSeparatorPaint: Paint? = null
    private var headerMarginBottom: Float = 0.toFloat()
    private var todayBackgroundPaint: Paint? = null
    private var futureBackgroundPaint: Paint? = null
    private var pastBackgroundPaint: Paint? = null
    private var futureWeekendBackgroundPaint: Paint? = null
    private var pastWeekendBackgroundPaint: Paint? = null
    private var nowLinePaint: Paint? = null
    private var todayHeaderTextPaint: Paint? = null
    private var eventBackgroundPaint: Paint? = null
    private var headerColumnWidth: Float = 0f
    private var eventRects: MutableList<EventRect>? = null
    private var previousPeriodEvents: List<WeekViewEvent>? = null
    private var currentPeriodEvents: List<WeekViewEvent>? = null
    private var nextPeriodEvents: List<WeekViewEvent>? = null
    private var eventTextPaint: TextPaint? = null
    private var headerColumnBackgroundPaint: Paint? = null
    private var fetchedPeriod = -1 // the middle period the calendar has fetched.
    private var refreshEvents = false
    private var currentFlingDirection = Direction.NONE
    private var scaleDetector: ScaleGestureDetector? = null
    private var isZooming: Boolean = false
    /**
     * Returns the first visible day in the week view.
     * @return The first visible day in the week view.
     */
    var firstVisibleDay: Calendar? = null
        private set
    /**
     * Returns the last visible day in the week view.
     * @return The last visible day in the week view.
     */
    var lastVisibleDay: Calendar? = null
        private set
    var isShowFirstDayOfWeekFirst = false
    private var mDefaultEventColor: Int = 0
    private var mMinimumFlingVelocity = 0
    private var mScaledTouchSlop = 0
    // Attributes and their default values.
    private var mHourHeight = 50
    private var mNewHourHeight = -1
    private var mMinHourHeight = 0 //no minimum specified (will be dynamic, based on screen)
    private var mEffectiveMinHourHeight = mMinHourHeight //compensates for the fact that you can't keep zooming out.
    private var mMaxHourHeight = 250
    private var mColumnGap = 10
    private var mFirstDayOfWeek = Calendar.MONDAY
    private var mTextSize = 12
    private var mHeaderColumnPadding = 10
    private var mHeaderColumnTextColor = Color.BLACK
    private var mNumberOfVisibleDays = 3
    private var mHeaderRowPadding = 10
    private var mHeaderRowBackgroundColor = Color.WHITE
    private var mDayBackgroundColor = Color.rgb(245, 245, 245)
    private var mPastBackgroundColor = Color.rgb(227, 227, 227)
    private var mFutureBackgroundColor = Color.rgb(245, 245, 245)
    private var mPastWeekendBackgroundColor = 0
    private var mFutureWeekendBackgroundColor = 0
    private var mNowLineColor = Color.rgb(102, 102, 102)
    private var mNowLineThickness = 5
    private var mHourSeparatorColor = Color.rgb(230, 230, 230)
    private var mTodayBackgroundColor = Color.rgb(239, 247, 254)
    private var mHourSeparatorHeight = 2
    private var mTodayHeaderTextColor = Color.rgb(39, 137, 228)
    private var mEventTextSize = 12
    private var mEventTextColor = Color.BLACK
    private var mEventPadding = 8
    private var mHeaderColumnBackgroundColor = Color.WHITE
    private var mIsFirstDraw = true
    private var mAreDimensionsInvalid = true
    @Deprecated("")
    private var mDayNameLength = LENGTH_LONG
    private var mOverlappingEventGap = 0
    private var mEventMarginVertical = 0
    /**
     * Get the scrolling speed factor in horizontal direction.
     * @return The speed factor in horizontal direction.
     */
    /**
     * Sets the speed for horizontal scrolling.
     * @param xScrollingSpeed The new horizontal scrolling speed.
     */
    var xScrollingSpeed = 1f
    private var mScrollToDay: Calendar? = null
    private var mScrollToHour = -1.0
    /**
     * Set corner radius for event rect.
     *
     * @param eventCornerRadius the radius in px.
     */
    var eventCornerRadius = 0
    private var mShowDistinctWeekendColor = false
    private var mShowNowLine = false
    private var mShowDistinctPastFutureColor = false
    /**
     * Get whether the week view should fling horizontally.
     * @return True if the week view has horizontal fling enabled.
     */
    /**
     * Set whether the week view should fling horizontally.
     * @return True if it should have horizontal fling enabled.
     */
    var isHorizontalFlingEnabled = true
    /**
     * Get whether the week view should fling vertically.
     * @return True if the week view has vertical fling enabled.
     */
    /**
     * Set whether the week view should fling vertically.
     * @return True if it should have vertical fling enabled.
     */
    var isVerticalFlingEnabled = true
    /**
     * Get the height of AllDay-events.
     * @return Height of AllDay-events.
     */
    /**
     * Set the height of AllDay-events.
     */
    var allDayEventHeight = 100
    /**
     * Get scroll duration
     * @return scroll duration
     */
    /**
     * Set the scroll duration
     */
    var scrollDuration = 250

    // Listeners.
    var eventClickListener: EventClickListener? = null
        private set
    var eventLongPressListener: EventLongPressListener? = null
    /**
     * Get event loader in the week view. Event loaders define the  interval after which the events
     * are loaded in week view. For a MonthLoader events are loaded for every month. You can define
     * your custom event loader by extending WeekViewLoader.
     * @return The event loader.
     */
    /**
     * Set event loader in the week view. For example, a MonthLoader. Event loaders define the
     * interval after which the events are loaded in week view. For a MonthLoader events are loaded
     * for every month. You can define your custom event loader by extending WeekViewLoader.
     * @param loader The event loader.
     */
    var weekViewLoader: WeekViewLoader? = null
    var emptyViewClickListener: EmptyViewClickListener? = null
    var emptyViewLongPressListener: EmptyViewLongPressListener? = null
    private var mDateTimeInterpreter: DateTimeInterpreter? = null
    var scrollListener: ScrollListener? = null

    private val mGestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            goToNearestOrigin()
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            // Check if view is zoomed.
            if (isZooming)
                return true

            when (currentScrollDirection) {
                WeekView.Direction.NONE -> {
                    // Allow scrolling only in one direction.
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        if (distanceX > 0) {
                            currentScrollDirection = Direction.LEFT
                        } else {
                            currentScrollDirection = Direction.RIGHT
                        }
                    } else {
                        currentScrollDirection = Direction.VERTICAL
                    }
                }
                WeekView.Direction.LEFT -> {
                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && distanceX < -mScaledTouchSlop) {
                        currentScrollDirection = Direction.RIGHT
                    }
                }
                WeekView.Direction.RIGHT -> {
                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && distanceX > mScaledTouchSlop) {
                        currentScrollDirection = Direction.LEFT
                    }
                }
            }

            // Calculate the new origin after scroll.
            when (currentScrollDirection) {
                WeekView.Direction.LEFT, WeekView.Direction.RIGHT -> {
                    currentOrigin.x -= distanceX * xScrollingSpeed
                    ViewCompat.postInvalidateOnAnimation(this@WeekView)
                }
                WeekView.Direction.VERTICAL -> {
                    currentOrigin.y -= distanceY
                    ViewCompat.postInvalidateOnAnimation(this@WeekView)
                }
            }
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (isZooming)
                return true

            if (currentFlingDirection == Direction.LEFT && !isHorizontalFlingEnabled ||
                    currentFlingDirection == Direction.RIGHT && !isHorizontalFlingEnabled ||
                    currentFlingDirection == Direction.VERTICAL && !isVerticalFlingEnabled) {
                return true
            }

            scroller!!.forceFinished(true)

            currentFlingDirection = currentScrollDirection
            when (currentFlingDirection) {
                WeekView.Direction.LEFT, WeekView.Direction.RIGHT -> scroller!!.fling(currentOrigin.x.toInt(), currentOrigin.y.toInt(), (velocityX * xScrollingSpeed).toInt(), 0, Integer.MIN_VALUE, Integer.MAX_VALUE, (-((mHourHeight * 24).toFloat() + headerHeight + (mHeaderRowPadding * 2).toFloat() + headerMarginBottom + timeTextHeight / 2 - height)).toInt(), 0)
                WeekView.Direction.VERTICAL -> scroller!!.fling(currentOrigin.x.toInt(), currentOrigin.y.toInt(), 0, velocityY.toInt(), Integer.MIN_VALUE, Integer.MAX_VALUE, (-((mHourHeight * 24).toFloat() + headerHeight + (mHeaderRowPadding * 2).toFloat() + headerMarginBottom + timeTextHeight / 2 - height)).toInt(), 0)
            }

            ViewCompat.postInvalidateOnAnimation(this@WeekView)
            return true
        }


        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            // If the tap was on an event then trigger the callback.
            if (eventRects != null && eventClickListener != null) {
                val reversedEventRects = eventRects
                Collections.reverse(reversedEventRects!!)
                for (event in reversedEventRects) {
                    if (event.rectF != null && e.x > event.rectF!!.left && e.x < event.rectF!!.right && e.y > event.rectF!!.top && e.y < event.rectF!!.bottom) {
                        eventClickListener!!.onEventClick(event.originalEvent, event.rectF)
                        playSoundEffect(SoundEffectConstants.CLICK)
                        return super.onSingleTapConfirmed(e)
                    }
                }
            }

            // If the tap was on in an empty space, then trigger the callback.
            if (emptyViewClickListener != null && e.x > headerColumnWidth && e.y > headerHeight + (mHeaderRowPadding * 2).toFloat() + headerMarginBottom) {
                val selectedTime = getTimeFromPoint(e.x, e.y)
                if (selectedTime != null) {
                    playSoundEffect(SoundEffectConstants.CLICK)
                    emptyViewClickListener!!.onEmptyViewClicked(selectedTime)
                }
            }

            return super.onSingleTapConfirmed(e)
        }

        override fun onLongPress(e: MotionEvent) {
            super.onLongPress(e)

            if (eventLongPressListener != null && eventRects != null) {
                val reversedEventRects = eventRects
                Collections.reverse(reversedEventRects!!)
                for (event in reversedEventRects) {
                    if (event.rectF != null && e.x > event.rectF!!.left && e.x < event.rectF!!.right && e.y > event.rectF!!.top && e.y < event.rectF!!.bottom) {
                        eventLongPressListener!!.onEventLongPress(event.originalEvent, event.rectF)
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        return
                    }
                }
            }

            // If the tap was on in an empty space, then trigger the callback.
            if (emptyViewLongPressListener != null && e.x > headerColumnWidth && e.y > headerHeight + (mHeaderRowPadding * 2).toFloat() + headerMarginBottom) {
                val selectedTime = getTimeFromPoint(e.x, e.y)
                if (selectedTime != null) {
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    emptyViewLongPressListener!!.onEmptyViewLongPress(selectedTime)
                }
            }
        }
    }

    var monthChangeListener: MonthLoader.MonthChangeListener?
        get() = if (weekViewLoader is MonthLoader) (weekViewLoader as MonthLoader).onMonthChangeListener else null
        set(monthChangeListener) {
            this.weekViewLoader = MonthLoader(monthChangeListener)
        }

    /**
     * Get the interpreter which provides the text to show in the header column and the header row.
     * @return The date, time interpreter.
     */
    /**
     * Set the interpreter which provides the text to show in the header column and the header row.
     * @param dateTimeInterpreter The date, time interpreter.
     */
    // Refresh time column width.
    var dateTimeInterpreter: DateTimeInterpreter
        get() {
            if (mDateTimeInterpreter == null) {
                mDateTimeInterpreter = object : DateTimeInterpreter {
                    override fun interpretDate(date: Calendar): String {
                        try {
                            val sdf = if (mDayNameLength == LENGTH_SHORT) SimpleDateFormat("EEEEE M/dd", Locale.getDefault()) else SimpleDateFormat("EEE M/dd", Locale.getDefault())
                            return sdf.format(date.time).toUpperCase()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            return ""
                        }

                    }

                    override fun interpretTime(hour: Int): String {
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, 0)

                        try {
                            val sdf = if (DateFormat.is24HourFormat(context)) SimpleDateFormat("HH:mm", Locale.getDefault()) else SimpleDateFormat("hh a", Locale.getDefault())
                            return sdf.format(calendar.time)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            return ""
                        }

                    }
                }
            }
            return mDateTimeInterpreter!!
        }
        set(dateTimeInterpreter) {
            this.mDateTimeInterpreter = dateTimeInterpreter
            initTextTimeWidth()
        }


    /**
     * Get the number of visible days in a week.
     * @return The number of visible days in a week.
     */
    /**
     * Set the number of visible days in a week.
     * @param numberOfVisibleDays The number of visible days in a week.
     */
    var numberOfVisibleDays: Int
        get() = mNumberOfVisibleDays
        set(numberOfVisibleDays) {
            this.mNumberOfVisibleDays = numberOfVisibleDays
            currentOrigin.x = 0f
            currentOrigin.y = 0f
            invalidate()
        }

    var hourHeight: Int
        get() = mHourHeight
        set(hourHeight) {
            mNewHourHeight = hourHeight
            invalidate()
        }

    var columnGap: Int
        get() = mColumnGap
        set(columnGap) {
            mColumnGap = columnGap
            invalidate()
        }

    /**
     * Set the first day of the week. First day of the week is used only when the week view is first
     * drawn. It does not of any effect after user starts scrolling horizontally.
     *
     *
     * **Note:** This method will only work if the week view is set to display more than 6 days at
     * once.
     *
     * @param firstDayOfWeek The supported values are [java.util.Calendar.SUNDAY],
     * [java.util.Calendar.MONDAY], [java.util.Calendar.TUESDAY],
     * [java.util.Calendar.WEDNESDAY], [java.util.Calendar.THURSDAY],
     * [java.util.Calendar.FRIDAY].
     */
    var firstDayOfWeek: Int
        get() = mFirstDayOfWeek
        set(firstDayOfWeek) {
            mFirstDayOfWeek = firstDayOfWeek
            invalidate()
        }

    var textSize: Int
        get() = mTextSize
        set(textSize) {
            mTextSize = textSize
            todayHeaderTextPaint!!.textSize = mTextSize.toFloat()
            headerTextPaint!!.textSize = mTextSize.toFloat()
            timeTextPaint!!.textSize = mTextSize.toFloat()
            invalidate()
        }

    var headerColumnPadding: Int
        get() = mHeaderColumnPadding
        set(headerColumnPadding) {
            mHeaderColumnPadding = headerColumnPadding
            invalidate()
        }

    var headerColumnTextColor: Int
        get() = mHeaderColumnTextColor
        set(headerColumnTextColor) {
            mHeaderColumnTextColor = headerColumnTextColor
            headerTextPaint!!.color = mHeaderColumnTextColor
            timeTextPaint!!.color = mHeaderColumnTextColor
            invalidate()
        }

    var headerRowPadding: Int
        get() = mHeaderRowPadding
        set(headerRowPadding) {
            mHeaderRowPadding = headerRowPadding
            invalidate()
        }

    var headerRowBackgroundColor: Int
        get() = mHeaderRowBackgroundColor
        set(headerRowBackgroundColor) {
            mHeaderRowBackgroundColor = headerRowBackgroundColor
            headerBackgroundPaint!!.color = mHeaderRowBackgroundColor
            invalidate()
        }

    var dayBackgroundColor: Int
        get() = mDayBackgroundColor
        set(dayBackgroundColor) {
            mDayBackgroundColor = dayBackgroundColor
            dayBackgroundPaint!!.color = mDayBackgroundColor
            invalidate()
        }

    var hourSeparatorColor: Int
        get() = mHourSeparatorColor
        set(hourSeparatorColor) {
            mHourSeparatorColor = hourSeparatorColor
            hourSeparatorPaint!!.color = mHourSeparatorColor
            invalidate()
        }

    var todayBackgroundColor: Int
        get() = mTodayBackgroundColor
        set(todayBackgroundColor) {
            mTodayBackgroundColor = todayBackgroundColor
            todayBackgroundPaint!!.color = mTodayBackgroundColor
            invalidate()
        }

    var hourSeparatorHeight: Int
        get() = mHourSeparatorHeight
        set(hourSeparatorHeight) {
            mHourSeparatorHeight = hourSeparatorHeight
            hourSeparatorPaint!!.strokeWidth = mHourSeparatorHeight.toFloat()
            invalidate()
        }

    var todayHeaderTextColor: Int
        get() = mTodayHeaderTextColor
        set(todayHeaderTextColor) {
            mTodayHeaderTextColor = todayHeaderTextColor
            todayHeaderTextPaint!!.color = mTodayHeaderTextColor
            invalidate()
        }

    var eventTextSize: Int
        get() = mEventTextSize
        set(eventTextSize) {
            mEventTextSize = eventTextSize
            eventTextPaint!!.textSize = mEventTextSize.toFloat()
            invalidate()
        }

    var eventTextColor: Int
        get() = mEventTextColor
        set(eventTextColor) {
            mEventTextColor = eventTextColor
            eventTextPaint!!.color = mEventTextColor
            invalidate()
        }

    var eventPadding: Int
        get() = mEventPadding
        set(eventPadding) {
            mEventPadding = eventPadding
            invalidate()
        }

    var headerColumnBackgroundColor: Int
        get() = mHeaderColumnBackgroundColor
        set(headerColumnBackgroundColor) {
            mHeaderColumnBackgroundColor = headerColumnBackgroundColor
            headerColumnBackgroundPaint!!.color = mHeaderColumnBackgroundColor
            invalidate()
        }

    var defaultEventColor: Int
        get() = mDefaultEventColor
        set(defaultEventColor) {
            mDefaultEventColor = defaultEventColor
            invalidate()
        }

    /**
     * **Note:** Use [.setDateTimeInterpreter] and
     * [.getDateTimeInterpreter] instead.
     * @return Either long or short day name is being used.
     */
    /**
     * Set the length of the day name displayed in the header row. Example of short day names is
     * 'M' for 'Monday' and example of long day names is 'Mon' for 'Monday'.
     *
     *
     * **Note:** Use [.setDateTimeInterpreter] instead.
     *
     * @param length Supported values are [com.alamkanak.weekview.WeekView.LENGTH_SHORT] and
     * [com.alamkanak.weekview.WeekView.LENGTH_LONG].
     */
    var dayNameLength: Int
        @Deprecated("")
        get() = mDayNameLength
        @Deprecated("")
        set(length) {
            if (length != LENGTH_LONG && length != LENGTH_SHORT) {
                throw IllegalArgumentException("length parameter must be either LENGTH_LONG or LENGTH_SHORT")
            }
            this.mDayNameLength = length
        }

    /**
     * Set the gap between overlapping events.
     * @param overlappingEventGap The gap between overlapping events.
     */
    var overlappingEventGap: Int
        get() = mOverlappingEventGap
        set(overlappingEventGap) {
            this.mOverlappingEventGap = overlappingEventGap
            invalidate()
        }

    /**
     * Set the top and bottom margin of the event. The event will release this margin from the top
     * and bottom edge. This margin is useful for differentiation consecutive events.
     * @param eventMarginVertical The top and bottom margin.
     */
    var eventMarginVertical: Int
        get() = mEventMarginVertical
        set(eventMarginVertical) {
            this.mEventMarginVertical = eventMarginVertical
            invalidate()
        }

    /**
     * Whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     * @return True if weekends should have different background colors.
     */
    /**
     * Set whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     * @param showDistinctWeekendColor True if weekends should have different background colors.
     */
    var isShowDistinctWeekendColor: Boolean
        get() = mShowDistinctWeekendColor
        set(showDistinctWeekendColor) {
            this.mShowDistinctWeekendColor = showDistinctWeekendColor
            invalidate()
        }

    /**
     * Whether past and future days should have two different background colors. The past and
     * future day colors are defined by the attributes `futureBackgroundColor` and
     * `pastBackgroundColor`.
     * @return True if past and future days should have two different background colors.
     */
    /**
     * Set whether weekends should have a background color different from the normal day background
     * color. The past and future day colors are defined by the attributes `futureBackgroundColor`
     * and `pastBackgroundColor`.
     * @param showDistinctPastFutureColor True if past and future should have two different
     * background colors.
     */
    var isShowDistinctPastFutureColor: Boolean
        get() = mShowDistinctPastFutureColor
        set(showDistinctPastFutureColor) {
            this.mShowDistinctPastFutureColor = showDistinctPastFutureColor
            invalidate()
        }

    /**
     * Get whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineThickness`.
     * @return True if "now" line should be displayed.
     */
    /**
     * Set whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineThickness`.
     * @param showNowLine True if "now" line should be displayed.
     */
    var isShowNowLine: Boolean
        get() = mShowNowLine
        set(showNowLine) {
            this.mShowNowLine = showNowLine
            invalidate()
        }

    /**
     * Get the "now" line color.
     * @return The color of the "now" line.
     */
    /**
     * Set the "now" line color.
     * @param nowLineColor The color of the "now" line.
     */
    var nowLineColor: Int
        get() = mNowLineColor
        set(nowLineColor) {
            this.mNowLineColor = nowLineColor
            invalidate()
        }

    /**
     * Get the "now" line thickness.
     * @return The thickness of the "now" line.
     */
    /**
     * Set the "now" line thickness.
     * @param nowLineThickness The thickness of the "now" line.
     */
    var nowLineThickness: Int
        get() = mNowLineThickness
        set(nowLineThickness) {
            this.mNowLineThickness = nowLineThickness
            invalidate()
        }

    /**
     * Get the first hour that is visible on the screen.
     * @return The first hour that is visible.
     */
    val firstVisibleHour: Double
        get() = (-currentOrigin.y / mHourHeight).toDouble()

    private enum class Direction {
        NONE, LEFT, RIGHT, VERTICAL
    }

    init {

        // Get the attribute values (if any).
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0)
        try {
            mFirstDayOfWeek = a.getInteger(R.styleable.WeekView_firstDayOfWeek, mFirstDayOfWeek)
            mHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourHeight, mHourHeight)
            mMinHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_minHourHeight, mMinHourHeight)
            mEffectiveMinHourHeight = mMinHourHeight
            mMaxHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_maxHourHeight, mMaxHourHeight)
            mTextSize = a.getDimensionPixelSize(R.styleable.WeekView_textSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize.toFloat(), context.resources.displayMetrics).toInt())
            mHeaderColumnPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerColumnPadding, mHeaderColumnPadding)
            mColumnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, mColumnGap)
            mHeaderColumnTextColor = a.getColor(R.styleable.WeekView_headerColumnTextColor, mHeaderColumnTextColor)
            mNumberOfVisibleDays = a.getInteger(R.styleable.WeekView_noOfVisibleDays, mNumberOfVisibleDays)
            isShowFirstDayOfWeekFirst = a.getBoolean(R.styleable.WeekView_showFirstDayOfWeekFirst, isShowFirstDayOfWeekFirst)
            mHeaderRowPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerRowPadding, mHeaderRowPadding)
            mHeaderRowBackgroundColor = a.getColor(R.styleable.WeekView_headerRowBackgroundColor, mHeaderRowBackgroundColor)
            mDayBackgroundColor = a.getColor(R.styleable.WeekView_dayBackgroundColor, mDayBackgroundColor)
            mFutureBackgroundColor = a.getColor(R.styleable.WeekView_futureBackgroundColor, mFutureBackgroundColor)
            mPastBackgroundColor = a.getColor(R.styleable.WeekView_pastBackgroundColor, mPastBackgroundColor)
            mFutureWeekendBackgroundColor = a.getColor(R.styleable.WeekView_futureWeekendBackgroundColor, mFutureBackgroundColor) // If not set, use the same color as in the week
            mPastWeekendBackgroundColor = a.getColor(R.styleable.WeekView_pastWeekendBackgroundColor, mPastBackgroundColor)
            mNowLineColor = a.getColor(R.styleable.WeekView_nowLineColor, mNowLineColor)
            mNowLineThickness = a.getDimensionPixelSize(R.styleable.WeekView_nowLineThickness, mNowLineThickness)
            mHourSeparatorColor = a.getColor(R.styleable.WeekView_hourSeparatorColor, mHourSeparatorColor)
            mTodayBackgroundColor = a.getColor(R.styleable.WeekView_todayBackgroundColor, mTodayBackgroundColor)
            mHourSeparatorHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourSeparatorHeight, mHourSeparatorHeight)
            mTodayHeaderTextColor = a.getColor(R.styleable.WeekView_todayHeaderTextColor, mTodayHeaderTextColor)
            mEventTextSize = a.getDimensionPixelSize(R.styleable.WeekView_eventTextSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mEventTextSize.toFloat(), context.resources.displayMetrics).toInt())
            mEventTextColor = a.getColor(R.styleable.WeekView_eventTextColor, mEventTextColor)
            mEventPadding = a.getDimensionPixelSize(R.styleable.WeekView_eventPadding, mEventPadding)
            mHeaderColumnBackgroundColor = a.getColor(R.styleable.WeekView_headerColumnBackground, mHeaderColumnBackgroundColor)
            mDayNameLength = a.getInteger(R.styleable.WeekView_dayNameLength, mDayNameLength)
            mOverlappingEventGap = a.getDimensionPixelSize(R.styleable.WeekView_overlappingEventGap, mOverlappingEventGap)
            mEventMarginVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventMarginVertical, mEventMarginVertical)
            xScrollingSpeed = a.getFloat(R.styleable.WeekView_xScrollingSpeed, xScrollingSpeed)
            eventCornerRadius = a.getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, eventCornerRadius)
            mShowDistinctPastFutureColor = a.getBoolean(R.styleable.WeekView_showDistinctPastFutureColor, mShowDistinctPastFutureColor)
            mShowDistinctWeekendColor = a.getBoolean(R.styleable.WeekView_showDistinctWeekendColor, mShowDistinctWeekendColor)
            mShowNowLine = a.getBoolean(R.styleable.WeekView_showNowLine, mShowNowLine)
            isHorizontalFlingEnabled = a.getBoolean(R.styleable.WeekView_horizontalFlingEnabled, isHorizontalFlingEnabled)
            isVerticalFlingEnabled = a.getBoolean(R.styleable.WeekView_verticalFlingEnabled, isVerticalFlingEnabled)
            allDayEventHeight = a.getDimensionPixelSize(R.styleable.WeekView_allDayEventHeight, allDayEventHeight)
            scrollDuration = a.getInt(R.styleable.WeekView_scrollDuration, scrollDuration)
        } finally {
            a.recycle()
        }

        init()
    }// Hold references.

    private fun init() {
        // Scrolling initialization.
        gestureDetector = GestureDetectorCompat(context, mGestureListener)
        scroller = OverScroller(context, FastOutLinearInInterpolator())

        mMinimumFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
        mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

        // Measure settings for time column.
        timeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        timeTextPaint!!.textAlign = Paint.Align.RIGHT
        timeTextPaint!!.textSize = mTextSize.toFloat()
        timeTextPaint!!.color = mHeaderColumnTextColor
        val rect = Rect()
        timeTextPaint!!.getTextBounds("00 PM", 0, "00 PM".length, rect)
        timeTextHeight = rect.height().toFloat()
        headerMarginBottom = timeTextHeight / 2
        initTextTimeWidth()

        // Measure settings for header row.
        headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        headerTextPaint!!.color = mHeaderColumnTextColor
        headerTextPaint!!.textAlign = Paint.Align.CENTER
        headerTextPaint!!.textSize = mTextSize.toFloat()
        headerTextPaint!!.getTextBounds("00 PM", 0, "00 PM".length, rect)
        headerTextHeight = rect.height().toFloat()
        headerTextPaint!!.typeface = Typeface.DEFAULT_BOLD

        // Prepare header background paint.
        headerBackgroundPaint = Paint()
        headerBackgroundPaint!!.color = mHeaderRowBackgroundColor

        // Prepare day background color paint.
        dayBackgroundPaint = Paint()
        dayBackgroundPaint!!.color = mDayBackgroundColor
        futureBackgroundPaint = Paint()
        futureBackgroundPaint!!.color = mFutureBackgroundColor
        pastBackgroundPaint = Paint()
        pastBackgroundPaint!!.color = mPastBackgroundColor
        futureWeekendBackgroundPaint = Paint()
        futureWeekendBackgroundPaint!!.color = mFutureWeekendBackgroundColor
        pastWeekendBackgroundPaint = Paint()
        pastWeekendBackgroundPaint!!.color = mPastWeekendBackgroundColor

        // Prepare hour separator color paint.
        hourSeparatorPaint = Paint()
        hourSeparatorPaint!!.style = Paint.Style.STROKE
        hourSeparatorPaint!!.strokeWidth = mHourSeparatorHeight.toFloat()
        hourSeparatorPaint!!.color = mHourSeparatorColor

        // Prepare the "now" line color paint
        nowLinePaint = Paint()
        nowLinePaint!!.strokeWidth = mNowLineThickness.toFloat()
        nowLinePaint!!.color = mNowLineColor

        // Prepare today background color paint.
        todayBackgroundPaint = Paint()
        todayBackgroundPaint!!.color = mTodayBackgroundColor

        // Prepare today header text color paint.
        todayHeaderTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        todayHeaderTextPaint!!.textAlign = Paint.Align.CENTER
        todayHeaderTextPaint!!.textSize = mTextSize.toFloat()
        todayHeaderTextPaint!!.typeface = Typeface.DEFAULT_BOLD
        todayHeaderTextPaint!!.color = mTodayHeaderTextColor

        // Prepare event background color.
        eventBackgroundPaint = Paint()
        eventBackgroundPaint!!.color = Color.rgb(174, 208, 238)

        // Prepare header column background color.
        headerColumnBackgroundPaint = Paint()
        headerColumnBackgroundPaint!!.color = mHeaderColumnBackgroundColor

        // Prepare event text size and color.
        eventTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG)
        eventTextPaint!!.style = Paint.Style.FILL
        eventTextPaint!!.color = mEventTextColor
        eventTextPaint!!.textSize = mEventTextSize.toFloat()

        // Set default event color.
        mDefaultEventColor = Color.parseColor("#9fc6e7")

        scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.OnScaleGestureListener {
            override fun onScaleEnd(detector: ScaleGestureDetector) {
                isZooming = false
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                isZooming = true
                goToNearestOrigin()
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                mNewHourHeight = Math.round(mHourHeight * detector.scaleFactor)
                invalidate()
                return true
            }
        })
    }

    // fix rotation changes
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mAreDimensionsInvalid = true
    }

    /**
     * Initialize time column width. Calculate value with all possible hours (supposed widest text).
     */
    private fun initTextTimeWidth() {
        timeTextWidth = 0f
        for (i in 0..23) {
            // Measure time string and get max width.
            val time = dateTimeInterpreter.interpretTime(i)
                    ?: throw IllegalStateException("A DateTimeInterpreter must not return null time")
            timeTextWidth = Math.max(timeTextWidth, timeTextPaint!!.measureText(time))
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the header row.
        drawHeaderRowAndEvents(canvas)

        // Draw the time column and all the axes/separators.
        drawTimeColumnAndAxes(canvas)
    }

    private fun calculateHeaderHeight() {
        //Make sure the header is the right size (depends on AllDay events)
        var containsAllDayEvent = false
        if (eventRects != null && eventRects!!.size > 0) {
            for (dayNumber in 0 until mNumberOfVisibleDays) {
                val day = firstVisibleDay!!.clone() as Calendar
                day.add(Calendar.DATE, dayNumber)
                for (i in eventRects!!.indices) {

                    if (isSameDay(eventRects!![i].event.startTime, day) && eventRects!![i].event.isAllDay) {
                        containsAllDayEvent = true
                        break
                    }
                }
                if (containsAllDayEvent) {
                    break
                }
            }
        }
        if (containsAllDayEvent) {
            headerHeight = headerTextHeight + (allDayEventHeight + headerMarginBottom)
        } else {
            headerHeight = headerTextHeight
        }
    }

    private fun drawTimeColumnAndAxes(canvas: Canvas) {
        // Draw the background color for the header column.
        canvas.drawRect(0f, headerHeight + mHeaderRowPadding * 2, headerColumnWidth, height.toFloat(), headerColumnBackgroundPaint!!)

        // Clip to paint in left column only.
        canvas.clipRect(0f, headerHeight + mHeaderRowPadding * 2, headerColumnWidth, height.toFloat(), Region.Op.REPLACE)

        for (i in 0..23) {
            val top = headerHeight + (mHeaderRowPadding * 2).toFloat() + currentOrigin.y + (mHourHeight * i).toFloat() + headerMarginBottom

            // Draw the text if its y position is not outside of the visible area. The pivot point of the text is the point at the bottom-right corner.
            val time = dateTimeInterpreter.interpretTime(i)
                    ?: throw IllegalStateException("A DateTimeInterpreter must not return null time")
            if (top < height) canvas.drawText(time, timeTextWidth + mHeaderColumnPadding, top + timeTextHeight, timeTextPaint!!)
        }
    }

    private fun drawHeaderRowAndEvents(canvas: Canvas) {
        // Calculate the available width for each day.
        headerColumnWidth = timeTextWidth + mHeaderColumnPadding * 2
        widthPerDay = width.toFloat() - headerColumnWidth - (mColumnGap * (mNumberOfVisibleDays - 1)).toFloat()
        widthPerDay = widthPerDay / mNumberOfVisibleDays

        calculateHeaderHeight() //Make sure the header is the right size (depends on AllDay events)

        val today = today()

        if (mAreDimensionsInvalid) {
            mEffectiveMinHourHeight = Math.max(mMinHourHeight, ((height.toFloat() - headerHeight - (mHeaderRowPadding * 2).toFloat() - headerMarginBottom) / 24).toInt())

            mAreDimensionsInvalid = false
            if (mScrollToDay != null)
                goToDate(mScrollToDay!!)

            mAreDimensionsInvalid = false
            if (mScrollToHour >= 0)
                goToHour(mScrollToHour)

            mScrollToDay = null
            mScrollToHour = -1.0
            mAreDimensionsInvalid = false
        }
        if (mIsFirstDraw) {
            mIsFirstDraw = false

            // If the week view is being drawn for the first time, then consider the first day of the week.
            if (mNumberOfVisibleDays >= 7 && today.get(Calendar.DAY_OF_WEEK) != mFirstDayOfWeek && isShowFirstDayOfWeekFirst) {
                val difference = today.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek
                currentOrigin.x += (widthPerDay + mColumnGap) * difference
            }
        }

        // Calculate the new height due to the zooming.
        if (mNewHourHeight > 0) {
            if (mNewHourHeight < mEffectiveMinHourHeight)
                mNewHourHeight = mEffectiveMinHourHeight
            else if (mNewHourHeight > mMaxHourHeight)
                mNewHourHeight = mMaxHourHeight

            currentOrigin.y = currentOrigin.y / mHourHeight * mNewHourHeight
            mHourHeight = mNewHourHeight
            mNewHourHeight = -1
        }

        // If the new mCurrentOrigin.y is invalid, make it valid.
        if (currentOrigin.y < height.toFloat() - (mHourHeight * 24).toFloat() - headerHeight - (mHeaderRowPadding * 2).toFloat() - headerMarginBottom - timeTextHeight / 2)
            currentOrigin.y = height.toFloat() - (mHourHeight * 24).toFloat() - headerHeight - (mHeaderRowPadding * 2).toFloat() - headerMarginBottom - timeTextHeight / 2

        // Don't put an "else if" because it will trigger a glitch when completely zoomed out and
        // scrolling vertically.
        if (currentOrigin.y > 0) {
            currentOrigin.y = 0f
        }

        // Consider scroll offset.
        val leftDaysWithGaps = (-Math.ceil((currentOrigin.x / (widthPerDay + mColumnGap)).toDouble())).toInt()
        val startFromPixel = currentOrigin.x + (widthPerDay + mColumnGap) * leftDaysWithGaps +
                headerColumnWidth
        var startPixel = startFromPixel

        // Prepare to iterate for each day.
        var day = today.clone() as Calendar
        day.add(Calendar.HOUR, 6)

        // Prepare to iterate for each hour to draw the hour lines.
        var lineCount = ((height.toFloat() - headerHeight - (mHeaderRowPadding * 2).toFloat() -
                headerMarginBottom) / mHourHeight).toInt() + 1
        lineCount = lineCount * (mNumberOfVisibleDays + 1)
        val hourLines = FloatArray(lineCount * 4)

        // Clear the cache for event rectangles.
        if (eventRects != null) {
            for (eventRect in eventRects!!) {
                eventRect.rectF = null
            }
        }

        // Clip to paint events only.
        canvas.clipRect(headerColumnWidth, headerHeight + (mHeaderRowPadding * 2).toFloat() + headerMarginBottom + timeTextHeight / 2, width.toFloat(), height.toFloat(), Region.Op.REPLACE)

        // Iterate through each day.
        val oldFirstVisibleDay = firstVisibleDay
        firstVisibleDay = today.clone() as Calendar
        firstVisibleDay!!.add(Calendar.DATE, -Math.round(currentOrigin.x / (widthPerDay + mColumnGap)))
        if (firstVisibleDay != oldFirstVisibleDay && scrollListener != null) {
            scrollListener!!.onFirstVisibleDayChanged(firstVisibleDay, oldFirstVisibleDay)
        }
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + mNumberOfVisibleDays + 1) {

            // Check if the day is today.
            day = today.clone() as Calendar
            lastVisibleDay = day.clone() as Calendar
            day.add(Calendar.DATE, dayNumber - 1)
            lastVisibleDay!!.add(Calendar.DATE, dayNumber - 2)
            val sameDay = isSameDay(day, today)

            // Get more events if necessary. We want to store the events 3 months beforehand. Get
            // events only when it is the first iteration of the loop.
            if (eventRects == null || refreshEvents ||
                    dayNumber == leftDaysWithGaps + 1 && fetchedPeriod != weekViewLoader!!.toWeekViewPeriodIndex(day).toInt() &&
                    Math.abs(fetchedPeriod - weekViewLoader!!.toWeekViewPeriodIndex(day)) > 0.5) {
                getMoreEvents(day)
                refreshEvents = false
            }

            // Draw background color for each day.
            val start = if (startPixel < headerColumnWidth) headerColumnWidth else startPixel
            if (widthPerDay + startPixel - start > 0) {
                if (mShowDistinctPastFutureColor) {
                    val isWeekend = day.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || day.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                    val pastPaint = if (isWeekend && mShowDistinctWeekendColor) pastWeekendBackgroundPaint else pastBackgroundPaint
                    val futurePaint = if (isWeekend && mShowDistinctWeekendColor) futureWeekendBackgroundPaint else futureBackgroundPaint
                    val startY = headerHeight + (mHeaderRowPadding * 2).toFloat() + timeTextHeight / 2 + headerMarginBottom + currentOrigin.y

                    if (sameDay) {
                        val now = Calendar.getInstance()
                        val beforeNow = (now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0f) * mHourHeight
                        canvas.drawRect(start, startY, startPixel + widthPerDay, startY + beforeNow, pastPaint!!)
                        canvas.drawRect(start, startY + beforeNow, startPixel + widthPerDay, height.toFloat(), futurePaint!!)
                    } else if (day.before(today)) {
                        canvas.drawRect(start, startY, startPixel + widthPerDay, height.toFloat(), pastPaint!!)
                    } else {
                        canvas.drawRect(start, startY, startPixel + widthPerDay, height.toFloat(), futurePaint!!)
                    }
                } else {
                    canvas.drawRect(start, headerHeight + (mHeaderRowPadding * 2).toFloat() + timeTextHeight / 2 + headerMarginBottom, startPixel + widthPerDay, height.toFloat(), if (sameDay) todayBackgroundPaint else dayBackgroundPaint)
                }
            }

            // Prepare the separator lines for hours.
            var i = 0
            for (hourNumber in 0..23) {
                val top = headerHeight + (mHeaderRowPadding * 2).toFloat() + currentOrigin.y + (mHourHeight * hourNumber).toFloat() + timeTextHeight / 2 + headerMarginBottom
                if (top > headerHeight + (mHeaderRowPadding * 2).toFloat() + timeTextHeight / 2 + headerMarginBottom - mHourSeparatorHeight && top < height && startPixel + widthPerDay - start > 0) {
                    hourLines[i * 4] = start
                    hourLines[i * 4 + 1] = top
                    hourLines[i * 4 + 2] = startPixel + widthPerDay
                    hourLines[i * 4 + 3] = top
                    i++
                }
            }

            // Draw the lines for hours.
            canvas.drawLines(hourLines, hourSeparatorPaint!!)

            // Draw the events.
            drawEvents(day, startPixel, canvas)

            // Draw the line at the current time.
            if (mShowNowLine && sameDay) {
                val startY = headerHeight + (mHeaderRowPadding * 2).toFloat() + timeTextHeight / 2 + headerMarginBottom + currentOrigin.y
                val now = Calendar.getInstance()
                val beforeNow = (now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0f) * mHourHeight
                canvas.drawLine(start, startY + beforeNow, startPixel + widthPerDay, startY + beforeNow, nowLinePaint!!)
            }

            // In the next iteration, start from the next day.
            startPixel += widthPerDay + mColumnGap
        }

        // Hide everything in the first cell (top left corner).
        canvas.clipRect(0f, 0f, timeTextWidth + mHeaderColumnPadding * 2, headerHeight + mHeaderRowPadding * 2, Region.Op.REPLACE)
        canvas.drawRect(0f, 0f, timeTextWidth + mHeaderColumnPadding * 2, headerHeight + mHeaderRowPadding * 2, headerBackgroundPaint!!)

        // Clip to paint header row only.
        canvas.clipRect(headerColumnWidth, 0f, width.toFloat(), headerHeight + mHeaderRowPadding * 2, Region.Op.REPLACE)

        // Draw the header background.
        canvas.drawRect(0f, 0f, width.toFloat(), headerHeight + mHeaderRowPadding * 2, headerBackgroundPaint!!)

        // Draw the header row texts.
        startPixel = startFromPixel
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + mNumberOfVisibleDays + 1) {
            // Check if the day is today.
            day = today.clone() as Calendar
            day.add(Calendar.DATE, dayNumber - 1)
            val sameDay = isSameDay(day, today)

            // Draw the day labels.
            val dayLabel = dateTimeInterpreter.interpretDate(day)
                    ?: throw IllegalStateException("A DateTimeInterpreter must not return null date")
            canvas.drawText(dayLabel, startPixel + widthPerDay / 2, headerTextHeight + mHeaderRowPadding, if (sameDay) todayHeaderTextPaint else headerTextPaint)
            drawAllDayEvents(day, startPixel, canvas)
            startPixel += widthPerDay + mColumnGap
        }

    }

    /**
     * Get the time and date where the user clicked on.
     * @param x The x position of the touch event.
     * @param y The y position of the touch event.
     * @return The time and date at the clicked position.
     */
    private fun getTimeFromPoint(x: Float, y: Float): Calendar? {
        val leftDaysWithGaps = (-Math.ceil((currentOrigin.x / (widthPerDay + mColumnGap)).toDouble())).toInt()
        var startPixel = currentOrigin.x + (widthPerDay + mColumnGap) * leftDaysWithGaps +
                headerColumnWidth
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + mNumberOfVisibleDays + 1) {
            val start = if (startPixel < headerColumnWidth) headerColumnWidth else startPixel
            if (widthPerDay + startPixel - start > 0 && x > start && x < startPixel + widthPerDay) {
                val day = today()
                day.add(Calendar.DATE, dayNumber - 1)
                val pixelsFromZero = (y - currentOrigin.y - headerHeight
                        - (mHeaderRowPadding * 2).toFloat() - timeTextHeight / 2 - headerMarginBottom)
                val hour = (pixelsFromZero / mHourHeight).toInt()
                val minute = (60 * (pixelsFromZero - hour * mHourHeight) / mHourHeight).toInt()
                day.add(Calendar.HOUR, hour)
                day.set(Calendar.MINUTE, minute)
                return day
            }
            startPixel += widthPerDay + mColumnGap
        }
        return null
    }

    /**
     * Draw all the events of a particular day.
     * @param date The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas The canvas to draw upon.
     */
    private fun drawEvents(date: Calendar, startFromPixel: Float, canvas: Canvas) {
        if (eventRects != null && eventRects!!.size > 0) {
            for (i in eventRects!!.indices) {
                if (isSameDay(eventRects!![i].event.startTime, date) && !eventRects!![i].event.isAllDay) {

                    // Calculate top.
                    val top = mHourHeight.toFloat() * 24f * eventRects!![i].top / 1440 + currentOrigin.y + headerHeight + (mHeaderRowPadding * 2).toFloat() + headerMarginBottom + timeTextHeight / 2 + mEventMarginVertical.toFloat()

                    // Calculate bottom.
                    var bottom = eventRects!![i].bottom
                    bottom = mHourHeight.toFloat() * 24f * bottom / 1440 + currentOrigin.y + headerHeight + (mHeaderRowPadding * 2).toFloat() + headerMarginBottom + timeTextHeight / 2 - mEventMarginVertical

                    // Calculate left and right.
                    var left = startFromPixel + eventRects!![i].left * widthPerDay
                    if (left < startFromPixel)
                        left += mOverlappingEventGap.toFloat()
                    var right = left + eventRects!![i].width * widthPerDay
                    if (right < startFromPixel + widthPerDay)
                        right -= mOverlappingEventGap.toFloat()

                    // Draw the event and the event name on top of it.
                    if (left < right &&
                            left < width &&
                            top < height &&
                            right > headerColumnWidth &&
                            bottom > headerHeight + (mHeaderRowPadding * 2).toFloat() + timeTextHeight / 2 + headerMarginBottom) {
                        eventRects!![i].rectF = RectF(left, top, right, bottom)
                        eventBackgroundPaint!!.color = if (eventRects!![i].event.color == 0) mDefaultEventColor else eventRects!![i].event.color
                        canvas.drawRoundRect(eventRects!![i].rectF!!, eventCornerRadius.toFloat(), eventCornerRadius.toFloat(), eventBackgroundPaint!!)
                        drawEventTitle(eventRects!![i].event, eventRects!![i].rectF!!, canvas, top, left)
                    } else
                        eventRects!![i].rectF = null
                }
            }
        }
    }

    /**
     * Draw all the Allday-events of a particular day.
     * @param date The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas The canvas to draw upon.
     */
    private fun drawAllDayEvents(date: Calendar, startFromPixel: Float, canvas: Canvas) {
        if (eventRects != null && eventRects!!.size > 0) {
            for (i in eventRects!!.indices) {
                if (isSameDay(eventRects!![i].event.startTime, date) && eventRects!![i].event.isAllDay) {

                    // Calculate top.
                    val top = (mHeaderRowPadding * 2).toFloat() + headerMarginBottom + +timeTextHeight / 2 + mEventMarginVertical.toFloat()

                    // Calculate bottom.
                    val bottom = top + eventRects!![i].bottom

                    // Calculate left and right.
                    var left = startFromPixel + eventRects!![i].left * widthPerDay
                    if (left < startFromPixel)
                        left += mOverlappingEventGap.toFloat()
                    var right = left + eventRects!![i].width * widthPerDay
                    if (right < startFromPixel + widthPerDay)
                        right -= mOverlappingEventGap.toFloat()

                    // Draw the event and the event name on top of it.
                    if (left < right &&
                            left < width &&
                            top < height &&
                            right > headerColumnWidth &&
                            bottom > 0) {
                        eventRects!![i].rectF = RectF(left, top, right, bottom)
                        eventBackgroundPaint!!.color = if (eventRects!![i].event.color == 0) mDefaultEventColor else eventRects!![i].event.color
                        canvas.drawRoundRect(eventRects!![i].rectF!!, eventCornerRadius.toFloat(), eventCornerRadius.toFloat(), eventBackgroundPaint!!)
                        drawEventTitle(eventRects!![i].event, eventRects!![i].rectF!!, canvas, top, left)
                    } else
                        eventRects!![i].rectF = null
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
    private fun drawEventTitle(event: WeekViewEvent, rect: RectF, canvas: Canvas, originalTop: Float, originalLeft: Float) {
        if (rect.right - rect.left - (mEventPadding * 2).toFloat() < 0) return
        if (rect.bottom - rect.top - (mEventPadding * 2).toFloat() < 0) return

        // Prepare the name of the event.
        val bob = SpannableStringBuilder()
        if (event.name != null) {
            bob.append(event.name)
            bob.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, bob.length, 0)
            bob.append(' ')
        }

        // Prepare the location of the event.
        if (event.location != null) {
            bob.append(event.location)
        }

        val availableHeight = (rect.bottom - originalTop - (mEventPadding * 2).toFloat()).toInt()
        val availableWidth = (rect.right - originalLeft - (mEventPadding * 2).toFloat()).toInt()

        // Get text dimensions.
        var textLayout = StaticLayout(bob, eventTextPaint, availableWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)

        val lineHeight = textLayout.height / textLayout.lineCount

        if (availableHeight >= lineHeight) {
            // Calculate available number of line counts.
            var availableLineCount = availableHeight / lineHeight
            do {
                // Ellipsize text to fit into event rect.
                textLayout = StaticLayout(TextUtils.ellipsize(bob, eventTextPaint!!, (availableLineCount * availableWidth).toFloat(), TextUtils.TruncateAt.END), eventTextPaint, (rect.right - originalLeft - (mEventPadding * 2).toFloat()).toInt(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)

                // Reduce line count.
                availableLineCount--

                // Repeat until text is short enough.
            } while (textLayout.height > availableHeight)

            // Draw text.
            canvas.save()
            canvas.translate(originalLeft + mEventPadding, originalTop + mEventPadding)
            textLayout.draw(canvas)
            canvas.restore()
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
    private inner class EventRect
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
    (var event: WeekViewEvent, var originalEvent: WeekViewEvent, var rectF: RectF?) {
        var left: Float = 0.toFloat()
        var width: Float = 0.toFloat()
        var top: Float = 0.toFloat()
        var bottom: Float = 0.toFloat()
    }


    /**
     * Gets more events of one/more month(s) if necessary. This method is called when the user is
     * scrolling the week view. The week view stores the events of three months: the visible month,
     * the previous month, the next month.
     * @param day The day where the user is currently is.
     */
    private fun getMoreEvents(day: Calendar) {

        // Get more events if the month is changed.
        if (eventRects == null)
            eventRects = ArrayList()
        if (weekViewLoader == null && !isInEditMode)
            throw IllegalStateException("You must provide a MonthChangeListener")

        // If a refresh was requested then reset some variables.
        if (refreshEvents) {
            eventRects!!.clear()
            previousPeriodEvents = null
            currentPeriodEvents = null
            nextPeriodEvents = null
            fetchedPeriod = -1
        }

        if (weekViewLoader != null) {
            val periodToFetch = weekViewLoader!!.toWeekViewPeriodIndex(day).toInt()
            if (!isInEditMode && (fetchedPeriod < 0 || fetchedPeriod != periodToFetch || refreshEvents)) {
                var previousPeriodEvents: List<WeekViewEvent>? = null
                var currentPeriodEvents: List<WeekViewEvent>? = null
                var nextPeriodEvents: List<WeekViewEvent>? = null

                if (this.previousPeriodEvents != null && this.currentPeriodEvents != null && this.nextPeriodEvents != null) {
                    if (periodToFetch == fetchedPeriod - 1) {
                        currentPeriodEvents = this.previousPeriodEvents
                        nextPeriodEvents = this.currentPeriodEvents
                    } else if (periodToFetch == fetchedPeriod) {
                        previousPeriodEvents = this.previousPeriodEvents
                        currentPeriodEvents = this.currentPeriodEvents
                        nextPeriodEvents = this.nextPeriodEvents
                    } else if (periodToFetch == fetchedPeriod + 1) {
                        previousPeriodEvents = this.currentPeriodEvents
                        currentPeriodEvents = this.nextPeriodEvents
                    }
                }
                if (currentPeriodEvents == null)
                    currentPeriodEvents = weekViewLoader!!.onLoad(periodToFetch)
                if (previousPeriodEvents == null)
                    previousPeriodEvents = weekViewLoader!!.onLoad(periodToFetch - 1)
                if (nextPeriodEvents == null)
                    nextPeriodEvents = weekViewLoader!!.onLoad(periodToFetch + 1)


                // Clear events.
                eventRects!!.clear()
                sortAndCacheEvents(previousPeriodEvents)
                sortAndCacheEvents(currentPeriodEvents)
                sortAndCacheEvents(nextPeriodEvents)
                calculateHeaderHeight()

                this.previousPeriodEvents = previousPeriodEvents
                this.currentPeriodEvents = currentPeriodEvents
                this.nextPeriodEvents = nextPeriodEvents
                fetchedPeriod = periodToFetch
            }
        }

        // Prepare to calculate positions of each events.
        val tempEvents = eventRects
        eventRects = ArrayList()

        // Iterate through each day with events to calculate the position of the events.
        while (tempEvents!!.size > 0) {
            val eventRects = ArrayList<EventRect>(tempEvents.size)

            // Get first event for a day.
            val eventRect1 = tempEvents.removeAt(0)
            eventRects.add(eventRect1)

            var i = 0
            while (i < tempEvents.size) {
                // Collect all other events for same day.
                val eventRect2 = tempEvents[i]
                if (isSameDay(eventRect1.event.startTime, eventRect2.event.startTime)) {
                    tempEvents.removeAt(i)
                    eventRects.add(eventRect2)
                } else {
                    i++
                }
            }
            computePositionOfEvents(eventRects)
        }
    }

    /**
     * Cache the event for smooth scrolling functionality.
     * @param event The event to cache.
     */
    private fun cacheEvent(event: WeekViewEvent) {
        if (event.startTime.compareTo(event.endTime) >= 0)
            return
        val splitedEvents = event.splitWeekViewEvents()
        for (splitedEvent in splitedEvents) {
            eventRects!!.add(EventRect(splitedEvent, event, null))
        }
    }

    /**
     * Sort and cache events.
     * @param events The events to be sorted and cached.
     */
    private fun sortAndCacheEvents(events: List<WeekViewEvent>) {
        sortEvents(events)
        for (event in events) {
            cacheEvent(event)
        }
    }

    /**
     * Sorts the events in ascending order.
     * @param events The events to be sorted.
     */
    private fun sortEvents(events: List<WeekViewEvent>) {
        Collections.sort(events) { event1, event2 ->
            val start1 = event1.startTime.timeInMillis
            val start2 = event2.startTime.timeInMillis
            var comparator = if (start1 > start2) 1 else if (start1 < start2) -1 else 0
            if (comparator == 0) {
                val end1 = event1.endTime.timeInMillis
                val end2 = event2.endTime.timeInMillis
                comparator = if (end1 > end2) 1 else if (end1 < end2) -1 else 0
            }
            comparator
        }
    }

    /**
     * Calculates the left and right positions of each events. This comes handy specially if events
     * are overlapping.
     * @param eventRects The events along with their wrapper class.
     */
    private fun computePositionOfEvents(eventRects: List<EventRect>) {
        // Make "collision groups" for all events that collide with others.
        val collisionGroups = ArrayList<MutableList<EventRect>>()
        for (eventRect in eventRects) {
            var isPlaced = false

            outerLoop@ for (collisionGroup in collisionGroups) {
                for (groupEvent in collisionGroup) {
                    if (isEventsCollide(groupEvent.event, eventRect.event) && groupEvent.event.isAllDay == eventRect.event.isAllDay) {
                        collisionGroup.add(eventRect)
                        isPlaced = true
                        break@outerLoop
                    }
                }
            }

            if (!isPlaced) {
                val newGroup = ArrayList<EventRect>()
                newGroup.add(eventRect)
                collisionGroups.add(newGroup)
            }
        }

        for (collisionGroup in collisionGroups) {
            expandEventsToMaxWidth(collisionGroup)
        }
    }

    /**
     * Expands all the events to maximum possible width. The events will try to occupy maximum
     * space available horizontally.
     * @param collisionGroup The group of events which overlap with each other.
     */
    private fun expandEventsToMaxWidth(collisionGroup: List<EventRect>) {
        // Expand the events to maximum possible width.
        val columns = ArrayList<MutableList<EventRect>>()
        columns.add(ArrayList())
        for (eventRect in collisionGroup) {
            var isPlaced = false
            for (column in columns) {
                if (column.size == 0) {
                    column.add(eventRect)
                    isPlaced = true
                } else if (!isEventsCollide(eventRect.event, column[column.size - 1].event)) {
                    column.add(eventRect)
                    isPlaced = true
                    break
                }
            }
            if (!isPlaced) {
                val newColumn = ArrayList<EventRect>()
                newColumn.add(eventRect)
                columns.add(newColumn)
            }
        }


        // Calculate left and right position for all the events.
        // Get the maxRowCount by looking in all columns.
        var maxRowCount = 0
        for (column in columns) {
            maxRowCount = Math.max(maxRowCount, column.size)
        }
        for (i in 0 until maxRowCount) {
            // Set the left and right values of the event.
            var j = 0f
            for (column in columns) {
                if (column.size >= i + 1) {
                    val eventRect = column[i]
                    eventRect.width = 1f / columns.size
                    eventRect.left = j / columns.size
                    if (!eventRect.event.isAllDay) {
                        eventRect.top = (eventRect.event.startTime.get(Calendar.HOUR_OF_DAY) * 60 + eventRect.event.startTime.get(Calendar.MINUTE)).toFloat()
                        eventRect.bottom = (eventRect.event.endTime.get(Calendar.HOUR_OF_DAY) * 60 + eventRect.event.endTime.get(Calendar.MINUTE)).toFloat()
                    } else {
                        eventRect.top = 0f
                        eventRect.bottom = allDayEventHeight.toFloat()
                    }
                    eventRects!!.add(eventRect)
                }
                j++
            }
        }
    }


    /**
     * Checks if two events overlap.
     * @param event1 The first event.
     * @param event2 The second event.
     * @return true if the events overlap.
     */
    private fun isEventsCollide(event1: WeekViewEvent, event2: WeekViewEvent): Boolean {
        val start1 = event1.startTime.timeInMillis
        val end1 = event1.endTime.timeInMillis
        val start2 = event2.startTime.timeInMillis
        val end2 = event2.endTime.timeInMillis
        return !(start1 >= end2 || end1 <= start2)
    }


    /**
     * Checks if time1 occurs after (or at the same time) time2.
     * @param time1 The time to check.
     * @param time2 The time to check against.
     * @return true if time1 and time2 are equal or if time1 is after time2. Otherwise false.
     */
    private fun isTimeAfterOrEquals(time1: Calendar?, time2: Calendar?): Boolean {
        return !(time1 == null || time2 == null) && time1.timeInMillis >= time2.timeInMillis
    }

    override fun invalidate() {
        super.invalidate()
        mAreDimensionsInvalid = true
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to setting and getting the properties.
    //
    /////////////////////////////////////////////////////////////////

    fun setOnEventClickListener(listener: EventClickListener) {
        this.eventClickListener = listener
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to scrolling.
    //
    /////////////////////////////////////////////////////////////////

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector!!.onTouchEvent(event)
        val `val` = gestureDetector!!.onTouchEvent(event)

        // Check after call of mGestureDetector, so mCurrentFlingDirection and mCurrentScrollDirection are set.
        if (event.action == MotionEvent.ACTION_UP && !isZooming && currentFlingDirection == Direction.NONE) {
            if (currentScrollDirection == Direction.RIGHT || currentScrollDirection == Direction.LEFT) {
                goToNearestOrigin()
            }
            currentScrollDirection = Direction.NONE
        }

        return `val`
    }

    private fun goToNearestOrigin() {
        var leftDays = (currentOrigin.x / (widthPerDay + mColumnGap)).toDouble()

        if (currentFlingDirection != Direction.NONE) {
            // snap to nearest day
            leftDays = Math.round(leftDays).toDouble()
        } else if (currentScrollDirection == Direction.LEFT) {
            // snap to last day
            leftDays = Math.floor(leftDays)
        } else if (currentScrollDirection == Direction.RIGHT) {
            // snap to next day
            leftDays = Math.ceil(leftDays)
        } else {
            // snap to nearest day
            leftDays = Math.round(leftDays).toDouble()
        }

        val nearestOrigin = (currentOrigin.x - leftDays * (widthPerDay + mColumnGap)).toInt()

        if (nearestOrigin != 0) {
            // Stop current animation.
            scroller!!.forceFinished(true)
            // Snap to date.
            scroller!!.startScroll(currentOrigin.x.toInt(), currentOrigin.y.toInt(), -nearestOrigin, 0, (Math.abs(nearestOrigin) / widthPerDay * scrollDuration).toInt())
            ViewCompat.postInvalidateOnAnimation(this@WeekView)
        }
        // Reset scrolling and fling direction.
        currentFlingDirection = Direction.NONE
        currentScrollDirection = currentFlingDirection
    }


    override fun computeScroll() {
        super.computeScroll()

        if (scroller!!.isFinished) {
            if (currentFlingDirection != Direction.NONE) {
                // Snap to day after fling is finished.
                goToNearestOrigin()
            }
        } else {
            if (currentFlingDirection != Direction.NONE && forceFinishScroll()) {
                goToNearestOrigin()
            } else if (scroller!!.computeScrollOffset()) {
                currentOrigin.y = scroller!!.currY.toFloat()
                currentOrigin.x = scroller!!.currX.toFloat()
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }

    /**
     * Check if scrolling should be stopped.
     * @return true if scrolling should be stopped before reaching the end of animation.
     */
    private fun forceFinishScroll(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // current velocity only available since api 14
            scroller!!.currVelocity <= mMinimumFlingVelocity
        } else {
            false
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
    fun goToToday() {
        val today = Calendar.getInstance()
        goToDate(today)
    }

    /**
     * Show a specific day on the week view.
     * @param date The date to show.
     */
    fun goToDate(date: Calendar) {
        scroller!!.forceFinished(true)
        currentFlingDirection = Direction.NONE
        currentScrollDirection = currentFlingDirection

        date.set(Calendar.HOUR_OF_DAY, 0)
        date.set(Calendar.MINUTE, 0)
        date.set(Calendar.SECOND, 0)
        date.set(Calendar.MILLISECOND, 0)

        if (mAreDimensionsInvalid) {
            mScrollToDay = date
            return
        }

        refreshEvents = true

        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val day = 1000L * 60L * 60L * 24L
        val dateInMillis = date.timeInMillis + date.timeZone.getOffset(date.timeInMillis)
        val todayInMillis = today.timeInMillis + today.timeZone.getOffset(today.timeInMillis)
        val dateDifference = dateInMillis / day - todayInMillis / day
        currentOrigin.x = -dateDifference * (widthPerDay + mColumnGap)
        invalidate()
    }

    /**
     * Refreshes the view and loads the events again.
     */
    fun notifyDatasetChanged() {
        refreshEvents = true
        invalidate()
    }

    /**
     * Vertically scroll to a specific hour in the week view.
     * @param hour The hour to scroll to in 24-hour format. Supported values are 0-24.
     */
    fun goToHour(hour: Double) {
        if (mAreDimensionsInvalid) {
            mScrollToHour = hour
            return
        }

        var verticalOffset = 0
        if (hour > 24)
            verticalOffset = mHourHeight * 24
        else if (hour > 0)
            verticalOffset = (mHourHeight * hour).toInt()

        if (verticalOffset > (mHourHeight * 24 - height).toFloat() + headerHeight + (mHeaderRowPadding * 2).toFloat() + headerMarginBottom)
            verticalOffset = ((mHourHeight * 24 - height).toFloat() + headerHeight + (mHeaderRowPadding * 2).toFloat() + headerMarginBottom).toInt()

        currentOrigin.y = (-verticalOffset).toFloat()
        invalidate()
    }


    /////////////////////////////////////////////////////////////////
    //
    //      Interfaces.
    //
    /////////////////////////////////////////////////////////////////

    interface EventClickListener {
        /**
         * Triggered when clicked on one existing event
         * @param event: event clicked.
         * @param eventRect: view containing the clicked event.
         */
        fun onEventClick(event: WeekViewEvent, eventRect: RectF?)
    }

    interface EventLongPressListener {
        /**
         * Similar to [com.alamkanak.weekview.WeekView.EventClickListener] but with a long press.
         * @param event: event clicked.
         * @param eventRect: view containing the clicked event.
         */
        fun onEventLongPress(event: WeekViewEvent, eventRect: RectF?)
    }

    interface EmptyViewClickListener {
        /**
         * Triggered when the users clicks on a empty space of the calendar.
         * @param time: [Calendar] object set with the date and time of the clicked position on the view.
         */
        fun onEmptyViewClicked(time: Calendar?)
    }

    interface EmptyViewLongPressListener {
        /**
         * Similar to [com.alamkanak.weekview.WeekView.EmptyViewClickListener] but with long press.
         * @param time: [Calendar] object set with the date and time of the long pressed position on the view.
         */
        fun onEmptyViewLongPress(time: Calendar?)
    }

    interface ScrollListener {
        /**
         * Called when the first visible day has changed.
         *
         * (this will also be called during the first draw of the weekview)
         * @param newFirstVisibleDay The new first visible day
         * @param oldFirstVisibleDay The old first visible day (is null on the first call).
         */
        fun onFirstVisibleDayChanged(newFirstVisibleDay: Calendar?, oldFirstVisibleDay: Calendar?)
    }

    companion object {

        @Deprecated("")
        val LENGTH_SHORT = 1
        @Deprecated("")
        val LENGTH_LONG = 2
    }
}

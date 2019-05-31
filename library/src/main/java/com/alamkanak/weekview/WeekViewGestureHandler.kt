package com.alamkanak.weekview

import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_UP
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

private enum class Direction {
    NONE, LEFT, RIGHT, VERTICAL
}

internal class WeekViewGestureHandler<T>(
    view: View,
    private val config: WeekViewConfigWrapper,
    private val cache: WeekViewCache<T>
) : GestureDetector.SimpleOnGestureListener() {

    private val listener = view as Listener
    private val touchHandler = WeekViewTouchHandler(config)

    private val scroller = OverScroller(view.context, FastOutLinearInInterpolator())
    private var currentScrollDirection = Direction.NONE
    private var currentFlingDirection = Direction.NONE

    private val gestureDetector = GestureDetector(view.context, this)

    private val scaleDetector = ScaleGestureDetector(view.context,
        object : ScaleGestureDetector.OnScaleGestureListener {
            override fun onScaleEnd(detector: ScaleGestureDetector) {
                isZooming = false
                cache.clearEventChipsCache()
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                isZooming = true
                goToNearestOrigin()
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val hourHeight = config.hourHeight
                config.newHourHeight = hourHeight * detector.scaleFactor
                listener.onScaled()
                return true
            }
        })

    private var isZooming: Boolean = false

    private val minimumFlingVelocity = ViewConfiguration.get(view.context).scaledMinimumFlingVelocity
    private val scaledTouchSlop = ViewConfiguration.get(view.context).scaledTouchSlop

    var eventClickListener: EventClickListener<T>? = null
    var eventLongPressListener: EventLongPressListener<T>? = null

    var emptyViewClickListener: EmptyViewClickListener? = null
    var emptyViewLongPressListener: EmptyViewLongPressListener? = null

    var scrollListener: ScrollListener? = null

    override fun onDown(
        e: MotionEvent
    ): Boolean {
        goToNearestOrigin()
        return true
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (isZooming) {
            return true
        }

        val absDistanceX = Math.abs(distanceX)
        val absDistanceY = Math.abs(distanceY)

        val canScrollHorizontally = config.horizontalScrollingEnabled

        when (currentScrollDirection) {
            Direction.NONE -> {
                // Allow scrolling only in one direction.
                currentScrollDirection = if (absDistanceX > absDistanceY && canScrollHorizontally) {
                    if (distanceX > 0) Direction.LEFT else Direction.RIGHT
                } else {
                    Direction.VERTICAL
                }
            }
            Direction.LEFT -> {
                // Change direction if there was enough change.
                if (absDistanceX > absDistanceY && distanceX < -scaledTouchSlop) {
                    currentScrollDirection = Direction.RIGHT
                }
            }
            Direction.RIGHT -> {
                // Change direction if there was enough change.
                if (absDistanceX > absDistanceY && distanceX > scaledTouchSlop) {
                    currentScrollDirection = Direction.LEFT
                }
            }
            else -> Unit
        }

        // Calculate the new origin after scroll.
        when (currentScrollDirection) {
            Direction.LEFT, Direction.RIGHT -> {
                config.currentOrigin.x -= distanceX * config.xScrollingSpeed
                config.currentOrigin.x = min(config.currentOrigin.x, config.maxX)
                config.currentOrigin.x = max(config.currentOrigin.x, config.minX)
                listener.onScrolled()
            }
            Direction.VERTICAL -> {
                config.currentOrigin.y -= distanceY
                listener.onScrolled()
            }
            else -> Unit
        }

        return true
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (isZooming) {
            return true
        }

        if (currentFlingDirection == Direction.LEFT && !config.horizontalScrollingEnabled ||
            currentFlingDirection == Direction.RIGHT && !config.horizontalFlingEnabled ||
            currentFlingDirection == Direction.VERTICAL && !config.verticalFlingEnabled) {
            return true
        }

        scroller.forceFinished(true)

        currentFlingDirection = currentScrollDirection
        when (currentFlingDirection) {
            Direction.LEFT, Direction.RIGHT -> onFlingHorizontal(velocityX)
            Direction.VERTICAL -> onFlingVertical(velocityY)
            else -> Unit
        }

        listener.onScrolled()
        return true
    }

    private fun onFlingHorizontal(
        originalVelocityX: Float
    ) {
        val startX = config.currentOrigin.x.toInt()
        val startY = config.currentOrigin.y.toInt()

        val velocityX = (originalVelocityX * config.xScrollingSpeed).toInt()
        val velocityY = 0

        val minX = config.minX.toInt()
        val maxX = config.maxX.toInt()

        val dayHeight = config.hourHeight * config.hoursPerDay
        val viewHeight = WeekView.height

        val minY = (dayHeight + config.headerHeight - viewHeight).toInt() * -1
        val maxY = 0

        scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
    }

    private fun onFlingVertical(
        originalVelocityY: Float
    ) {
        val startX = config.currentOrigin.x.toInt()
        val startY = config.currentOrigin.y.toInt()

        val velocityX = 0
        val velocityY = originalVelocityY.toInt()

        val minX = Integer.MIN_VALUE
        val maxX = Integer.MAX_VALUE

        val dayHeight = config.hourHeight * config.hoursPerDay
        val viewHeight = WeekView.height

        val minY = (dayHeight + config.headerHeight - viewHeight).toInt() * -1
        val maxY = 0

        scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
    }

    override fun onSingleTapConfirmed(
        e: MotionEvent
    ): Boolean {
        eventClickListener?.let { listener ->
            val eventChip = findHitEvent(e) ?: return@let
            val event = eventChip.originalEvent
            val data = Preconditions.checkNotNull(event.data,
                "No data to show. Did you pass the original object into the constructor of WeekViewEvent?")
            val rect = checkNotNull(eventChip.rect)
            listener.onEventClick(data, rect)
            return super.onSingleTapConfirmed(e)
        }

        // If the tap was on in an empty space, then trigger the callback.
        val timeColumnWidth = config.timeColumnWidth

        if (emptyViewClickListener != null
            && e.x > timeColumnWidth && e.y > config.headerHeight) {
            val selectedTime = touchHandler.getTimeFromPoint(e)
            if (selectedTime != null) {
                emptyViewClickListener?.onEmptyViewClicked(selectedTime)
            }
        }

        return super.onSingleTapConfirmed(e)
    }

    override fun onLongPress(e: MotionEvent) {
        super.onLongPress(e)

        eventLongPressListener?.let { listener ->
            val eventChip = findHitEvent(e) ?: return@let
            val data = Preconditions.checkNotNull(eventChip.event.data,
                "No data to show. Did you pass the original object into the constructor of WeekViewEvent?"
            )
            val rect = checkNotNull(eventChip.rect)
            listener.onEventLongPress(data, rect)
        }

        val timeColumnWidth = config.timeColumnWidth

        // If the tap was on in an empty space, then trigger the callback.
        emptyViewLongPressListener?.let { listener ->
            if (e.x > timeColumnWidth && e.y > config.headerHeight) {
                val selectedTime = touchHandler.getTimeFromPoint(e) ?: return@let
                listener.onEmptyViewLongPress(selectedTime)
            }
        }
    }

    private fun findHitEvent(
        e: MotionEvent
    ): EventChip<T>? {
        return cache.allEventChips.firstOrNull { it.isHit(e) }
    }

    private fun goToNearestOrigin() {
        val totalDayWidth = config.totalDayWidth
        var leftDays = (config.currentOrigin.x / totalDayWidth).toDouble()

        leftDays = if (currentFlingDirection != Direction.NONE) {
            // snap to nearest day
            round(leftDays)
        } else if (currentScrollDirection == Direction.LEFT) {
            // snap to last day
            floor(leftDays)
        } else if (currentScrollDirection == Direction.RIGHT) {
            // snap to next day
            ceil(leftDays)
        } else {
            // snap to nearest day
            round(leftDays)
        }

        val nearestOrigin = (config.currentOrigin.x - leftDays * totalDayWidth).toInt()

        if (nearestOrigin != 0) {
            // Stop current animation
            scroller.forceFinished(true)

            // Snap to date
            val startX = config.currentOrigin.x.toInt()
            val startY = config.currentOrigin.y.toInt()

            val distanceX = -nearestOrigin
            val distanceY = 0

            val daysScrolled = Math.abs(nearestOrigin) / config.widthPerDay
            val duration = (daysScrolled * config.scrollDuration).toInt()

            scroller.startScroll(startX, startY, distanceX, distanceY, duration)
            listener.onScrolled()
        }

        // Reset scrolling and fling direction.
        currentFlingDirection = Direction.NONE
        currentScrollDirection = currentFlingDirection
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        val value = gestureDetector.onTouchEvent(event)

        // Check after call of gestureDetector, so currentFlingDirection and currentScrollDirection are set
        if (event.action == ACTION_UP && !isZooming && currentFlingDirection == Direction.NONE) {
            if (currentScrollDirection == Direction.RIGHT || currentScrollDirection == Direction.LEFT) {
                goToNearestOrigin()
            }
            currentScrollDirection = Direction.NONE
        }

        return value
    }

    fun forceScrollFinished() {
        scroller.forceFinished(true)
        currentFlingDirection = Direction.NONE
        currentScrollDirection = currentFlingDirection
    }

    fun computeScroll() {
        val isFinished = scroller.isFinished
        val isFlinging = currentFlingDirection != Direction.NONE
        val isScrolling = currentScrollDirection != Direction.NONE

        if (isFinished && isFlinging) {
            // Snap to day after fling is finished
            goToNearestOrigin()
        } else if (isFinished and !isScrolling) {
            // Snap to day after scrolling is finished
            goToNearestOrigin()
        } else {
            if (isFlinging && shouldForceFinishScroll()) {
                goToNearestOrigin()
            } else if (scroller.computeScrollOffset()) {
                config.currentOrigin.y = scroller.currY.toFloat()
                config.currentOrigin.x = scroller.currX.toFloat()
                listener.onScrolled()
            }
        }
    }

    private fun shouldForceFinishScroll(): Boolean {
        return scroller.currVelocity <= minimumFlingVelocity
    }

    internal interface Listener {
        fun onScaled()
        fun onScrolled()
    }

}

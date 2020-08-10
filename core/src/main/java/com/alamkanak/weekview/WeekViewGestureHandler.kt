package com.alamkanak.weekview

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.ViewConfiguration
import com.alamkanak.weekview.Direction.Left
import com.alamkanak.weekview.Direction.None
import com.alamkanak.weekview.Direction.Right
import com.alamkanak.weekview.Direction.Vertical
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private enum class Direction {
    None, Left, Right, Vertical;

    val isHorizontal: Boolean
        get() = this == Left || this == Right

    val isVertical: Boolean
        get() = this == Vertical
}

internal class WeekViewGestureHandler(
    context: Context,
    private val viewState: ViewState,
    private val eventChipsCache: EventChipsCache,
    private val touchHandler: WeekViewTouchHandler,
    private val onInvalidation: () -> Unit
) : GestureDetector.SimpleOnGestureListener() {

    private val scroller = ValueAnimator()

    private var currentScrollDirection = None
    private var currentFlingDirection = None

    private val scaleDetector = ScaleGestureDetector(context, viewState, scroller, onInvalidation)
    private val gestureDetector = GestureDetector(context, this)

    private val scaledTouchSlop = context.scaledTouchSlop

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
        val absDistanceX = abs(distanceX)
        val absDistanceY = abs(distanceY)

        val canScrollHorizontally = viewState.horizontalScrollingEnabled

        when (currentScrollDirection) {
            None -> {
                // Allow scrolling only in one direction.
                currentScrollDirection = if (absDistanceX > absDistanceY && canScrollHorizontally) {
                    if (distanceX > 0) Left else Right
                } else {
                    Vertical
                }
            }
            Left -> {
                // Change direction if there was enough change.
                if (absDistanceX > absDistanceY && distanceX < -scaledTouchSlop) {
                    currentScrollDirection = Right
                }
            }
            Right -> {
                // Change direction if there was enough change.
                if (absDistanceX > absDistanceY && distanceX > scaledTouchSlop) {
                    currentScrollDirection = Left
                }
            }
            else -> Unit
        }

        // Calculate the new origin after scroll.
        when (currentScrollDirection) {
            Left, Right -> {
                viewState.currentOrigin.x -= distanceX
                viewState.currentOrigin.x = viewState.currentOrigin.x.limit(
                    minValue = viewState.minX,
                    maxValue = viewState.maxX
                )
                onInvalidation()
            }
            Vertical -> {
                viewState.currentOrigin.y -= distanceY
                onInvalidation()
            }
            None -> Unit
        }

        return true
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (currentFlingDirection.isHorizontal && !viewState.horizontalScrollingEnabled) {
            return true
        }

        scroller.stop()

        currentFlingDirection = currentScrollDirection
        when {
            currentFlingDirection.isHorizontal -> onFlingHorizontal()
            currentFlingDirection.isVertical -> onFlingVertical(velocityY)
            else -> Unit
        }

        onInvalidation()
        return true
    }

    private lateinit var preFlingFirstVisibleDate: Calendar

    private fun onFlingHorizontal() {
        val destinationDate = when (currentFlingDirection) {
            Left -> preFlingFirstVisibleDate + Days(viewState.numberOfVisibleDays)
            Right -> preFlingFirstVisibleDate - Days(viewState.numberOfVisibleDays)
            else -> throw IllegalStateException()
        }

        val destinationOffset = viewState.getXOriginForDate(destinationDate)
        val adjustedDestinationOffset = destinationOffset.limit(
            minValue = viewState.minX,
            maxValue = viewState.maxX
        )

        scroller.animate(
            fromValue = viewState.currentOrigin.x,
            toValue = adjustedDestinationOffset,
            onUpdate = {
                viewState.currentOrigin.x = it
                onInvalidation()
            }
        )
    }

    private fun onFlingVertical(
        originalVelocityY: Float
    ) {
        val dayHeight = viewState.hourHeight * viewState.hoursPerDay
        val viewHeight = viewState.viewHeight

        val minY = (dayHeight + viewState.headerHeight - viewHeight) * -1
        val maxY = 0f

        val currentOffset = viewState.currentOrigin.y
        val destinationOffset = currentOffset + (originalVelocityY * 0.18).roundToInt()
        val adjustedDestinationOffset = destinationOffset.limit(minValue = minY, maxValue = maxY)

        scroller.animate(
            fromValue = viewState.currentOrigin.y,
            toValue = adjustedDestinationOffset,
            onUpdate = {
                viewState.currentOrigin.y = it
                onInvalidation()
            }
        )
    }

    override fun onSingleTapConfirmed(
        e: MotionEvent
    ): Boolean {
        touchHandler.handleClick(e.x, e.y)
        return super.onSingleTapConfirmed(e)
    }

    override fun onLongPress(e: MotionEvent) {
        super.onLongPress(e)
        touchHandler.handleLongClick(e.x, e.y)
    }

    internal fun findHitEvent(x: Float, y: Float): EventChip? {
        val candidates = eventChipsCache.allEventChips.filter { it.isHit(x, y) }
        return when {
            candidates.isEmpty() -> null
            // Two events hit. This is most likely because an all-day event was clicked, but a
            // single event is rendered underneath it. We return the all-day event.
            candidates.size == 2 -> candidates.first { it.event.isAllDay }
            else -> candidates.first()
        }
    }

    private fun goToNearestOrigin() {
        val dayWidth = viewState.totalDayWidth
        val daysFromOrigin = viewState.currentOrigin.x / dayWidth.toDouble()
        val adjustedDaysFromOrigin = daysFromOrigin.roundToInt()

        val nearestOrigin = viewState.currentOrigin.x - adjustedDaysFromOrigin * dayWidth
        if (nearestOrigin != 0f) {
            val currentOffset = viewState.currentOrigin.x
            val destinationOffset = adjustedDaysFromOrigin * dayWidth

            scroller.animate(
                fromValue = currentOffset,
                toValue = destinationOffset,
                onUpdate = {
                    viewState.currentOrigin.x = it
                    onInvalidation()
                }
            )
        }

        // Reset scrolling and fling direction.
        currentFlingDirection = None
        currentScrollDirection = currentFlingDirection
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        val handled = gestureDetector.onTouchEvent(event)

        if (event.action == ACTION_UP && currentScrollDirection != None && currentFlingDirection == None) {
            goToNearestOrigin()
            currentScrollDirection = None
        } else if (event.action == ACTION_DOWN) {
            preFlingFirstVisibleDate = viewState.firstVisibleDate.copy()
        }

        return handled
    }

    fun forceScrollFinished() {
        scroller.stop()
        currentFlingDirection = None
        currentScrollDirection = currentFlingDirection
    }

    private val Context.scaledTouchSlop: Int
        get() = ViewConfiguration.get(this).scaledTouchSlop
}

internal fun Float.limit(minValue: Float, maxValue: Float): Float = min(max(this, minValue), maxValue)

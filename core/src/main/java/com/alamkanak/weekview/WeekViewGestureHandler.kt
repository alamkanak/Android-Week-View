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
import kotlin.math.roundToInt

private enum class Direction {
    None, Left, Right, Vertical;

    val isHorizontal: Boolean
        get() = this == Left || this == Right

    val isVertical: Boolean
        get() = this == Vertical
}

class WeekViewGestureHandler internal constructor(
    context: Context,
    private val viewState: ViewState,
    private val touchHandler: WeekViewTouchHandler,
    private val navigator: Navigator
) : GestureDetector.SimpleOnGestureListener() {

    private var currentScrollDirection = None
    private var currentFlingDirection = None

    private val scaleDetector = ScaleGestureDetector(
        context = context,
        viewState = viewState,
        navigator = navigator
    )
    private val gestureDetector = GestureDetector(context, this)

    private val scaledTouchSlop = context.scaledTouchSlop

    override fun onDown(e: MotionEvent): Boolean {
        goToNearestOrigin()
        return true
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        currentScrollDirection = interpretScroll(distanceX, distanceY, viewState)
        navigator.performScroll(distanceX, distanceY, currentScrollDirection)
        return true
    }

    private fun interpretScroll(
        distanceX: Float,
        distanceY: Float,
        viewState: ViewState
    ): Direction {
        val absDistanceX = abs(distanceX)
        val absDistanceY = abs(distanceY)
        val canScrollHorizontally = viewState.horizontalScrollingEnabled

        return when (currentScrollDirection) {
            None -> {
                // Allow scrolling only in one direction.
                if (absDistanceX > absDistanceY && canScrollHorizontally) {
                    if (distanceX > 0) Left else Right
                } else {
                    Vertical
                }
            }
            Left -> {
                // Change direction if there was enough change.
                if (absDistanceX > absDistanceY && distanceX < -scaledTouchSlop) {
                    Right
                } else {
                    currentScrollDirection
                }
            }
            Right -> {
                // Change direction if there was enough change.
                if (absDistanceX > absDistanceY && distanceX > scaledTouchSlop) {
                    Left
                } else {
                    currentScrollDirection
                }
            }
            else -> currentScrollDirection
        }
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

        navigator.stop()

        currentFlingDirection = currentScrollDirection
        when {
            currentFlingDirection.isHorizontal -> onFlingHorizontal()
            currentFlingDirection.isVertical -> onFlingVertical(velocityY)
            else -> Unit
        }

        navigator.requestInvalidation()
        return true
    }

    private lateinit var preFlingFirstVisibleDate: Calendar

    private fun onFlingHorizontal() {
        val destinationDate = preFlingFirstVisibleDate.performFling(
            direction = currentFlingDirection,
            viewState = viewState
        )
        navigator.scrollHorizontallyTo(date = destinationDate)
    }

    private fun onFlingVertical(
        originalVelocityY: Float
    ) {
        val currentOffset = viewState.currentOrigin.y
        val destinationOffset = currentOffset + (originalVelocityY * 0.18).roundToInt()
        navigator.scrollVerticallyTo(offset = destinationOffset)
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        touchHandler.handleClick(e.x, e.y)
        return super.onSingleTapUp(e)
    }

    override fun onLongPress(e: MotionEvent) {
        super.onLongPress(e)
        touchHandler.handleLongClick(e.x, e.y)
    }

    private fun goToNearestOrigin() {
        val dayWidth = viewState.dayWidth
        val daysFromOrigin = viewState.currentOrigin.x / dayWidth.toDouble()
        val adjustedDaysFromOrigin = daysFromOrigin.roundToInt()

        val nearestOrigin = viewState.currentOrigin.x - adjustedDaysFromOrigin * dayWidth
        if (nearestOrigin != 0f) {
            navigator.scrollHorizontallyTo(offset = adjustedDaysFromOrigin * dayWidth)
        }

        // Reset scrolling and fling direction.
        currentFlingDirection = None
        currentScrollDirection = currentFlingDirection
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        if (currentScrollDirection == Vertical && currentFlingDirection == None) {
            scaleDetector.onTouchEvent(event)
        }

        val handled = gestureDetector.onTouchEvent(event)

        if (event.action == ACTION_UP && currentFlingDirection == None) {
            if (currentScrollDirection.isHorizontal) {
                goToNearestOrigin()
            }
            currentScrollDirection = None
        } else if (event.action == ACTION_DOWN) {
            preFlingFirstVisibleDate = viewState.firstVisibleDate.copy()
        }

        return handled
    }

    fun forceScrollFinished() {
        navigator.stop()
        currentFlingDirection = None
        currentScrollDirection = currentFlingDirection
    }

    private val Context.scaledTouchSlop: Int
        get() = ViewConfiguration.get(this).scaledTouchSlop
}

private fun Calendar.performFling(direction: Direction, viewState: ViewState): Calendar {
    val daysDelta = Days(viewState.numberOfVisibleDays)
    return when (direction) {
        Left -> {
            if (viewState.isLtr) {
                this + daysDelta
            } else {
                this - daysDelta
            }
        }
        Right -> {
            if (viewState.isLtr) {
                this - daysDelta
            } else {
                this + daysDelta
            }
        }
        else -> throw IllegalStateException()
    }
}

private fun Navigator.performScroll(
    distanceX: Float,
    distanceY: Float,
    direction: Direction
) {

    when (direction) {
        Left, Right -> {
            scrollHorizontallyBy(distance = distanceX)
        }
        Vertical -> {
            scrollVerticallyBy(distance = distanceY)
        }
        None -> Unit
    }
}

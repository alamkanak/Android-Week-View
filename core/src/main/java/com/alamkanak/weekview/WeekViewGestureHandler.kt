package com.alamkanak.weekview

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
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

internal class WeekViewGestureHandler(
    context: Context,
    private val viewState: ViewState,
    private val touchHandler: WeekViewTouchHandler,
    private val navigator: Navigator,
    private val dragHandler: DragHandler,
) : GestureDetector.SimpleOnGestureListener() {

    private var scrollDirection: Direction = None
    private var flingDirection: Direction = None

    private val scaleDetector = ScaleGestureDetector(
        context = context,
        viewState = viewState,
        navigator = navigator,
    )

    private val gestureDetector = GestureDetector(context, this)

    private var preFlingFirstVisibleDate: Calendar = today()

    override fun onDown(e: MotionEvent): Boolean {
        if (scrollDirection == None && flingDirection != None) {
            goToNearestOrigin()
        }
        return true
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float,
    ): Boolean {
        if (scrollDirection == None) {
            // Only change the scroll direction if we're starting a new scroll. If the user changes
            // the direction while already scrolling, we stay in the current scrolling direction.
            scrollDirection = determineScrollDirection(distanceX, distanceY)
        }

        when {
            scrollDirection.isHorizontal -> {
                navigator.scrollHorizontallyBy(distance = distanceX)
            }
            scrollDirection.isVertical -> {
                navigator.scrollVerticallyBy(distance = distanceY)
            }
        }

        return true
    }

    private fun determineScrollDirection(
        distanceX: Float,
        distanceY: Float
    ): Direction {
        val absDistanceX = abs(distanceX)
        val absDistanceY = abs(distanceY)
        val canScrollHorizontally = viewState.horizontalScrollingEnabled

        return if (absDistanceY > absDistanceX) {
            Vertical
        } else if (absDistanceX > absDistanceY && canScrollHorizontally) {
            if (distanceX > 0) {
                Left
            } else {
                Right
            }
        } else {
            None
        }
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (flingDirection.isHorizontal && !viewState.horizontalScrollingEnabled) {
            return true
        }

        navigator.stop()

        flingDirection = scrollDirection
        when {
            flingDirection.isHorizontal -> onFlingHorizontal()
            flingDirection.isVertical -> onFlingVertical(velocityY)
            else -> Unit
        }

        navigator.requestInvalidation()
        return true
    }

    private fun onFlingHorizontal() {
        val destinationDate = preFlingFirstVisibleDate.performFling(
            direction = flingDirection,
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

        val longClickResult = touchHandler.handleLongClick(e.x, e.y) ?: return
        if (!longClickResult.handled) {
            dragHandler.startDragAndDrop(longClickResult.eventChip, e.x, e.y)
        }
    }

    private fun goToNearestOrigin() {
        if (viewState.numberOfVisibleDays >= 7) {
            goToNearestWeek()
        } else {
            goToNearestDay()
        }
    }

    private fun goToNearestWeek() {
        val firstVisibleDate = viewState.firstVisibleDate
        val nearestOriginDate = viewState.currentDate
        val daysScrolled = abs(firstVisibleDate.toEpochDays() - nearestOriginDate.toEpochDays())

        val scrollTarget = if (daysScrolled > 3) {
            if (nearestOriginDate < firstVisibleDate) {
                nearestOriginDate.previousFirstDayOfWeek()
            } else {
                nearestOriginDate.nextFirstDayOfWeek()
            }
        } else {
            firstVisibleDate
        }

        navigator.scrollHorizontallyTo(date = scrollTarget)
    }

    private fun goToNearestDay() {
        val dayWidth = viewState.dayWidth
        val daysFromOrigin = viewState.currentOrigin.x / dayWidth.toDouble()
        val roundedDaysFromOrigin = daysFromOrigin.roundToInt()

        val nearestOrigin = viewState.currentOrigin.x - roundedDaysFromOrigin * dayWidth
        if (nearestOrigin != 0f) {
            navigator.scrollHorizontallyTo(offset = roundedDaysFromOrigin * dayWidth)
        }
    }

    private fun resetScrollAndFlingDirections() {
        scrollDirection = None
        flingDirection = None
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        if (!scrollDirection.isHorizontal && flingDirection == None) {
            scaleDetector.onTouchEvent(event)
        }

        val handled = gestureDetector.onTouchEvent(event)

        if (event.action == ACTION_UP) {
            onUp()
        }

        if (event.action == ACTION_DOWN) {
            preFlingFirstVisibleDate = viewState.firstVisibleDate.copy()
        }

        if (event.action == ACTION_MOVE && dragHandler.isDragging) {
            dragHandler.updateDragAndDrop(event)
        }

        if (event.action == ACTION_UP && dragHandler.isDragging) {
            dragHandler.finishDragAndDrop()
        }

        return handled
    }

    private fun onUp() {
        if (flingDirection == None && scrollDirection != None) {
            handleScrollingFinished()
        }

        resetScrollAndFlingDirections()
    }

    private fun handleScrollingFinished() {
        when (scrollDirection) {
            Vertical -> navigator.notifyVerticalScrollingFinished()
            Left, Right -> goToNearestOrigin()
            None -> Unit
        }
    }

    fun forceScrollFinished() {
        navigator.stop()
        resetScrollAndFlingDirections()
    }
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

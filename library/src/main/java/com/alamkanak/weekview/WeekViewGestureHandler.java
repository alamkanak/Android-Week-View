package com.alamkanak.weekview;

import android.content.Context;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import java.util.Calendar;
import java.util.List;

import static android.view.KeyEvent.ACTION_UP;
import static com.alamkanak.weekview.Constants.HOURS_PER_DAY;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.round;

final class WeekViewGestureHandler<T> extends GestureDetector.SimpleOnGestureListener {

    private enum Direction {
        NONE, LEFT, RIGHT, VERTICAL
    }

    private Listener listener;

    private WeekViewData<T> data;
    private WeekViewConfig config;
    private WeekViewDrawingConfig drawingConfig;

    private WeekViewTouchHandler touchHandler;

    private OverScroller scroller;
    private Direction currentScrollDirection = Direction.NONE;
    private Direction currentFlingDirection = Direction.NONE;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleDetector;
    private boolean isZooming;

    private int minimumFlingVelocity;
    private int scaledTouchSlop;

    private EventClickListener<T> eventClickListener;
    private EventLongPressListener<T> eventLongPressListener;

    private EmptyViewClickListener emptyViewClickListener;
    private EmptyViewLongPressListener emptyViewLongPressListener;

    private WeekViewLoader<T> weekViewLoader;
    private ScrollListener scrollListener;

    WeekViewGestureHandler(Context context, View view,
                                  WeekViewConfig config, WeekViewData<T> data) {
        this.listener = (Listener) view;

        this.data = data;
        this.config = config;
        this.drawingConfig = config.drawingConfig;

        touchHandler = new WeekViewTouchHandler(config);
        gestureDetector = new GestureDetector(context, this);
        scroller = new OverScroller(context, new FastOutLinearInInterpolator());

        minimumFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                isZooming = false;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                isZooming = true;
                goToNearestOrigin();
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float hourHeight = WeekViewGestureHandler.this.config.hourHeight;
                drawingConfig.newHourHeight = round(hourHeight * detector.getScaleFactor());
                listener.onScaled();
                return true;
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //   Gesture Detector
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onDown(MotionEvent e) {
        goToNearestOrigin();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (isZooming) {
            return true;
        }

        final float absDistanceX = Math.abs(distanceX);
        final float absDistanceY = Math.abs(distanceY);

        final boolean canScrollHorizontally = config.horizontalScrollingEnabled;

        switch (currentScrollDirection) {
            case NONE: {
                // Allow scrolling only in one direction.
                if (absDistanceX > absDistanceY && canScrollHorizontally) {
                    if (distanceX > 0) {
                        currentScrollDirection = Direction.LEFT;
                    } else {
                        currentScrollDirection = Direction.RIGHT;
                    }
                } else {
                    currentScrollDirection = Direction.VERTICAL;
                }
                break;
            }
            case LEFT: {
                // Change direction if there was enough change.
                if (absDistanceX > absDistanceY && distanceX < -scaledTouchSlop) {
                    currentScrollDirection = Direction.RIGHT;
                }
                break;
            }
            case RIGHT: {
                // Change direction if there was enough change.
                if (absDistanceX > absDistanceY && distanceX > scaledTouchSlop) {
                    currentScrollDirection = Direction.LEFT;
                }
                break;
            }
        }

        // Calculate the new origin after scroll.
        switch (currentScrollDirection) {
            case LEFT:
            case RIGHT:
                drawingConfig.currentOrigin.x -= distanceX * config.xScrollingSpeed;
                listener.onScrolled();
                break;
            case VERTICAL:
                drawingConfig.currentOrigin.y -= distanceY;
                listener.onScrolled();
                break;
        }
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (isZooming) {
            return true;
        }

        if ((currentFlingDirection == Direction.LEFT && !config.horizontalFlingEnabled) ||
                (currentFlingDirection == Direction.RIGHT && !config.horizontalFlingEnabled) ||
                (currentFlingDirection == Direction.VERTICAL && !config.verticalFlingEnabled)) {
            return true;
        }

        scroller.forceFinished(true);

        currentFlingDirection = currentScrollDirection;
        switch (currentFlingDirection) {
            case LEFT:
            case RIGHT:
                onFlingHorizontal(velocityX);
                break;
            case VERTICAL:
                onFlingVertical(velocityY);
                break;
        }

        listener.onScrolled();
        return true;
    }

    private void onFlingHorizontal(float originalVelocityX) {
        final int startX = (int) drawingConfig.currentOrigin.x;
        final int startY = (int) drawingConfig.currentOrigin.y;

        final int velocityX = (int) (originalVelocityX * config.xScrollingSpeed);
        final int velocityY = 0;

        final int minX = Integer.MIN_VALUE;
        final int maxX = Integer.MAX_VALUE;

        final int dayHeight = config.hourHeight * HOURS_PER_DAY;
        final int viewHeight = WeekView.getViewHeight();

        final float headerHeight = drawingConfig.headerHeight
                + config.headerRowPadding * 2
                + drawingConfig.headerMarginBottom;

        final int minY = (int) (dayHeight + headerHeight - viewHeight) * (-1);
        final int maxY = 0;

        scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
    }

    private void onFlingVertical(float originalVelocityY) {
        final int startX = (int) drawingConfig.currentOrigin.x;
        final int startY = (int) drawingConfig.currentOrigin.y;

        final int velocityX = 0;
        final int velocityY = (int) originalVelocityY;

        final int minX = Integer.MIN_VALUE;
        final int maxX = Integer.MAX_VALUE;

        final int dayHeight = config.hourHeight * HOURS_PER_DAY;
        final int viewHeight = WeekView.getViewHeight();

        final float headerHeight = drawingConfig.headerHeight
                + config.headerRowPadding * 2
                + drawingConfig.headerMarginBottom;

        final int minY = (int) (dayHeight + headerHeight - viewHeight) * (-1);
        final int maxY = 0;

        scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        final EventChip<T> eventChip = findHitEvent(e);
        if (eventChip != null && eventClickListener != null) {
            T data = eventChip.event.getData();
            if (data != null) {
                eventClickListener.onEventClick(data, eventChip.rect);
            } else {
                throw new WeekViewException("No data to show. Did you pass the original object into the constructor of WeekViewEvent?");
            }

            return super.onSingleTapConfirmed(e);
        }

        // If the tap was on in an empty space, then trigger the callback.
        final float headerHeight = drawingConfig.headerHeight
                + config.headerRowPadding * 2
                + drawingConfig.headerMarginBottom;
        final float timeColumnWidth = drawingConfig.timeColumnWidth;

        if (emptyViewClickListener != null
                && e.getX() > timeColumnWidth && e.getY() > headerHeight) {
            final Calendar selectedTime = touchHandler.getTimeFromPoint(e);
            if (selectedTime != null) {
                emptyViewClickListener.onEmptyViewClicked(selectedTime);
            }
        }

        return super.onSingleTapConfirmed(e);
    }

    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e);

        final EventChip<T> eventChip = findHitEvent(e);
        if (eventChip != null && eventLongPressListener != null) {
            final T data = eventChip.originalEvent.getData();
            if (data != null) {
                eventLongPressListener.onEventLongPress(data, eventChip.rect);
            } else {
                throw new WeekViewException("No data to show. Did you pass the original object into the constructor of WeekViewEvent?");
            }
        }

        final float headerHeight = drawingConfig.headerHeight
                + config.headerRowPadding * 2
                + drawingConfig.headerMarginBottom;
        final float timeColumnWidth = drawingConfig.timeColumnWidth;

        // If the tap was on in an empty space, then trigger the callback.
        if (emptyViewLongPressListener != null
                && e.getX() > timeColumnWidth && e.getY() > headerHeight) {
            final Calendar selectedTime = touchHandler.getTimeFromPoint(e);
            if (selectedTime != null) {
                emptyViewLongPressListener.onEmptyViewLongPress(selectedTime);
            }
        }
    }

    private EventChip<T> findHitEvent(MotionEvent e) {
        final List<EventChip<T>> eventChips = data.getAllEventChips();
        if (eventChips != null) {
            for (EventChip<T> eventChip : eventChips) {
                if (eventChip.isHit(e)) {
                    return eventChip;
                }
            }
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //   Getters and Setters
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    EventClickListener getEventClickListener() {
        return eventClickListener;
    }

    void setEventClickListener(EventClickListener<T> eventClickListener) {
        this.eventClickListener = eventClickListener;
    }

    EventLongPressListener getEventLongPressListener() {
        return eventLongPressListener;
    }

    void setEventLongPressListener(EventLongPressListener<T> eventLongPressListener) {
        this.eventLongPressListener = eventLongPressListener;
    }

    WeekViewLoader<T> getWeekViewLoader() {
        return weekViewLoader;
    }

    void setWeekViewLoader(WeekViewLoader<T> weekViewLoader) {
        this.weekViewLoader = weekViewLoader;
    }

    EmptyViewClickListener getEmptyViewClickListener() {
        return emptyViewClickListener;
    }

    void setEmptyViewClickListener(EmptyViewClickListener emptyViewClickListener) {
        this.emptyViewClickListener = emptyViewClickListener;
    }

    EmptyViewLongPressListener getEmptyViewLongPressListener() {
        return emptyViewLongPressListener;
    }

    void setEmptyViewLongPressListener(EmptyViewLongPressListener emptyViewLongPressListener) {
        this.emptyViewLongPressListener = emptyViewLongPressListener;
    }

    ScrollListener getScrollListener() {
        return scrollListener;
    }

    void setScrollListener(ScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    private void goToNearestOrigin() {
        final float totalDayWidth = config.getTotalDayWidth();
        double leftDays = drawingConfig.currentOrigin.x / totalDayWidth;

        if (currentFlingDirection != Direction.NONE) {
            // snap to nearest day
            leftDays = round(leftDays);
        } else if (currentScrollDirection == Direction.LEFT) {
            // snap to last day
            leftDays = floor(leftDays);
        } else if (currentScrollDirection == Direction.RIGHT) {
            // snap to next day
            leftDays = ceil(leftDays);
        } else {
            // snap to nearest day
            leftDays = round(leftDays);
        }

        final int nearestOrigin = (int) (drawingConfig.currentOrigin.x - leftDays * totalDayWidth);

        if (nearestOrigin != 0) {
            // Stop current animation
            scroller.forceFinished(true);

            // Snap to date
            final int startX = (int) drawingConfig.currentOrigin.x;
            final int startY = (int) drawingConfig.currentOrigin.y;

            final int distanceX = -nearestOrigin;
            final int distanceY = 0;

            final float daysScrolled = Math.abs(nearestOrigin) / drawingConfig.widthPerDay;
            final int duration = (int) (daysScrolled * config.scrollDuration);

            scroller.startScroll(startX, startY, distanceX, distanceY, duration);
            listener.onScrolled();
        }

        // Reset scrolling and fling direction.
        currentScrollDirection = currentFlingDirection = Direction.NONE;
    }

    boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        final boolean val = gestureDetector.onTouchEvent(event);

        // Check after call of gestureDetector, so currentFlingDirection and currentScrollDirection are set
        if (event.getAction() == ACTION_UP && !isZooming && currentFlingDirection == Direction.NONE) {
            if (currentScrollDirection == Direction.RIGHT || currentScrollDirection == Direction.LEFT) {
                goToNearestOrigin();
            }
            currentScrollDirection = Direction.NONE;
        }

        return val;
    }

    void forceScrollFinished() {
        scroller.forceFinished(true);
        currentScrollDirection = currentFlingDirection = Direction.NONE;
    }

    void computeScroll() {
        if (scroller.isFinished() && currentFlingDirection != Direction.NONE) {
            // Snap to day after fling is finished
            goToNearestOrigin();
        } else {
            if (currentFlingDirection != Direction.NONE && shouldForceFinishScroll()) {
                goToNearestOrigin();
            } else if (scroller.computeScrollOffset()) {
                drawingConfig.currentOrigin.y = scroller.getCurrY();
                drawingConfig.currentOrigin.x = scroller.getCurrX();
                listener.onScrolled();
            }
        }
    }

    /**
     * Check if scrolling should be stopped.
     *
     * @return true if scrolling should be stopped before reaching the end of animation.
     */
    private boolean shouldForceFinishScroll() {
        return scroller.getCurrVelocity() <= minimumFlingVelocity;
    }

    interface Listener {
        void onScaled();
        void onScrolled();
    }

}

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
import static com.alamkanak.weekview.Preconditions.checkState;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;

final class WeekViewGestureHandler<T> extends GestureDetector.SimpleOnGestureListener {

    private enum Direction {
        NONE, LEFT, RIGHT, VERTICAL
    }

    private final Listener listener;

    private final WeekViewCache<T> cache;
    private final WeekViewConfigWrapper config;

    private final WeekViewTouchHandler touchHandler;

    private final OverScroller scroller;
    private Direction currentScrollDirection = Direction.NONE;
    private Direction currentFlingDirection = Direction.NONE;

    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleDetector;
    private boolean isZooming;

    private final int minimumFlingVelocity;
    private final int scaledTouchSlop;

    private EventClickListener<T> eventClickListener;
    private EventLongPressListener<T> eventLongPressListener;

    private EmptyViewClickListener emptyViewClickListener;
    private EmptyViewLongPressListener emptyViewLongPressListener;

    private WeekViewLoader<T> weekViewLoader;
    private ScrollListener scrollListener;

    WeekViewGestureHandler(Context context, View view,
                           WeekViewConfigWrapper config, WeekViewCache<T> cache) {
        this.listener = (Listener) view;

        this.cache = cache;
        this.config = config;

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
                float hourHeight = WeekViewGestureHandler.this.config.getHourHeight();
                WeekViewGestureHandler.this.config.setNewHourHeight(hourHeight * detector.getScaleFactor());
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

        final boolean canScrollHorizontally = config.getHorizontalScrollingEnabled();

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
                config.getCurrentOrigin().x -= distanceX * config.getXScrollingSpeed();

                float minX = config.getMinX();
                float maxX = config.getMaxX();

                config.getCurrentOrigin().x = min(config.getCurrentOrigin().x, maxX);
                config.getCurrentOrigin().x = max(config.getCurrentOrigin().x, minX);

                listener.onScrolled();
                break;
            case VERTICAL:
                config.getCurrentOrigin().y -= distanceY;
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

        if ((currentFlingDirection == Direction.LEFT && !config.getHorizontalScrollingEnabled()) ||
                (currentFlingDirection == Direction.RIGHT && !config.getHorizontalFlingEnabled()) ||
                (currentFlingDirection == Direction.VERTICAL && !config.getVerticalFlingEnabled())) {
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
        final int startX = (int) config.getCurrentOrigin().x;
        final int startY = (int) config.getCurrentOrigin().y;

        final int velocityX = (int) (originalVelocityX * config.getXScrollingSpeed());
        final int velocityY = 0;

        final int minX = (int) config.getMinX();
        final int maxX = (int) config.getMaxX();

        final float dayHeight = config.getHourHeight() * config.getHoursPerDay();
        final int viewHeight = WeekView.getViewHeight();

        final int minY = (int) (dayHeight + config.getHeaderHeight() - viewHeight) * (-1);
        final int maxY = 0;

        scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
    }

    private void onFlingVertical(float originalVelocityY) {
        final int startX = (int) config.getCurrentOrigin().x;
        final int startY = (int) config.getCurrentOrigin().y;

        final int velocityX = 0;
        final int velocityY = (int) originalVelocityY;

        final int minX = Integer.MIN_VALUE;
        final int maxX = Integer.MAX_VALUE;

        final float dayHeight = config.getHourHeight() * config.getHoursPerDay();
        final int viewHeight = WeekView.getViewHeight();

        final int minY = (int) (dayHeight + config.getHeaderHeight() - viewHeight) * (-1);
        final int maxY = 0;

        scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        final EventChip<T> eventChip = findHitEvent(e);
        if (eventChip != null && eventClickListener != null) {
            T data = eventChip.event.getData();
            checkState(data != null, "No data to show. Did you pass the " +
                    "original object into the constructor of WeekViewEvent?");
            eventClickListener.onEventClick(data, eventChip.rect);
            return super.onSingleTapConfirmed(e);
        }

        // If the tap was on in an empty space, then trigger the callback.
        final float timeColumnWidth = config.getTimeColumnWidth();

        if (emptyViewClickListener != null
                && e.getX() > timeColumnWidth && e.getY() > config.getHeaderHeight()) {
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
            checkState(data != null, "No data to show. Did you pass the " +
                    "original object into the constructor of WeekViewEvent?");
            eventLongPressListener.onEventLongPress(data, eventChip.rect);
        }

        final float timeColumnWidth = config.getTimeColumnWidth();

        // If the tap was on in an empty space, then trigger the callback.
        if (emptyViewLongPressListener != null
                && e.getX() > timeColumnWidth && e.getY() > config.getHeaderHeight()) {
            final Calendar selectedTime = touchHandler.getTimeFromPoint(e);
            if (selectedTime != null) {
                emptyViewLongPressListener.onEmptyViewLongPress(selectedTime);
            }
        }
    }

    private EventChip<T> findHitEvent(MotionEvent e) {
        final List<EventChip<T>> eventChips = cache.getAllEventChips();
        for (EventChip<T> eventChip : eventChips) {
            if (eventChip.isHit(e)) {
                return eventChip;
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
        double leftDays = config.getCurrentOrigin().x / totalDayWidth;

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

        final int nearestOrigin = (int) (config.getCurrentOrigin().x - leftDays * totalDayWidth);

        if (nearestOrigin != 0) {
            // Stop current animation
            scroller.forceFinished(true);

            // Snap to date
            final int startX = (int) config.getCurrentOrigin().x;
            final int startY = (int) config.getCurrentOrigin().y;

            final int distanceX = -nearestOrigin;
            final int distanceY = 0;

            final float daysScrolled = Math.abs(nearestOrigin) / config.getWidthPerDay();
            final int duration = (int) (daysScrolled * config.getScrollDuration());

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
        final boolean isFinished = scroller.isFinished();
        final boolean isFlinging = currentFlingDirection != Direction.NONE;
        final boolean isScrolling = currentScrollDirection != Direction.NONE;

        if (isFinished && isFlinging) {
            // Snap to day after fling is finished
            goToNearestOrigin();
        } else if (isFinished & !isScrolling) {
            // Snap to day after scrolling is finished
            goToNearestOrigin();
        } else {
            if (isFlinging && shouldForceFinishScroll()) {
                goToNearestOrigin();
            } else if (scroller.computeScrollOffset()) {
                config.getCurrentOrigin().y = scroller.getCurrY();
                config.getCurrentOrigin().x = scroller.getCurrX();
                listener.onScrolled();
            }
        }
    }

    private boolean shouldForceFinishScroll() {
        return scroller.getCurrVelocity() <= minimumFlingVelocity;
    }

    interface Listener {
        void onScaled();
        void onScrolled();
    }

}

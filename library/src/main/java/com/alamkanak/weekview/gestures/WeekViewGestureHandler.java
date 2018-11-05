package com.alamkanak.weekview.gestures;

import android.content.Context;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import com.alamkanak.weekview.data.WeekViewLoader;
import com.alamkanak.weekview.drawing.EventChip;
import com.alamkanak.weekview.drawing.WeekViewDrawingConfig;
import com.alamkanak.weekview.listeners.EmptyViewClickListener;
import com.alamkanak.weekview.listeners.EmptyViewLongPressListener;
import com.alamkanak.weekview.listeners.EventClickListener;
import com.alamkanak.weekview.listeners.EventLongPressListener;
import com.alamkanak.weekview.listeners.ScrollListener;
import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.model.WeekViewData;
import com.alamkanak.weekview.ui.WeekView;
import com.alamkanak.weekview.utils.WeekViewException;

import java.util.Calendar;
import java.util.List;

import static android.view.KeyEvent.ACTION_UP;
import static com.alamkanak.weekview.ui.WeekView.Direction.LEFT;
import static com.alamkanak.weekview.ui.WeekView.Direction.NONE;
import static com.alamkanak.weekview.ui.WeekView.Direction.RIGHT;
import static com.alamkanak.weekview.ui.WeekView.Direction.VERTICAL;
import static com.alamkanak.weekview.utils.Constants.HOURS_PER_DAY;
import static java.lang.Math.round;

public class WeekViewGestureHandler<T> extends GestureDetector.SimpleOnGestureListener {

    private Listener listener;

    private WeekViewData<T> data;
    private WeekViewConfig config;
    private WeekViewDrawingConfig drawingConfig;

    private WeekViewTouchHandler touchHandler;

    public OverScroller scroller;
    public WeekView.Direction currentScrollDirection = NONE;
    public WeekView.Direction currentFlingDirection = NONE;

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

    public WeekViewGestureHandler(Context context, View view,
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

        switch (currentScrollDirection) {
            case NONE: {
                // Allow scrolling only in one direction.
                if (absDistanceX > absDistanceY) {
                    if (distanceX > 0) {
                        currentScrollDirection = LEFT;
                    } else {
                        currentScrollDirection = RIGHT;
                    }
                } else {
                    currentScrollDirection = VERTICAL;
                }
                break;
            }
            case LEFT: {
                // Change direction if there was enough change.
                if (absDistanceX > absDistanceY && distanceX < -scaledTouchSlop) {
                    currentScrollDirection = RIGHT;
                }
                break;
            }
            case RIGHT: {
                // Change direction if there was enough change.
                if (absDistanceX > absDistanceY && distanceX > scaledTouchSlop) {
                    currentScrollDirection = LEFT;
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

        if ((currentFlingDirection == LEFT && !config.horizontalFlingEnabled) ||
                (currentFlingDirection == RIGHT && !config.horizontalFlingEnabled) ||
                (currentFlingDirection == VERTICAL && !config.verticalFlingEnabled)) {
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
        final float timeColumnWidth = drawingConfig.headerColumnWidth;

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

        EventChip<T> eventChip = findHitEvent(e);
        if (eventChip != null && eventLongPressListener != null) {
            T data = eventChip.originalEvent.getData();
            if (data != null) {
                eventLongPressListener.onEventLongPress(data, eventChip.rect);
            } else {
                throw new WeekViewException("No data to show. Did you pass the original object into the constructor of WeekViewEvent?");
            }
        }

        float headerHeight = drawingConfig.headerHeight
                + config.headerRowPadding * 2
                + drawingConfig.headerMarginBottom;
        float timeColumnWidth = drawingConfig.headerColumnWidth;

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
        List<EventChip<T>> eventChips = data.getAllEventChips();
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

    public EventClickListener getEventClickListener() {
        return eventClickListener;
    }

    public void setEventClickListener(EventClickListener<T> eventClickListener) {
        this.eventClickListener = eventClickListener;
    }

    public EventLongPressListener getEventLongPressListener() {
        return eventLongPressListener;
    }

    public void setEventLongPressListener(EventLongPressListener<T> eventLongPressListener) {
        this.eventLongPressListener = eventLongPressListener;
    }

    public WeekViewLoader<T> getWeekViewLoader() {
        return weekViewLoader;
    }

    public void setWeekViewLoader(WeekViewLoader<T> weekViewLoader) {
        this.weekViewLoader = weekViewLoader;
    }

    public EmptyViewClickListener getEmptyViewClickListener() {
        return emptyViewClickListener;
    }

    public void setEmptyViewClickListener(EmptyViewClickListener emptyViewClickListener) {
        this.emptyViewClickListener = emptyViewClickListener;
    }

    public EmptyViewLongPressListener getEmptyViewLongPressListener() {
        return emptyViewLongPressListener;
    }

    public void setEmptyViewLongPressListener(EmptyViewLongPressListener emptyViewLongPressListener) {
        this.emptyViewLongPressListener = emptyViewLongPressListener;
    }

    public ScrollListener getScrollListener() {
        return scrollListener;
    }

    public void setScrollListener(ScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    private void goToNearestOrigin() {
        final float totalDayWidth = drawingConfig.widthPerDay + config.columnGap;
        double leftDays = drawingConfig.currentOrigin.x / totalDayWidth;

        if (currentFlingDirection != NONE) {
            // snap to nearest day
            leftDays = round(leftDays);
        } else if (currentScrollDirection == LEFT) {
            // snap to last day
            leftDays = Math.floor(leftDays);
        } else if (currentScrollDirection == RIGHT) {
            // snap to next day
            leftDays = Math.ceil(leftDays);
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
        currentScrollDirection = currentFlingDirection = NONE;
    }

    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        final boolean val = gestureDetector.onTouchEvent(event);

        // Check after call of gestureDetector, so currentFlingDirection and currentScrollDirection are set
        if (event.getAction() == ACTION_UP && !isZooming && currentFlingDirection == NONE) {
            if (currentScrollDirection == RIGHT || currentScrollDirection == LEFT) {
                goToNearestOrigin();
            }
            currentScrollDirection = NONE;
        }

        return val;
    }

    public void computeScroll() {
        if (scroller.isFinished() && currentFlingDirection != NONE) {
            // Snap to day after fling is finished
            goToNearestOrigin();
        } else {
            if (currentFlingDirection != NONE && shouldForceFinishScroll()) {
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

    public interface Listener {
        void onScaled();
        void onScrolled();
    }

}

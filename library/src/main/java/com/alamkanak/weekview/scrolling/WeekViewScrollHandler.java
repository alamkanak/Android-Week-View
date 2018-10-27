package com.alamkanak.weekview.scrolling;

import android.content.Context;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import com.alamkanak.weekview.ui.WeekView;
import com.alamkanak.weekview.data.WeekViewLoader;
import com.alamkanak.weekview.drawing.EventRect;
import com.alamkanak.weekview.drawing.WeekViewDrawingConfig;
import com.alamkanak.weekview.listeners.EmptyViewClickListener;
import com.alamkanak.weekview.listeners.EmptyViewLongPressListener;
import com.alamkanak.weekview.listeners.EventClickListener;
import com.alamkanak.weekview.listeners.EventLongPressListener;
import com.alamkanak.weekview.listeners.ScrollListener;
import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.model.WeekViewData;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.alamkanak.weekview.utils.DateUtils.today;

public class WeekViewScrollHandler {

    private Listener listener;

    private WeekViewData data;
    private WeekViewConfig config;
    private WeekViewDrawingConfig drawingConfig;

    public WeekViewScrollHandler(Context context, View view,
                                 final WeekViewConfig config,
                                 final WeekViewDrawingConfig drawingConfig, WeekViewData data) {
        this.listener = (Listener) view;

        this.data = data;
        this.config = config;
        this.drawingConfig = drawingConfig;

        gestureDetector = new GestureDetector(context, gestureListener);
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
                drawingConfig.newHourHeight = Math.round(config.hourHeight * detector.getScaleFactor());
                listener.onScaled();
                return true;
            }
        });
    }

    public GestureDetector gestureDetector;
    public OverScroller scroller;

    public WeekView.Direction currentScrollDirection = WeekView.Direction.NONE;
    public WeekView.Direction currentFlingDirection = WeekView.Direction.NONE;

    public ScaleGestureDetector scaleDetector;
    public boolean isZooming;

    public int minimumFlingVelocity = 0;
    public int scaledTouchSlop = 0;

    // Listeners
    public EventClickListener eventClickListener;
    public EventLongPressListener eventLongPressListener;
    public WeekViewLoader weekViewLoader;
    public EmptyViewClickListener emptyViewClickListener;
    public EmptyViewLongPressListener emptyViewLongPressListener;
    public ScrollListener scrollListener;

    private final GestureDetector.SimpleOnGestureListener gestureListener =
            new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDown(MotionEvent e) {
                    goToNearestOrigin();
                    return true;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    // Check if view is zoomed.
                    if (isZooming)
                        return true;

                    switch (currentScrollDirection) {
                        case NONE: {
                            // Allow scrolling only in one direction.
                            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                                if (distanceX > 0) {
                                    currentScrollDirection = WeekView.Direction.LEFT;
                                } else {
                                    currentScrollDirection = WeekView.Direction.RIGHT;
                                }
                            } else {
                                currentScrollDirection = WeekView.Direction.VERTICAL;
                            }
                            break;
                        }
                        case LEFT: {
                            // Change direction if there was enough change.
                            if (Math.abs(distanceX) > Math.abs(distanceY) && (distanceX < -scaledTouchSlop)) {
                                currentScrollDirection = WeekView.Direction.RIGHT;
                            }
                            break;
                        }
                        case RIGHT: {
                            // Change direction if there was enough change.
                            if (Math.abs(distanceX) > Math.abs(distanceY) && (distanceX > scaledTouchSlop)) {
                                currentScrollDirection = WeekView.Direction.LEFT;
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
                    if (isZooming)
                        return true;

                    if ((currentFlingDirection == WeekView.Direction.LEFT && !config.horizontalFlingEnabled) ||
                            (currentFlingDirection == WeekView.Direction.RIGHT && !config.horizontalFlingEnabled) ||
                            (currentFlingDirection == WeekView.Direction.VERTICAL && !config.verticalFlingEnabled)) {
                        return true;
                    }

                    scroller.forceFinished(true);

                    currentFlingDirection = currentScrollDirection;
                    switch (currentFlingDirection) {
                        case LEFT:
                        case RIGHT:
                            scroller.fling((int) drawingConfig.currentOrigin.x, (int) drawingConfig.currentOrigin.y, (int) (velocityX * config.xScrollingSpeed), 0, Integer.MIN_VALUE, Integer.MAX_VALUE, (int) -(config.hourHeight * 24 + drawingConfig.headerHeight + config.headerRowPadding * 2 + drawingConfig.headerMarginBottom + drawingConfig.timeTextHeight / 2 - listener.getViewHeight()), 0);
                            break;
                        case VERTICAL:
                            scroller.fling((int) drawingConfig.currentOrigin.x, (int) drawingConfig.currentOrigin.y, 0, (int) velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, (int) -(config.hourHeight * 24 + drawingConfig.headerHeight + config.headerRowPadding * 2 + drawingConfig.headerMarginBottom + drawingConfig.timeTextHeight / 2 - listener.getViewHeight()), 0);
                            break;
                    }

                    listener.onScrolled();
                    return true;
                }


                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    // If the tap was on an event then trigger the callback.
                    if (data.eventRects != null && eventClickListener != null) {
                        List<EventRect> reversedEventRects = data.eventRects;
                        Collections.reverse(reversedEventRects);
                        for (EventRect event : reversedEventRects) {
                            if (event.rectF != null && e.getX() > event.rectF.left && e.getX() < event.rectF.right && e.getY() > event.rectF.top && e.getY() < event.rectF.bottom) {
                                eventClickListener.onEventClick(event.originalEvent, event.rectF);
                                return super.onSingleTapConfirmed(e);
                            }
                        }
                    }

                    // If the tap was on in an empty space, then trigger the callback.
                    if (emptyViewClickListener != null && e.getX() > drawingConfig.headerColumnWidth && e.getY() > (drawingConfig.headerHeight + config.headerRowPadding * 2 + drawingConfig.headerMarginBottom)) {
                        Calendar selectedTime = getTimeFromPoint(e.getX(), e.getY());
                        if (selectedTime != null) {
                            emptyViewClickListener.onEmptyViewClicked(selectedTime);
                        }
                    }

                    return super.onSingleTapConfirmed(e);
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);

                    if (eventLongPressListener != null && data.eventRects != null) {
                        List<EventRect> reversedEventRects = data.eventRects;
                        Collections.reverse(reversedEventRects);
                        for (EventRect event : reversedEventRects) {
                            if (event.rectF != null && e.getX() > event.rectF.left && e.getX() < event.rectF.right && e.getY() > event.rectF.top && e.getY() < event.rectF.bottom) {
                                eventLongPressListener.onEventLongPress(event.originalEvent, event.rectF);
                                listener.performHapticFeedback();
                                return;
                            }
                        }
                    }

                    // If the tap was on in an empty space, then trigger the callback.
                    if (emptyViewLongPressListener != null && e.getX() > drawingConfig.headerColumnWidth && e.getY() > (drawingConfig.headerHeight + config.headerRowPadding * 2 + drawingConfig.headerMarginBottom)) {
                        Calendar selectedTime = getTimeFromPoint(e.getX(), e.getY());
                        if (selectedTime != null) {
                            listener.performHapticFeedback();
                            emptyViewLongPressListener.onEmptyViewLongPress(selectedTime);
                        }
                    }
                }
            };

    private void goToNearestOrigin() {
        double leftDays = drawingConfig.currentOrigin.x / (drawingConfig.widthPerDay + config.columnGap);

        if (currentFlingDirection != WeekView.Direction.NONE) {
            // snap to nearest day
            leftDays = Math.round(leftDays);
        } else if (currentScrollDirection == WeekView.Direction.LEFT) {
            // snap to last day
            leftDays = Math.floor(leftDays);
        } else if (currentScrollDirection == WeekView.Direction.RIGHT) {
            // snap to next day
            leftDays = Math.ceil(leftDays);
        } else {
            // snap to nearest day
            leftDays = Math.round(leftDays);
        }

        int nearestOrigin = (int) (drawingConfig.currentOrigin.x - leftDays * (drawingConfig.widthPerDay + config.columnGap));

        if (nearestOrigin != 0) {
            // Stop current animation.
            scroller.forceFinished(true);
            // Snap to date.
            scroller.startScroll((int) drawingConfig.currentOrigin.x, (int) drawingConfig.currentOrigin.y, -nearestOrigin, 0, (int) (Math.abs(nearestOrigin) / drawingConfig.widthPerDay * config.scrollDuration));
            listener.onScrolled();
        }
        // Reset scrolling and fling direction.
        currentScrollDirection = currentFlingDirection = WeekView.Direction.NONE;
    }

    /**
     * Get the time and date where the user clicked on.
     *
     * @param x The x position of the touch event.
     * @param y The y position of the touch event.
     * @return The time and date at the clicked position.
     */
    private Calendar getTimeFromPoint(float x, float y) {
        int leftDaysWithGaps = (int) -(Math.ceil(drawingConfig.currentOrigin.x / (drawingConfig.widthPerDay + config.columnGap)));
        float startPixel = drawingConfig.currentOrigin.x + (drawingConfig.widthPerDay + config.columnGap) * leftDaysWithGaps +
                drawingConfig.headerColumnWidth;
        for (int dayNumber = leftDaysWithGaps + 1;
             dayNumber <= leftDaysWithGaps + config.numberOfVisibleDays + 1;
             dayNumber++) {
            float start = (startPixel < drawingConfig.headerColumnWidth ? drawingConfig.headerColumnWidth : startPixel);
            if (drawingConfig.widthPerDay + startPixel - start > 0 && x > start && x < startPixel + drawingConfig.widthPerDay) {
                Calendar day = today();
                day.add(Calendar.DATE, dayNumber - 1);
                float pixelsFromZero = y - drawingConfig.currentOrigin.y - drawingConfig.headerHeight
                        - config.headerRowPadding * 2 - drawingConfig.timeTextHeight / 2 - drawingConfig.headerMarginBottom;
                int hour = (int) (pixelsFromZero / config.hourHeight);
                int minute = (int) (60 * (pixelsFromZero - hour * config.hourHeight) / config.hourHeight);
                day.add(Calendar.HOUR, hour);
                day.set(Calendar.MINUTE, minute);
                return day;
            }
            startPixel += drawingConfig.widthPerDay + config.columnGap;
        }
        return null;
    }

    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        boolean val = gestureDetector.onTouchEvent(event);

        // Check after call of gestureDetector, so currentFlingDirection and currentScrollDirection are set.
        if (event.getAction() == MotionEvent.ACTION_UP && !isZooming && currentFlingDirection == WeekView.Direction.NONE) {
            if (currentScrollDirection == WeekView.Direction.RIGHT || currentScrollDirection == WeekView.Direction.LEFT) {
                goToNearestOrigin();
            }
            currentScrollDirection = WeekView.Direction.NONE;
        }

        return val;
    }

    public void computeScroll() {
        if (scroller.isFinished()) {
            if (currentFlingDirection != WeekView.Direction.NONE) {
                // Snap to day after fling is finished.
                goToNearestOrigin();
            }
        } else {
            if (currentFlingDirection != WeekView.Direction.NONE && shouldForceFinishScroll()) {
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
    public boolean shouldForceFinishScroll() {
        return scroller.getCurrVelocity() <= minimumFlingVelocity;
    }

    public interface Listener {
        void onScaled();
        void onScrolled();
        int getViewHeight();
        void performHapticFeedback();
    }

}

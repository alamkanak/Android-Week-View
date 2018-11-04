package com.alamkanak.weekview.scrolling;

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

import java.util.Calendar;
import java.util.List;

import static android.view.KeyEvent.ACTION_UP;
import static com.alamkanak.weekview.ui.WeekView.Direction.LEFT;
import static com.alamkanak.weekview.ui.WeekView.Direction.NONE;
import static com.alamkanak.weekview.ui.WeekView.Direction.RIGHT;
import static com.alamkanak.weekview.ui.WeekView.Direction.VERTICAL;
import static com.alamkanak.weekview.utils.Constants.HOURS_PER_DAY;
import static com.alamkanak.weekview.utils.DateUtils.today;
import static java.lang.Math.max;
import static java.lang.Math.round;

public class WeekViewGestureHandler<T> {

    private Listener listener;

    private WeekViewData<T> data;
    private WeekViewConfig config;
    private WeekViewDrawingConfig drawingConfig;

    public WeekViewGestureHandler(Context context, View view,
                                  WeekViewConfig config, WeekViewData<T> data) {
        this.listener = (Listener) view;

        this.data = data;
        this.config = config;
        this.drawingConfig = config.drawingConfig;

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
                float hourHeight = WeekViewGestureHandler.this.config.hourHeight;
                drawingConfig.newHourHeight = round(hourHeight * detector.getScaleFactor());
                listener.onScaled();
                return true;
            }
        });
    }

    public OverScroller scroller;
    public WeekView.Direction currentScrollDirection = NONE;
    public WeekView.Direction currentFlingDirection = NONE;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleDetector;
    private boolean isZooming;

    private int minimumFlingVelocity = 0;
    private int scaledTouchSlop = 0;

    // Listeners
    private EventClickListener<T> eventClickListener;
    private EventLongPressListener<T> eventLongPressListener;

    private EmptyViewClickListener emptyViewClickListener;
    private EmptyViewLongPressListener emptyViewLongPressListener;

    private WeekViewLoader weekViewLoader;
    private ScrollListener scrollListener;

    private final GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

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

            float absDistanceX = Math.abs(distanceX);
            float absDistanceY = Math.abs(distanceY);

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
            final float halfTextHeight = drawingConfig.timeTextHeight / 2;

            final int minY = (int) (dayHeight + headerHeight + halfTextHeight - viewHeight) * (-1);
            final int maxY = 0;

            scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
        }

        private void onFlingVertical(float originalVelocityY) {
            int startX = (int) drawingConfig.currentOrigin.x;
            int startY = (int) drawingConfig.currentOrigin.y;

            int velocityX = 0;
            int velocityY = (int) originalVelocityY;

            int minX = Integer.MIN_VALUE;
            int maxX = Integer.MAX_VALUE;

            int dayHeight = config.hourHeight * HOURS_PER_DAY;
            int viewHeight = WeekView.getViewHeight();

            float headerHeight = drawingConfig.headerHeight
                    + config.headerRowPadding * 2
                    + drawingConfig.headerMarginBottom;
            float halfTextHeight = drawingConfig.timeTextHeight / 2;

            int minY = (int) (dayHeight + headerHeight + halfTextHeight - viewHeight) * (-1);
            int maxY = 0;

            scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            final List<EventChip<T>> eventChips = data.getAllEventChips();

            // TODO: Reduce code
            if (eventChips != null && eventClickListener != null) {
                for (EventChip<T> eventChip : eventChips) {
                    if (eventChip.isHit(e)) {
                        T data = eventChip.event.getData();

                        if (data != null) {
                            eventClickListener.onEventClick(data, eventChip.rect);
                        } else {
                            // TODO Exception
                        }

                        return super.onSingleTapConfirmed(e);
                    }
                }
            }

            // If the tap was on in an empty space, then trigger the callback.
            final float headerHeight = drawingConfig.headerHeight
                    + config.headerRowPadding * 2
                    + drawingConfig.headerMarginBottom;
            final float timeColumnWidth = drawingConfig.headerColumnWidth;

            if (emptyViewClickListener != null
                    && e.getX() > timeColumnWidth && e.getY() > headerHeight) {
                final Calendar selectedTime = getTimeFromPoint(e.getX(), e.getY());
                if (selectedTime != null) {
                    emptyViewClickListener.onEmptyViewClicked(selectedTime);
                }
            }

            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);

            List<EventChip<T>> eventChips = data.getAllEventChips();
            if (eventChips != null && eventLongPressListener != null) {
                for (EventChip<T> eventChip : eventChips) {
                    if (eventChip.isHit(e)) {
                        T data = eventChip.originalEvent.getData();

                        if (data != null) {
                            eventLongPressListener.onEventLongPress(data, eventChip.rect);
                            listener.performHapticFeedback();
                        } else {
                            // TODO Exception
                        }

                        return;
                    }
                }
            }

            float headerHeight = drawingConfig.headerHeight
                    + config.headerRowPadding * 2
                    + drawingConfig.headerMarginBottom;
            float timeColumnWidth = drawingConfig.headerColumnWidth;

            // If the tap was on in an empty space, then trigger the callback.
            if (emptyViewLongPressListener != null
                    && e.getX() > timeColumnWidth && e.getY() > headerHeight) {
                Calendar selectedTime = getTimeFromPoint(e.getX(), e.getY());
                if (selectedTime != null) {
                    listener.performHapticFeedback();
                    emptyViewLongPressListener.onEmptyViewLongPress(selectedTime);
                }
            }
        }
    };

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

    public WeekViewLoader getWeekViewLoader() {
        return weekViewLoader;
    }

    public void setWeekViewLoader(WeekViewLoader weekViewLoader) {
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
        float totalDayWidth = drawingConfig.widthPerDay + config.columnGap;
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

        int nearestOrigin = (int) (drawingConfig.currentOrigin.x - leftDays * totalDayWidth);

        if (nearestOrigin != 0) {
            // Stop current animation
            scroller.forceFinished(true);

            // Snap to date
            int startX = (int) drawingConfig.currentOrigin.x;
            int startY = (int) drawingConfig.currentOrigin.y;

            int distanceX = -nearestOrigin;
            int distanceY = 0;

            float daysScrolled = Math.abs(nearestOrigin) / drawingConfig.widthPerDay;
            int duration = (int) (daysScrolled * config.scrollDuration);

            scroller.startScroll(startX, startY, distanceX, distanceY, duration);
            listener.onScrolled();
        }

        // Reset scrolling and fling direction.
        currentScrollDirection = currentFlingDirection = NONE;
    }

    /**
     * Get the time and date where the user clicked on.
     *
     * @param touchX The x position of the touch event.
     * @param touchY The y position of the touch event.
     * @return The time and date at the clicked position.
     */
    private Calendar getTimeFromPoint(float touchX, float touchY) {
        float widthPerDay = drawingConfig.widthPerDay;
        float totalDayWidth = widthPerDay + config.columnGap;
        float originX = drawingConfig.currentOrigin.x;
        float timeColumnWidth = drawingConfig.headerColumnWidth;

        int leftDaysWithGaps = (int) (Math.ceil(originX / totalDayWidth) * (-1));
        float startPixel = originX + totalDayWidth * leftDaysWithGaps + timeColumnWidth;

        int begin = leftDaysWithGaps + 1;
        int end = leftDaysWithGaps + config.numberOfVisibleDays + 1;

        for (int dayNumber = begin; dayNumber <= end; dayNumber++) {
            float start = max(startPixel, timeColumnWidth);

            // TODO: Figure those out
            boolean b = widthPerDay + startPixel - start > 0;
            boolean isWithinDay = (touchX > start) & (touchX < startPixel + widthPerDay);

            if (b && isWithinDay) {
                Calendar day = today();
                day.add(Calendar.DATE, dayNumber - 1);

                float originY = drawingConfig.currentOrigin.y;
                float headerHeight = drawingConfig.headerHeight
                        + config.headerRowPadding * 2
                        + drawingConfig.headerMarginBottom;
                float halfTextHeight = drawingConfig.timeTextHeight / 2;
                float hourHeight = config.hourHeight;

                float pixelsFromZero = touchY - originY - halfTextHeight - headerHeight;
                int hour = (int) (pixelsFromZero / hourHeight);
                int minute = (int) (60 * (pixelsFromZero - hour * hourHeight) / hourHeight);
                day.add(Calendar.HOUR, hour);
                day.set(Calendar.MINUTE, minute);
                return day;
            }

            startPixel += totalDayWidth;
        }

        return null;
    }

    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        boolean val = gestureDetector.onTouchEvent(event);

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
        void performHapticFeedback(); // TODO: Remove?
    }

}

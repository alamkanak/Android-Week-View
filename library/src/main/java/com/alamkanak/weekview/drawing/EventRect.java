package com.alamkanak.weekview.drawing;

import android.graphics.RectF;
import android.view.MotionEvent;

import com.alamkanak.weekview.model.WeekViewEvent;

/**
 * A class to hold reference to the events and their visual representation. An EventRect is
 * actually the rectangle that is drawn on the calendar for a given event. There may be more
 * than one rectangle for a single event (an event that expands more than one day). In that
 * case two instances of the EventRect will be used for a single event. The given event will be
 * stored in "originalEvent". But the event that corresponds to rectangle the rectangle
 * instance will be stored in "event".
 */
public class EventRect {

    public WeekViewEvent event;
    public WeekViewEvent originalEvent;
    public RectF rectF;
    public float left;
    public float width;
    public float top;
    public float bottom;

    /**
     * Create a new instance of event rect. An EventRect is actually the rectangle that is drawn
     * on the calendar for a given event. There may be more than one rectangle for a single
     * event (an event that expands more than one day). In that case two instances of the
     * EventRect will be used for a single event. The given event will be stored in
     * "originalEvent". But the event that corresponds to rectangle the rectangle instance will
     * be stored in "event".
     *
     * @param event         Represents the event which this instance of rectangle represents.
     * @param originalEvent The original event that was passed by the user.
     * @param rectF         The rectangle.
     */
    public EventRect(WeekViewEvent event, WeekViewEvent originalEvent, RectF rectF) {
        this.event = event;
        this.rectF = rectF;
        this.originalEvent = originalEvent;
    }

    public boolean isHit(MotionEvent e) {
        if (rectF == null) {
            return false;
        }

        return e.getX() > rectF.left
                && e.getX() < rectF.right
                && e.getY() > rectF.top
                && e.getY() < rectF.bottom;
    }

}

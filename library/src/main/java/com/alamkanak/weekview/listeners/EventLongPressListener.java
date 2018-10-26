package com.alamkanak.weekview.listeners;

import android.graphics.RectF;

import com.alamkanak.weekview.model.WeekViewEvent;

public interface EventLongPressListener {

    /**
     * Similar to {@link com.alamkanak.weekview.listeners.EventClickListener} but with a long press.
     *
     * @param event:     event clicked.
     * @param eventRect: view containing the clicked event.
     */
    void onEventLongPress(WeekViewEvent event, RectF eventRect);

}

package com.alamkanak.weekview.interfaces;

import android.graphics.RectF;

import com.alamkanak.weekview.WeekViewEvent;

/**
 * Created by Thomas on 01/02/2016.
 */
public interface EventLongPressListener{
    /**
     * Similar to {@link com.alamkanak.weekview.WeekView.EventClickListener} but with a long press.
     * @param event: event clicked.
     * @param eventRect: view containing the clicked event.
     */
    void onEventLongPress(WeekViewEvent event, RectF eventRect);
}

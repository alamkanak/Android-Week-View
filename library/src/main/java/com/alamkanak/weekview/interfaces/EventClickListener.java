package com.alamkanak.weekview.interfaces;

import android.graphics.RectF;

import com.alamkanak.weekview.WeekViewEvent;

/**
 * Created by Thomas on 01/02/2016.
 */
public interface EventClickListener {
    /**
     * Triggered when clicked on one existing event
     * @param event: event clicked.
     * @param eventRect: view containing the clicked event.
     */
    void onEventClick(WeekViewEvent event, RectF eventRect);
}

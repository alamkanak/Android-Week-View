package com.alamkanak.weekview.listeners;

import android.graphics.RectF;

import com.alamkanak.weekview.model.WeekViewEvent;

public interface EventClickListener {

    /**
     * Triggered when clicked on one existing event
     *
     * @param event:     event clicked.
     * @param eventRect: view containing the clicked event.
     */
    void onEventClick(WeekViewEvent event, RectF eventRect);

}

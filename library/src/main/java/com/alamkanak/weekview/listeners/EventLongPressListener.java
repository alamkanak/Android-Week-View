package com.alamkanak.weekview.listeners;

import android.graphics.RectF;

public interface EventLongPressListener<T> {

    /**
     * Similar to {@link com.alamkanak.weekview.listeners.EventClickListener} but with a long press.
     *
     * @param data:     event clicked.
     * @param eventRect: view containing the clicked event.
     */
    void onEventLongPress(T data, RectF eventRect);

}

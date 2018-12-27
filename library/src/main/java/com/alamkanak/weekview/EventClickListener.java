package com.alamkanak.weekview;

import android.graphics.RectF;
import android.support.annotation.NonNull;

public interface EventClickListener<T> {

    /**
     * Triggered when clicked on one existing event
     *
     * @param data:     event clicked.
     * @param eventRect: view containing the clicked event.
     */
    void onEventClick(@NonNull T data, @NonNull RectF eventRect);

}

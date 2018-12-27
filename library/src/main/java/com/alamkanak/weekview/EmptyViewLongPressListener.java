package com.alamkanak.weekview;

import android.support.annotation.NonNull;

import java.util.Calendar;

public interface EmptyViewLongPressListener {

    /**
     * Similar to {@link EmptyViewClickListener} but with long press.
     *
     * @param time: {@link Calendar} object set with the date and time of the long pressed position on the view.
     */
    void onEmptyViewLongPress(@NonNull Calendar time);

}

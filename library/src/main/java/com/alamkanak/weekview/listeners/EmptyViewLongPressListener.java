package com.alamkanak.weekview.listeners;

import java.util.Calendar;

public interface EmptyViewLongPressListener {

    /**
     * Similar to {@link com.alamkanak.weekview.listeners.EmptyViewClickListener} but with long press.
     *
     * @param time: {@link Calendar} object set with the date and time of the long pressed position on the view.
     */
    void onEmptyViewLongPress(Calendar time);

}

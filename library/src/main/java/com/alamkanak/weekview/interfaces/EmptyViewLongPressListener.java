package com.alamkanak.weekview.interfaces;

import java.util.Calendar;

/**
 * Created by Thomas on 01/02/2016.
 */
public interface EmptyViewLongPressListener {
    /**
     * Similar to {@link com.alamkanak.weekview.WeekView.EmptyViewClickListener} but with long press.
     * @param time: {@link Calendar} object set with the date and time of the long pressed position on the view.
     */
    void onEmptyViewLongPress(Calendar time);
}

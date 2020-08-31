package com.alamkanak.weekview.interfaces;

import java.util.Calendar;

/**
 * Created by Thomas on 01/02/2016.
 */
public interface EmptyViewClickListener {
    /**
     * Triggered when the users clicks on a empty space of the calendar.
     * @param time: {@link Calendar} object set with the date and time of the clicked position on the view.
     */
    void onEmptyViewClicked(Calendar time);
}

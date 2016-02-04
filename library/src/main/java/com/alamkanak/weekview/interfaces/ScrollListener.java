package com.alamkanak.weekview.interfaces;

import java.util.Calendar;

/**
 * Created by Thomas on 01/02/2016.
 */
public interface ScrollListener {
    /**
     * Called when the first visible day has changed.
     *
     * (this will also be called during the first draw of the weekview)
     * @param newFirstVisibleDay The new first visible day
     * @param oldFirstVisibleDay The old first visible day (is null on the first call).
     */
    void onFirstVisibleDayChanged(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay);
}

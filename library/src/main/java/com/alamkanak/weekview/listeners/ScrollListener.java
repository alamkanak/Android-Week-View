package com.alamkanak.weekview.listeners;

import java.util.Calendar;

public interface ScrollListener {

    /**
     * Called when the first visible day has changed.
     * <p>
     * (this will also be called during the first draw of the WeekView)
     *
     * @param newFirstVisibleDay The new first visible day
     * @param oldFirstVisibleDay The old first visible day (is null on the first call).
     */
    void onFirstVisibleDayChanged(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay);

}

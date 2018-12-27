package com.alamkanak.weekview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;

public interface ScrollListener {

    /**
     * Called when the first visible day has changed.
     *
     * @param newFirstVisibleDay The new first visible day
     * @param oldFirstVisibleDay The old first visible day (is null on the first call).
     */
    void onFirstVisibleDayChanged(@NonNull Calendar newFirstVisibleDay,
                                  @Nullable Calendar oldFirstVisibleDay);

}

package com.alamkanak.weekview.data;

import com.alamkanak.weekview.model.WeekViewData;
import com.alamkanak.weekview.model.WeekViewEvent;
import com.alamkanak.weekview.model.WeekViewViewState;

import java.util.Calendar;
import java.util.List;

@Deprecated
public class WeekViewLoaderHelper {

    @Deprecated
    public static void load(WeekViewData data, WeekViewViewState viewState,
                            WeekViewLoader weekViewLoader, Calendar day) {
        int periodToFetch = (int) weekViewLoader.toWeekViewPeriodIndex(day);
        boolean isRefreshEligible = data.fetchedPeriod < 0
                || data.fetchedPeriod != periodToFetch
                || viewState.shouldRefreshEvents;

        if (isRefreshEligible) {
            List<? extends WeekViewEvent> previousPeriodEvents = null;
            List<? extends WeekViewEvent> currentPeriodEvents = null;
            List<? extends WeekViewEvent> nextPeriodEvents = null;

            if (data.previousPeriodEvents != null
                    && data.currentPeriodEvents != null && data.nextPeriodEvents != null) {
                if (periodToFetch == data.fetchedPeriod - 1) {
                    currentPeriodEvents = data.previousPeriodEvents;
                    nextPeriodEvents = data.currentPeriodEvents;
                } else if (periodToFetch == data.fetchedPeriod) {
                    previousPeriodEvents = data.previousPeriodEvents;
                    currentPeriodEvents = data.currentPeriodEvents;
                    nextPeriodEvents = data.nextPeriodEvents;
                } else if (periodToFetch == data.fetchedPeriod + 1) {
                    previousPeriodEvents = data.currentPeriodEvents;
                    currentPeriodEvents = data.nextPeriodEvents;
                }
            }

            if (currentPeriodEvents == null) {
                currentPeriodEvents = weekViewLoader.onLoad(periodToFetch);
            }

            if (previousPeriodEvents == null) {
                previousPeriodEvents = weekViewLoader.onLoad(periodToFetch - 1);
            }

            if (nextPeriodEvents == null) {
                nextPeriodEvents = weekViewLoader.onLoad(periodToFetch + 1);
            }

            // Clear events.
            data.getAllEventChips().clear();
            data.sortAndCacheEvents(previousPeriodEvents);
            data.sortAndCacheEvents(currentPeriodEvents);
            data.sortAndCacheEvents(nextPeriodEvents);

            data.previousPeriodEvents = previousPeriodEvents;
            data.currentPeriodEvents = currentPeriodEvents;
            data.nextPeriodEvents = nextPeriodEvents;
            data.fetchedPeriod = periodToFetch;
        }
    }

}

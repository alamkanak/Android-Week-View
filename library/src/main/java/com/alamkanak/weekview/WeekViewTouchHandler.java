package com.alamkanak.weekview;

import android.view.MotionEvent;

import java.util.Calendar;

import static com.alamkanak.weekview.DateUtils.today;
import static java.lang.Math.max;

final class WeekViewTouchHandler {

    private final WeekViewConfigWrapper config;

    WeekViewTouchHandler(WeekViewConfigWrapper config) {
        this.config = config;
    }

    /**
     * Returns the time and date where the user clicked on.
     *
     * @param event The {@link MotionEvent} of the touch event.
     * @return The time and date at the clicked position.
     */
    Calendar getTimeFromPoint(MotionEvent event) {
        final float touchX = event.getX();
        final float touchY = event.getY();

        final float widthPerDay = config.getWidthPerDay();
        final float totalDayWidth = widthPerDay + config.getColumnGap();
        final float originX = config.getCurrentOrigin().x;
        final float timeColumnWidth = config.getTimeColumnWidth();

        final int leftDaysWithGaps = (int) (Math.ceil(originX / totalDayWidth) * (-1));
        float startPixel = originX + totalDayWidth * leftDaysWithGaps + timeColumnWidth;

        final int begin = leftDaysWithGaps + 1;
        final int end = leftDaysWithGaps + config.getNumberOfVisibleDays() + 1;

        for (int dayNumber = begin; dayNumber <= end; dayNumber++) {
            final float start = max(startPixel, timeColumnWidth);

            final boolean isVisibleHorizontally = startPixel + widthPerDay - start > 0;
            final boolean isWithinDay = (touchX > start) & (touchX < startPixel + widthPerDay);

            if (isVisibleHorizontally && isWithinDay) {
                final Calendar day = today();
                day.add(Calendar.DATE, dayNumber - 1);

                final float originY = config.getCurrentOrigin().y;
                final float hourHeight = config.getHourHeight();

                final float pixelsFromZero = touchY - originY - config.getHeaderHeight();
                final int hour = (int) (pixelsFromZero / hourHeight);
                final int minute = (int) (60 * (pixelsFromZero - hour * hourHeight) / hourHeight);
                day.add(Calendar.HOUR, hour + config.getMinHour());
                day.set(Calendar.MINUTE, minute);
                return day;
            }

            startPixel += totalDayWidth;
        }

        return null;
    }

}

package com.alamkanak.weekview;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.style.StyleSpan;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.alamkanak.weekview.WeekViewUtil.*;

/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://april-shower.com
 */
public class WeekViewEvent {

    private static final Calendar sStartCalendar = Calendar.getInstance();
    private static final Calendar sEndCalendar = Calendar.getInstance();

    private long mId;
    private long mStartTime;
    private long mEndTime;
    private String mName;
    private String mLocation;
    private int mColor;
    private int mAvailableHeight;
    private int mAvailableWidth;
    private StaticLayout mTextLayout;
    private SpannableStringBuilder mStringBuilder = new SpannableStringBuilder();

    private boolean mAllDay;

    public WeekViewEvent(){

    }

    /**
     * Initializes the event for week view.
     * @param id The id of the event.
     * @param name Name of the event.
     * @param startYear Year when the event starts.
     * @param startMonth Month when the event starts.
     * @param startDay Day when the event starts.
     * @param startHour Hour (in 24-hour format) when the event starts.
     * @param startMinute Minute when the event starts.
     * @param endYear Year when the event ends.
     * @param endMonth Month when the event ends.
     * @param endDay Day when the event ends.
     * @param endHour Hour (in 24-hour format) when the event ends.
     * @param endMinute Minute when the event ends.
     */
    public WeekViewEvent(long id, String name, int startYear, int startMonth, int startDay, int startHour, int startMinute, int endYear, int endMonth, int endDay, int endHour, int endMinute) {
        this.mId = id;

        sStartCalendar.set(Calendar.YEAR, startYear);
        sStartCalendar.set(Calendar.MONTH, startMonth-1);
        sStartCalendar.set(Calendar.DAY_OF_MONTH, startDay);
        sStartCalendar.set(Calendar.HOUR_OF_DAY, startHour);
        sStartCalendar.set(Calendar.MINUTE, startMinute);

        mStartTime = sStartCalendar.getTimeInMillis();

        sEndCalendar.set(Calendar.YEAR, endYear);
        sEndCalendar.set(Calendar.MONTH, endMonth-1);
        sEndCalendar.set(Calendar.DAY_OF_MONTH, endDay);
        sEndCalendar.set(Calendar.HOUR_OF_DAY, endHour);
        sEndCalendar.set(Calendar.MINUTE, endMinute);

        mEndTime = sEndCalendar.getTimeInMillis();

        this.mName = name;
    }

    /**
     * Initializes the event for week view.
     * @param id The id of the event.
     * @param name Name of the event.
     * @param location The location of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     * @param allDay Is the event an all day event.
     */
    public WeekViewEvent(long id, String name, String location, Calendar startTime, Calendar endTime, boolean allDay) {
        this.mId = id;
        this.mName = name;
        this.mLocation = location;
        this.mStartTime = startTime.getTimeInMillis();
        this.mEndTime = endTime.getTimeInMillis();
        this.mAllDay = allDay;
    }

    /**
     * Initializes the event for week view.
     * @param id The id of the event.
     * @param name Name of the event.
     * @param location The location of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     * @param allDay Is the event an all day event.
     */
    public WeekViewEvent(long id, String name, String location, long startTime, long endTime, boolean allDay) {
        this.mId = id;
        this.mName = name;
        this.mLocation = location;
        this.mStartTime = startTime;
        this.mEndTime = endTime;
        this.mAllDay = allDay;
    }


    /**
     * Initializes the event for week view.
     * @param id The id of the event.
     * @param name Name of the event.
     * @param location The location of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     */
    public WeekViewEvent(long id, String name, String location, Calendar startTime, Calendar endTime) {
        this(id, name, location, startTime, endTime, false);
    }

    /**
     * Initializes the event for week view.
     * @param id The id of the event.
     * @param name Name of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     */
    public WeekViewEvent(long id, String name, Calendar startTime, Calendar endTime) {
        this(id, name, null, startTime, endTime);
    }


    public long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(Calendar startTime) {
        this.mStartTime = startTime.getTimeInMillis();
    }

    public void setStartTime(long startTime) {
        mStartTime = startTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public void setEndTime(Calendar endTime) {
        this.mEndTime = endTime.getTimeInMillis();
    }

    public void setEndTime(long endTime) {
        mEndTime = endTime;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
        refreshStringBuilder();
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        this.mLocation = location;
        // Prepare the location of the event.
        refreshStringBuilder();
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public boolean isAllDay() {
        return mAllDay;
    }

    public void setAllDay(boolean allDay) {
        this.mAllDay = allDay;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public int getmAvailableHeight() {
        return mAvailableHeight;
    }

    public void setmAvailableHeight(int mAvailableHeight) {
        this.mAvailableHeight = mAvailableHeight;
    }

    public int getmAvailableWidth() {
        return mAvailableWidth;
    }

    public void setmAvailableWidth(int mAvailableWidth) {
        this.mAvailableWidth = mAvailableWidth;
    }

    public StaticLayout getTextLayout() {
        return mTextLayout;
    }

    public void setTextLayout(StaticLayout mTextLayout) {
        this.mTextLayout = mTextLayout;
    }

    public SpannableStringBuilder getStringBuilder() {
        return mStringBuilder;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WeekViewEvent that = (WeekViewEvent) o;

        return mId == that.mId;

    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }

    private static Calendar sEndTimeCalendar = Calendar.getInstance();
    private static Calendar sOverDayCalendar = Calendar.getInstance();
    public List<WeekViewEvent> splitWeekViewEvents(){
        //This function splits the WeekViewEvent in WeekViewEvents by day
        List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();
        // The first millisecond of the next day is still the same day. (no need to split events for this).
        if (!isSameDay(mStartTime, mEndTime - 1)) {
            sEndTimeCalendar.setTimeInMillis(mStartTime);
            sEndTimeCalendar.set(Calendar.HOUR_OF_DAY, 23);
            sEndTimeCalendar.set(Calendar.MINUTE, 59);
            WeekViewEvent event1 = new WeekViewEvent(this.getId(), this.getName(), this.getLocation(), this.getStartTime(), sEndCalendar.getTimeInMillis(), this.isAllDay());
            event1.setColor(this.getColor());
            events.add(event1);

            // Add other days.
            sEndTimeCalendar.add(Calendar.DATE, 1);
            while (!isSameDay(sEndTimeCalendar.getTimeInMillis(), mEndTime)) {
                sOverDayCalendar.setTimeInMillis(sEndTimeCalendar.getTimeInMillis());
                sOverDayCalendar.set(Calendar.HOUR_OF_DAY, 0);
                sOverDayCalendar.set(Calendar.MINUTE, 0);
                long startOfOverDay = sOverDayCalendar.getTimeInMillis();
                sOverDayCalendar.set(Calendar.HOUR_OF_DAY, 23);
                sOverDayCalendar.set(Calendar.MINUTE, 59);
                long endOfOverDay = sOverDayCalendar.getTimeInMillis();
                WeekViewEvent eventMore = new WeekViewEvent(mId, mName, null,
                    startOfOverDay, endOfOverDay, mAllDay);
                eventMore.setColor(mColor);
                events.add(eventMore);

                // Add next day.
                sEndTimeCalendar.add(Calendar.DATE, 1);
            }

            // Add last day.
            sEndTimeCalendar.setTimeInMillis(mEndTime);
            sEndTimeCalendar.set(Calendar.HOUR_OF_DAY, 0);
            sEndTimeCalendar.set(Calendar.MINUTE, 0);
            WeekViewEvent event2 = new WeekViewEvent(mId, mName, mLocation,
                sEndTimeCalendar.getTimeInMillis(), mEndTime, mAllDay);
            event2.setColor(mColor);
            events.add(event2);
        } else {
            events.add(this);
        }

        return events;
    }

    private void refreshStringBuilder() {
        mStringBuilder.clear();
        if (mName != null) {
            mStringBuilder.append(mName);
            mStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, mStringBuilder.length(), 0);
            mStringBuilder.append(' ');
        }
        if (mLocation != null) {
            mStringBuilder.append(mLocation);
        }
    }
}

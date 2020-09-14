package com.alamkanak.weekview.interfaces;

import com.alamkanak.weekview.WeekViewEvent;

import java.util.List;

/**
 * Created by Thomas on 22/02/2016.
 */

public interface MonthChangeListener{
    /**
     * Very important interface, it's the base to load events in the calendar.
     * This method is called three times: once to load the previous month, once to load the next month and once to load the current month.<br/>
     * <strong>That's why you can have three times the same event at the same place if you mess up with the configuration</strong>
     * @param newYear : year of the events required by the view.
     * @param newMonth : month of the events required by the view <br/><strong>1 based (not like JAVA API) --> January = 1 and December = 12</strong>.
     * @return a list of the events happening <strong>during the specified month</strong>.
     */
    List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth);
}

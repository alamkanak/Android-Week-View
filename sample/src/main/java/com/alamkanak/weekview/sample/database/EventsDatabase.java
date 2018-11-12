package com.alamkanak.weekview.sample.database;

import com.alamkanak.weekview.WeekViewDisplayable;
import com.alamkanak.weekview.sample.apiclient.Event;

import java.util.Calendar;
import java.util.List;

public interface EventsDatabase {

    List<WeekViewDisplayable<Event>> getEventsInRange(Calendar startDate, Calendar endDate);

}

package com.alamkanak.weekview.sample.database;

import com.alamkanak.weekview.model.WeekViewDisplayable;

import java.util.Calendar;
import java.util.List;

public interface EventsDatabase {

    List<WeekViewDisplayable> getEventsInRange(Calendar startDate, Calendar endDate);

}

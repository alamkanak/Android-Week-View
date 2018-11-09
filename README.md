Android Week View
=================

**Android Week View** is an Android library for displaying calendar views within an app. It was initially developed by [Raquib-ul Alam](https://github.com/alamkanak), but is not currently maintained and does not work when using API level 28. Therefore, I’m providing this fork. 

Usage
---------
1. Add the JitPack repository to your project-level build file and the dependency to the app-level build file.
```groovy
// build.gradle (project-level)
allprojects {
 repositories {
  ...
  maven { url 'https://jitpack.io' }
 }
}

// build.gradle (app-level)
implementation 'com.github.thellmund:Android-Week-View:1.3' // Bugfix-only version
implementation 'com.github.thellmund:Android-Week-View:3.0' // Version with updated API
```

2. Add `WeekView` in your XML layout.
```xml
<com.alamkanak.weekview.ui.WeekView
    android:id="@+id/weekView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:eventTextColor="@color/white"
    app:textSize="12sp"
    app:hourHeight="60dp"
    app:headerColumnPadding="8dp"
    app:headerColumnTextColor="@color/light_blue"
    app:headerRowPadding="12dp"
    app:columnGap="8dp"
    app:noOfVisibleDays="3"
    app:headerRowBackgroundColor="@color/light_gray"
    app:dayBackgroundColor="@color/white"
    app:todayBackgroundColor="@color/light_blue"
    app:headerColumnBackground="@color/white"/>
```

3. Prepare the class of objects that you want to display in `WeekView` by implementing `WeekViewDisplayable<T>`.
```java
public class CalendarItem implements WeekViewDisplayable<CalendarItem> {

    private long id;
    private String title;
    private DateTime startTime;
    private DateTime endTime;
    private String location;
    private int color;
    
    /* ... */
    
    @Override
    public WeekViewEvent<CalendarItem> toWeekViewEvent() {
        // Note: It's important to pass "this" as the last argument to WeekViewEvent's constructor.
        // This way, the EventClickListener can return this object in its onEventClick() method.
        boolean isAllDay = DateUtils.isAllDay(startTime, endTime);
        return new WeekViewEvent<>(
            id, title, startTime.toGregorianCalendar(), 
            endTime.toGregorianCalendar(), location, color, isAllDay, this
        );
    }

}
```

4. Configure `WeekView` in code.
```java
WeekView<CalendarItem> weekView = (WeekView) findViewById(R.id.weekView);
weekView.setOnEventClickListener(new EventClickListener<CalendarItem>() {
    @Override
    public void onEventClick(CalendarItem event, RectF eventRect) {
        // Do something with the CalendarItem
    }
});

// WeekView has infinite horizontal scrolling. Therefore, you need to provide the events 
// of a month whenever that the currently displayed month changes.
weekView.setMonthChangeListener(new MonthLoader.MonthChangeListener<CalendarItem>() {
    @Override
    public List<WeekViewDisplayable<CalendarItem>> onMonthChange(Calendar startDate, Calendar endDate) {
        return mDatabase.getCalendarItemsInRange(startDate, endDate);
    }
});
```

--- 

Customization
-------------------

Many aspects of `WeekView` and the individual event chips can be customized.

- `allDayEventHeight`: All-day events are displayed in a header row at the top. This attributes specifies their height. If not set, these events will be set to the height of their content.
- `columnGap`: The horizontal gap between individual days.
- `dayBackgroundColor`: The background color of the current day, e.g. to emphasize it in a multi-day view.
- `eventMarginVertical`: The vertical gap between back-to-back events.
- `eventPadding`: The gap between the event title and the border of its event chip. 
- `eventTextColor`: The color of the event title and location in the event chip.
- `eventTextSize`: The size of text in the event chip.
- `firstDayOfWeek`: The first day of the week. Default is Monday.
- `headerColumnBackground`: The background of the time column which is displayed on the left-hand side.
- `headerColumnPadding`
- `headerColumnTextColor`: The color of the hour text in the time column.
- `headerRowBackgroundColor`: The background color of the header row which runs at the top of `WeekView`.
- `headerRowPadding`: The padding within the header row.
- `hourHeight`: The height with with one hour is displayed. Default is 50dp.
- `hourSeparatorColor`: The color of the horizontal lines that indicate the hours.
- `hourSeparatorHeight`: The height of the horizontal hour lines.
- `noOfVisibleDays`: The number of days that are visible. Default is 3.
- `overlappingEventGap`: The horizontal gap between events that occur concurrently.
- `textSize`: The text size used for text in the time column and the days row, which contains the currently displayed days.
- `todayBackgroundColor`: The background color of the current day.
- `todayHeaderTextColor`: The text color of the current day in the days row.
- `showDistinctPastFutureColor`: Indicates whether distinct background colors should be used for past and future days. Default is false.
- `futureBackgroundColor`: The background color to use for future days. Requires `showDistinctPastFutureColor` to be true.
- `pastBackgroundColor`: The background color to use for past days. Requires `showDistinctPastFutureColor` to be true.
- `showDistinctWeekendColor`: Indicates whether weekends should be marked with a distinct color. Default is false.
- `futureWeekendBackgroundColor`: The background color of future weekend days. Requires `showDistinctWeekendColor` to be true.
- `pastWeekendBackgroundColor`: The background color of past weekend days. Requires `showDistinctWeekendColor` to be true.
- `showNowLine`: Indicates whether a horizontal line should be displayed at the current time. Default is false.
- `nowLineColor`: The color of the now line. 
- `nowLineThickness`: The height of the now line.
- `scrollDuration`: The duration of automatic scrolling. The value determines how fast `WeekView` scrolls to a particular day.

---

Interfaces
-------------------

The following interfaces are used to provide data and interactability to `WeekView`.  
- `setMonthChangeListener()` to provide events to the calendar by months
- `setOnEventClickListener()` to get a callback when an event is clicked
- `setEventLongPressListener()` to get a callback when an event is long pressed
- `setDateTimeInterpreter()` to set your own labels for the calendar header row and header column
- `setWeekViewLoader()` to provide events to the calendar
- `setEmptyViewClickListener()` to get a callback when any empty space is clicked
- `setEmptyViewLongPressListener()` to get a callback when any empty space is long pressed
- `setScrollListener()` to get an event every time the first visible day has changed

---

[Original README] Android Week View
=================

![](images/screen-shot.png)

Features
------------

* Week view calendar
* Day view calendar
* Custom styling
* Horizontal and vertical scrolling
* Infinite horizontal scrolling
* Live preview of custom styling in xml preview window

Who uses it
---------------

* [Series Addict](https://play.google.com/store/apps/details?id=com.alamkanak.seriesaddict)
* Using the library? Just [tweet me](https://twitter.com/alamkanak) or [send me an email](mailto:alam.kanak@gmail.com).

Usage
---------

1. Import the library into your project.
  * Grab via maven
  
    ```xml
    <dependency>
      <groupId>com.github.alamkanak</groupId>
      <artifactId>android-week-view</artifactId>
      <version>1.2.6</version>
      <type>aar</type>
    </dependency>
    ```
  * Grab via gradle
  
    ```groovy
    compile 'com.github.alamkanak:android-week-view:1.2.6'
    ```
2. Add WeekView in your xml layout.

    ```xml
    <com.alamkanak.weekview.ui.WeekView
            android:id="@+id/weekView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:eventTextColor="@android:color/white"
            app:textSize="12sp"
            app:hourHeight="60dp"
            app:headerColumnPadding="8dp"
            app:headerColumnTextColor="#8f000000"
            app:headerRowPadding="12dp"
            app:columnGap="8dp"
            app:noOfVisibleDays="3"
            app:headerRowBackgroundColor="#ffefefef"
            app:dayBackgroundColor="#05000000"
            app:todayBackgroundColor="#1848adff"
            app:headerColumnBackground="#ffffffff"/>
    ```
3. Write the following code in your java file.

    ```java
    // Get a reference for the week view in the layout.
    mWeekView = (WeekView) findViewById(R.id.weekView);

    // Set an action when any event is clicked.
    mWeekView.setOnEventClickListener(mEventClickListener);

    // The week view has infinite scrolling horizontally. We have to provide the events of a
    // month every time the month changes on the week view.
    mWeekView.setMonthChangeListener(mMonthChangeListener);

    // Set long press listener for events.
    mWeekView.setEventLongPressListener(mEventLongPressListener);
    ```
4. Implement `WeekView.MonthChangeListener`, `WeekView.EventClickListener`, `WeekView.EventLongPressListener` according to your need.

5. Provide the events for the `WeekView` in `WeekView.MonthChangeListener.onMonthChange()` callback. Please remember that the calendar pre-loads events of three consecutive months to enable lag-free scrolling.

    ```java
    MonthLoader.MonthChangeListener mMonthChangeListener = new MonthLoader.MonthChangeListener() {
        @Override
        public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
            // Populate the week view with some events.
            List<WeekViewEvent> events = getEvents(newYear, newMonth);
            return events;
        }
    };
    ```

Customization
-------------------

You can customize the look of the `WeekView` in xml. Use the following attributes in xml. All these attributes also have getters and setters to enable you to change the style dynamically.

- `allDayEventHeight`
- `columnGap`
- `dayBackgroundColor`
- `dayNameLength`
- `eventMarginVertical`
- `eventPadding`
- `eventTextColor`
- `eventTextSize`
- `firstDayOfWeek`
- `headerColumnBackground`
- `headerColumnPadding`
- `headerColumnTextColor`
- `headerRowBackgroundColor`
- `headerRowPadding`
- `hourHeight`
- `hourSeparatorColor`
- `hourSeparatorHeight`
- `noOfVisibleDays`
- `overlappingEventGap`
- `textSize`
- `todayBackgroundColor`
- `todayHeaderTextColor`
- `showDistinctPastFutureColor`
- `futureBackgroundColor`
- `pastBackgroundColor`
- `showDistinctWeekendColor`
- `futureWeekendBackgroundColor`
- `pastWeekendBackgroundColor`
- `showNowLine`
- `nowLineColor`
- `nowLineThickness`
- `scrollDuration`

Interfaces
----------

Use the following interfaces according to your need.

- `mWeekView.setWeekViewLoader()` to provide events to the calendar
- `mWeekView.setMonthChangeListener()` to provide events to the calendar by months
- `mWeekView.setOnEventClickListener()` to get a callback when an event is clicked
- `mWeekView.setEventLongPressListener()` to get a callback when an event is long pressed
- `mWeekView.setEmptyViewClickListener()` to get a callback when any empty space is clicked
- `mWeekView.setEmptyViewLongPressListener()` to get a callback when any empty space is long pressed
- `mWeekView.setDateTimeInterpreter()` to set your own labels for the calendar header row and header column
- `mWeekView.setScrollListener()` to get an event every time the first visible day has changed

Sample
----------

There is also a [sample app](https://github.com/alamkanak/Android-Week-View/tree/master/sample) to get you started.

To do
-------

* Add event touch feedback selector
* Show events that expand multiple days properly

Changelog
---------

**Version 1.2.6**

* Add empty view click listener
* Fix padding bug
* Fix bug when setting colors of different components
* Add ability to turn off fling gesture
* Add example of how to load events asynchronously in the sample app

**Version 1.2.5**

* Add support for using subclasses of `WeekViewEvent`
* Fix scroll animation
* Add support for semi-transparent header colors

**Version 1.2.4**

* **NOTE:** If you are using `WeekView.MonthChangeListener`, make sure to change it into `MonthLoader.MonthChangeListener`
* Add support to have loaders other than MonthViewLoader
* Add pinch to zoom support
* Add support for location
* Add ability to have different colors for past, future, weekend days
* Add support for "now" line

**Version 1.2.3**

* Get callbacks when scrolling horizontally
* `goToHour` and `goToDate` methods has been fixed
* Use `getFirstVisibleHour` method to get the first visible hour in the week view

**Version 1.2.1**

* Better scrolling added
* Get callbacks when empty view is tapped/long pressed
* Control the speed of scrolling
* Support for multiple language added
* Ability to set your own interpreter for header row and column added

**Version 1.1.7**

* You can now dynamically scroll to an hour of your preference.

**Version 1.1.6**

* Added support for events that expands to multiple days

**Version 1.1.5**

* A bug related to overlapping events fixed
* You can now programmatically get first and last visible day in the week view

**Version 1.1.4**

* Small bug fixed

**Version 1.1.3**

* Margins support added for overlapping events

**Version 1.1.2**

* Small bugs fixed
* Hour separator inconsistency fixed

**Version 1.1.1**

* Overlapping event bug fixed

**Version 1.1.0**

* Added support for overlapping events

License
----------

    Copyright 2014 Raquib-ul-Alam

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

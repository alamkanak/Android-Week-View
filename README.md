Android Week View
=================

**Android Week View** is an android library to display calendars (week view or day view) within the app. It supports custom styling.

![](images/screen-shot.png)

> Please note that this project is still under development. I will be happy if you collaborate in this project and help it become even more polished.

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

* [Series Addict](http://seriesaddict.april-shower.com) (still under development)

Usage
---------

1. Import the library into your project.
  * Grab via maven
  
    ```xml
    <dependency>
      <groupId>com.github.alamkanak</groupId>
      <artifactId>android-week-view</artifactId>
      <version>1.0.3</version>
      <type>aar</type>
    </dependency>
    ```
  * Grab via gradle
  
    ```groovy
    compile 'com.github.alamkanak:android-week-view:1.0.3'
    ```
2. Add WeekView in your xml layout.

    ```xml
    <com.alamkanak.weekview.WeekView
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

5. Provide the events for the `WeekView` in `WeekView.MonthChangeListener.onMonthChange()` callback. Please remember that the calendar preloads events of three consecutive months to enable lag-free scrolling.

    ```java
    WeekView.MonthChangeListener mMonthChangeListener = new WeekView.MonthChangeListener() {
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

- `columnGap`
- `dayBackgroundColor`
- `dayNameLength`
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
- `textSize`
- `todayBackgroundColor`
- `todayHeaderTextColor`

Sample
----------

There is also a [sample app](https://github.com/alamkanak/Android-Week-View/tree/master/sample) to get you started.

To do
-------

* Show overlapping events side by side
* Add event touch feedback selector
* Show events that expand multiple days properly

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

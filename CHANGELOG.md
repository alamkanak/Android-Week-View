Changelog
=========

## Version 3.3
*(2019-03-17)*
- New: You can now define the time range to be displayed for each day by setting `minHour` and `maxHour` in your layout XML, or by calling `weekView.setMinHour()` and `weekView.setMaxHour()` in your code.
- New: You can now add the option to automatically scroll to the current time when `WeekView` is first displayed by setting `showCurrentTimeFirst` to `true`.
- New: You can opt to use a multi-line date header by setting `singleLineHeader` to `false`.
- Fix: The attribute `showNowLine` is no longer ignored.

Thanks to [Bwaim](https://github.com/Bwaim) and [Mauker1](https://github.com/Mauker1) for contributing to this release!

## Version 3.2.1
*(2019-02-15)*
- This release fixes a JitPack build error, which caused an exception when adding this library to a project. 

## Version 3.2
*(2019-02-15)*
- New: Better interoperability with Kotlin by adding nullability information and lambda methods to WeekView
- New: You can limit the date range of the calendar via `weekView.setMinDate(date)` and `weekView.setMaxDate(date)`.
- New: You can customize the interval of hours displayed in the time column.
- New: You can choose to display the hour separators in the time column.
- New: You can set a custom text color for individual events, via `weekViewEvent.setTextColor(int textColor)`.
- New: The text size of all-day events can now be set via `allDayEventTextSize`. The height of all-day event chips will adapt accordingly. In the same breath, this release deprecates `allDayEventHeight`.
- Improved: Events that span multiple days no longer show a corner radius at the end of a day if they continue on the next day, or at the beginning of a day if they began on the previous day.
- Improved: The paddings in the header have been tweaked to be more consistent.
- Fixed: Events after the change to daylight saving time were displayed on the wrong date.
- Fixed: Changing the number of days no longer scrolls the calendar back to the current date.
- Fixed: When `showFirstDayOfWeekFirst` is set, WeekView now displays the correct day, depending on your setting of `firstDayOfWeek`.
- Fixed: While scrolling, the header would sometimes shrink even if all-day events were visible. This is no longer the case.

Thanks [Bwaim](https://github.com/Bwaim) and [cs8898](https://github.com/cs8898) for contributing to this release!

## Version 3.1.3
*(2018-12-01)*
- Fix bug where `onMonthChange` was called with the wrong month (thanks for submitting the issue, [tylermarien](https://github.com/tylermarien)!)

## Version 3.1.2
*(2018-11-25)*
- Fix issue where a day’s events weren’t displayed until the user finished scrolling
- Fix bug where events longer than one day weren’t drawn (thanks for the fix, [Menthuss](https://github.com/Menthuss)!)

## Version 3.1.1
*(2018-11-14)*
- Fix bugs related to day and hour separators (thanks [SapuSeven](https://github.com/SapuSeven)!)

## Version 3.1
*(2018-11-13)*
- Update naming of methods and view attributes. While this unfortunately is another breaking change, the updated naming should be more future-prove.
- Add a customizable bottom line below the header (off by default).
- Add a customizable vertical line between the time column and the events area (off by default).
- Add option `horizontalScrollingEnabled` to completely disable horizontal scrolling. This is helpful for static calendar views that always show a particular time span. 
- Save number of visible days on orientation change, so that it doesn’t go back to the initial value after a device rotation.

## Version 3.0
*(2018-11-04)*
- Refactor the project for improved understandability (contributions welcome!)
- Change `EventClickListener` to return the class that is provided in `onMonthChange(startDate, endDate)` instead of a `WeekViewEvent`

## Version 2.0.1
*(2018-09-19)*
- Introduce attribute `singleDayHorizontalMargin` to specify the start and end margin for events in the single-day view

## Version 2.0
*(2018-09-08)*
- Introduce `WeekViewDisplayable` interface

## Earlier releases
- See: [original repository](https://github.com/alamkanak/Android-Week-View)

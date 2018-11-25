Changelog
=========

## Version 3.1.2
*(2018-11-25)*
- Fix issue where a day’s events weren’t displayed until the user finished scrolling.
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

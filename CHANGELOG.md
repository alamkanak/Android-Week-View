Changelog
=========

## Version 5.0.0-beta02
*(2020-09-30)*
- New: You can now set the text color for weekend date labels via `weekendTextColor`.
- Fixed: WeekView no longer keeps outdated events around if an empty list of events was submitted through `WeekView.SimpleAdapter`.
- Fixed: WeekView no longer uses incorrect dates for `minDate` and `maxDate`.

## Version 5.0.0-beta01
*(2020-09-29)*
This beta release contains new functionality and includes breaking changes. To get started, take a look at the [wiki](https://github.com/thellmund/Android-Week-View/wiki/Getting-started-(v5-beta)).
- New: You can now use `SpannableString` for event titles and locations to provide custom styling.
- New: You can now choose to show the current week number in the header.
- New: You can now elevate the header by providing a header row bottom shadow.
- Changed: WeekView now relies on `WeekView.Adapter<T>` for submitting events and providing callbacks. As a consequence, all listeners used for click and scroll callbacks have been removed.
- Changed: WeekView now uses `DateFormatter` and `TimeFormatter` to format date and time labels. `DateTimeInterpreter` has been deprecated and will be removed soon.
- Changed: WeekView now scrolls more naturally. In the process, multiple scrolling-related attributes have been deprecated.
- Changed: WeekView now automatically handles newline characters in date labels. As a consequence, the `singleLineHeader` attributes has been removed.
- Changed: WeekView no longer uses a generic. As a consequence, you no longer need to use `findViewById<WeekView<T>>`.
- Changed: WeekView no longer replaces events of months that have already been cached when using a paginated approach. To force-refresh the cache, call `refresh()` on an implementation of `WeekView.PagingAdapter`.
- Fixed: WeekView no longer crashes with a `ConcurrentModificationException` when submitting new events in quick succession.
- Fixed: WeekView no longer renders borders of multi-day events incorrectly.
- Fixed: WeekView no longer draws the day background incorrectly when `minHour` is set.

Thanks for contributing, [FeFelten](https://github.com/FeFelten)!

## Version 4.1.6
*(2020-05-10)*
- Fixed: WeekView would crash if restoring the state before `firstVisibleDate` was initialized.

Thanks for fixing this, [shuirna](https://github.com/shuirna)!

## Version 4.1.5
*(2020-02-09)*
- Fixed: WeekView would crash on submitting new events when the previous submit was an empty list of events.

Thanks for reporting the issue, [michaelbukachi](https://github.com/michaelbukachi)!

## Version 4.1.4
*(2020-02-06)*
- Fixed: `goToHour(hour)` scrolled to the wrong time if `minHour` was set.
- Changed: `goToHour(hour)` now throws an `IllegalArgumentException` if `hour` is outside of the time range constructed by `minHour` and `maxHour`.

Thanks for reporting the issue, [Mkryglikov](https://github.com/Mkryglikov)!

## Version 4.1.3
*(2020-01-30)*
- Fixed: Clicks on event chips weren't recognized after zooming in some cases.

Thanks for reporting these issues, [Mkryglikov](https://github.com/Mkryglikov)!

## Version 4.1.2
*(2020-01-19)*
- Fixed: Dynamically setting the time column background via `setTimeColumnBackgroundColor()` didn't work.
- Fixed: When `showFirstDayOfWeekFirst`, WeekView would sometimes show the incorrect week.

Thanks for reporting these issues, [Huakas](https://github.com/Huakas)!

## Version 4.1.1
*(2020-01-13)*
- Fixed: WeekView no longer crashes when `EmojiCompat` is not used in the application.

Thanks for reporting this, [Huakas](https://github.com/Huakas)!

## Version 4.1
*(2019-12-09)*
- New: Emojis in event titles and locations are now displayed correctly.
- New: When [TalkBack](https://support.google.com/accessibility/android/answer/6283677?hl=en) is turned on, `WeekView` now provides accessibility support for interacting with events.

## Version 4.0.1
*(2019-11-01)*
- Fixed: Event chips no longer disappear when scrolling in some cases.
- Fixed: Multi-day all-day events are no longer cut off after the first day.

Thanks for reporting issues, [MohammadB72](https://github.com/MohammadB72) and [verzhbitski](https://github.com/verzhbitski)!

## Version 4.0.0
*(2019-09-29)*

This release includes many new features and breaking changes.
- New: Providing events to `WeekView` is now easier. Check out the [wiki](https://github.com/thellmund/Android-Week-View/wiki) to find out more.
- New: You can use JodaTime, JSR-310, and ThreeTenABP with `WeekView`. Check out the [wiki](https://github.com/thellmund/Android-Week-View/wiki/Extensions) to find out more. 
- New: When building a `WeekViewEvent` via `WeekViewEvent.Builder`, you can pass in resource IDs for the title and location.
- New: When declaring the style of a `WeekViewEvent` via `WeekViewEvent.Style.Builder`, you can pass in resource IDs for color and dimension properties.
- New: You can use custom fonts with `WeekView` by setting `fontFamily`, `typeface`, and `textStyle` in the XML layout file.
- New: You can set the text in event chips to adapt to the chip’s height by setting `isAdaptiveEventTextSize = true` or `app:adaptiveEventTextSize="true"`.
- Changed: `WeekView` now uses AndroidX instead of the old Support Library.
- Changed: `WeekView` is now a 100% Kotlin project.
- Changed: `WeekViewLoader` is removed; use any of the new `EventsLoader`s instead.
- Changed: The naming of various listeners and setter methods is more unified. Check out the [wiki](https://github.com/thellmund/Android-Week-View/wiki/Listeners) to find out more.
- Changed: Use more extensive caching and reduce redundant `Canvas` operations for better performance.
- Changed: The “now line” is now drawn over the entire width of a day. 
- Changed: Event chips use anti-aliasing for smoother corner radiuses.
- Changed: The event location is displayed underneath the event title if there is enough space.
- Fixed: Live preview of `WeekView` in Android Studio is working again.
- Fixed: Text is no longer being drawn outside of a too-small event chip.
- Fixed: Event clicks are no longer ignored or attributed to the wrong event.
- Fixed: A situation where both `OnEventLongClickListener` and `OnEmptyViewLongClickListener` were called no longer occurs.

Thanks to everyone who contributed to this release and reported issues!

## Version 3.4.1
*(2019-05-10)*
- Changed: The `columnGap` attribute is now applied to all days currently visible. Previously, it was not applied to the last day.
- Fixed: Calling `goToDate()` no longer scrolls `WeekView` to the wrong date.

Thanks to everyone who reported issues!

## Version 3.4
*(2019-05-06)*
- New: You can now use `WeekViewEvent.Builder` to build a `WeekViewEvent`.
- New: The styling of a `WeekViewEvent` is now done via `WeekViewEvent.Style`, which you can construct via `WeekViewEvent.Style.Builder`. You can use it to set the background color, text color, border width, border color and text strike-through of the event.
- New: `WeekView` now restores the currently displayed date on configuration changes.
- Fixed: Calls to `setHeaderRowTextColor()` and `setHeaderRowTextSize()` are no longer ignored.
- Fixed: Calling `notifyDataSetChanged()` results in `onMonthChange()` being called again.
- Fixed: On the day of a time change, 3 AM is no longer being shown twice in the time column.
- Fixed: `onMonthChange()` is no longer called unnecessarily.

Thanks to everyone who reported issues!

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

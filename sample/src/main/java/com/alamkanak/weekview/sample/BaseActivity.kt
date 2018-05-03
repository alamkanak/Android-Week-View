package com.alamkanak.weekview.sample

import android.graphics.RectF
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.alamkanak.weekview.DateTimeInterpreter
import com.alamkanak.weekview.MonthLoader
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent
import java.text.SimpleDateFormat
import java.util.*

/**
 * This is a base activity which contains week view and all the codes necessary to initialize the
 * week view.
 * Created by Raquib-ul-Alam Kanak on 1/3/2014.
 * Website: http://alamkanak.github.io
 */
abstract class BaseActivity : AppCompatActivity(), WeekView.EventClickListener, MonthLoader.MonthChangeListener, WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener {
    private var mWeekViewType = TYPE_THREE_DAY_VIEW
    lateinit var weekView: WeekView
        private set


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        // Get a reference for the week view in the layout.
        weekView = findViewById<WeekView>(R.id.weekView)

        // Show a toast message about the touched event.
        weekView.setOnEventClickListener(this)

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        weekView.monthChangeListener = this

        // Set long press listener for events.
        weekView.eventLongPressListener = this

        // Set long press listener for empty view
        weekView.emptyViewLongPressListener = this

        // Set up a date time interpreter to interpret how the date and time will be formatted in
        // the week view. This is optional.
        setupDateTimeInterpreter(false)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        setupDateTimeInterpreter(id == R.id.action_week_view)
        when (id) {
            R.id.action_today -> {
                weekView.goToToday()
                return true
            }
            R.id.action_day_view -> {
                if (mWeekViewType != TYPE_DAY_VIEW) {
                    item.isChecked = !item.isChecked
                    mWeekViewType = TYPE_DAY_VIEW
                    weekView.numberOfVisibleDays = 1

                    // Lets change some dimensions to best fit the view.
                    weekView.columnGap = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
                    weekView.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics).toInt()
                    weekView.eventTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics).toInt()
                }
                return true
            }
            R.id.action_three_day_view -> {
                if (mWeekViewType != TYPE_THREE_DAY_VIEW) {
                    item.isChecked = !item.isChecked
                    mWeekViewType = TYPE_THREE_DAY_VIEW
                    weekView.numberOfVisibleDays = 3

                    // Lets change some dimensions to best fit the view.
                    weekView.columnGap = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
                    weekView.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics).toInt()
                    weekView.eventTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics).toInt()
                }
                return true
            }
            R.id.action_week_view -> {
                if (mWeekViewType != TYPE_WEEK_VIEW) {
                    item.isChecked = !item.isChecked
                    mWeekViewType = TYPE_WEEK_VIEW
                    weekView.numberOfVisibleDays = 7

                    // Lets change some dimensions to best fit the view.
                    weekView.columnGap = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics).toInt()
                    weekView.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, resources.displayMetrics).toInt()
                    weekView.eventTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, resources.displayMetrics).toInt()
                }
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     * @param shortDate True if the date values should be short.
     */
    private fun setupDateTimeInterpreter(shortDate: Boolean) {
        weekView.dateTimeInterpreter = object : DateTimeInterpreter {
            override fun interpretDate(date: Calendar): String {
                val weekdayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
                var weekday = weekdayNameFormat.format(date.time)
                val format = SimpleDateFormat(" M/d", Locale.getDefault())

                // All android api level do not have a standard way of getting the first letter of
                // the week day name. Hence we get the first char programmatically.
                // Details: http://stackoverflow.com/questions/16959502/get-one-letter-abbreviation-of-week-day-of-a-date-in-java#answer-16959657
                if (shortDate)
                    weekday = weekday[0].toString()
                return weekday.toUpperCase() + format.format(date.time)
            }

            override fun interpretTime(hour: Int): String {
                return if (hour > 11) (hour - 12).toString() + " PM" else if (hour == 0) "12 AM" else hour.toString() + " AM"
            }
        }
    }

    protected fun getEventTitle(time: Calendar?): String {
        return String.format("Event of %02d:%02d %s/%d", time!!.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH) + 1, time.get(Calendar.DAY_OF_MONTH))
    }

    override fun onEventClick(event: WeekViewEvent, eventRect: RectF?) {
        Toast.makeText(this, "Clicked " + event.name!!, Toast.LENGTH_SHORT).show()
    }

    override fun onEventLongPress(event: WeekViewEvent, eventRect: RectF?) {
        Toast.makeText(this, "Long pressed event: " + event.name!!, Toast.LENGTH_SHORT).show()
    }

    override fun onEmptyViewLongPress(time: Calendar?) {
        Toast.makeText(this, "Empty view long pressed: " + getEventTitle(time), Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val TYPE_DAY_VIEW = 1
        private val TYPE_THREE_DAY_VIEW = 2
        private val TYPE_WEEK_VIEW = 3
    }
}

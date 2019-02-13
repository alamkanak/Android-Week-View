package com.alamkanak.weekview.sample

import android.graphics.RectF
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.Guideline
import android.widget.SeekBar
import android.widget.Toast
import com.alamkanak.weekview.*
import com.alamkanak.weekview.sample.apiclient.Event
import com.alamkanak.weekview.sample.database.EventsDatabase
import com.alamkanak.weekview.sample.database.FakeEventsDatabase
import java.text.SimpleDateFormat
import java.util.*

class ConstraintActivity : AppCompatActivity(), EventClickListener<Event>, MonthChangeListener<Event>,
        EventLongPressListener<Event>, EmptyViewLongPressListener {

    lateinit var mWeekView: WeekView<Event>
    var mDatabase: EventsDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_constraint)

        mDatabase = FakeEventsDatabase(this)

        mWeekView = findViewById(R.id.weekView)
        mWeekView.setOnEventClickListener(this)
        mWeekView.setMonthChangeListener(this)
        mWeekView.setEventLongPressListener(this)
        mWeekView.emptyViewLongPressListener = this

        setupDateTimeInterpreter()
        setupSeekbarAction()
    }

    fun getEventTitle(time: Calendar): String {
        val hour = time.get(Calendar.HOUR_OF_DAY)
        val minute = time.get(Calendar.MINUTE)
        val month = time.get(Calendar.MONTH) + 1
        val dayOfMonth = time.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.getDefault(), "Event of %02d:%02d %s/%d", hour, minute, month, dayOfMonth)
    }

    override fun onMonthChange(startDate: Calendar, endDate: Calendar): List<WeekViewDisplayable<Event>> {
        return mDatabase!!.getEventsInRange(startDate, endDate)
    }

    override fun onEventClick(event: Event, eventRect: RectF) {
        Toast.makeText(this, "Clicked " + event.title, Toast.LENGTH_SHORT).show()
    }

    override fun onEventLongPress(event: Event, eventRect: RectF) {
        Toast.makeText(this, "Long pressed event: " + event.title, Toast.LENGTH_SHORT).show()
    }

    override fun onEmptyViewLongPress(time: Calendar) {
        Toast.makeText(this, "Empty view long pressed: " + getEventTitle(time), Toast.LENGTH_SHORT).show()
    }

    fun setupDateTimeInterpreter() {
        mWeekView.dateTimeInterpreter = object : DateTimeInterpreter {

            override fun interpretDate(date: Calendar): String {
                val sdfDate = SimpleDateFormat("E dd", Locale.getDefault())
                try {
                    var result = sdfDate.format(date.time).replace(". ".toRegex(), "\n")
                    result = result.substring(0, 1).toUpperCase() + result.substring(1)
                    return result
                } catch (e: Exception) {
                    e.printStackTrace()
                    return ""
                }

            }

            override fun interpretTime(hour: Int): String {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, 0)

                try {
                    val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                    return sdfTime.format(calendar.time)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return ""
                }

            }
        }
    }

    private fun setupSeekbarAction() {
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        seekBar.progress = 75
        val guideline = findViewById<Guideline>(R.id.calendar_guideline)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Nothing to do
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Nothing to do
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                guideline.setGuidelinePercent(progress / 100f)
            }
        })
    }
}

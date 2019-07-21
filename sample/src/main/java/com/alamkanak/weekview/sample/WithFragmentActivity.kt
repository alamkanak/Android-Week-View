package com.alamkanak.weekview.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.sample.apiclient.Event
import com.alamkanak.weekview.sample.data.EventsDatabase
import com.alamkanak.weekview.sample.data.FakeEventsDatabase

class WithFragmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_with_fragment)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.contentFrame, WeekFragment.newInstance())
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

class WeekFragment : Fragment() {

    private val weekView: WeekView<Event> by lazy {
        requireActivity().findViewById<WeekView<Event>>(R.id.weekView)
    }

    private val database: EventsDatabase by lazy {
        FakeEventsDatabase(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_week, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        weekView.setOnMonthChangeListener(database::getEventsInRange)
    }

    companion object {
        fun newInstance() = WeekFragment()
    }
}

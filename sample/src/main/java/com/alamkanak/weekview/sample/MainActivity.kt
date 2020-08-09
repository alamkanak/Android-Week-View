package com.alamkanak.weekview.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alamkanak.weekview.sample.util.EqualSpacingItemDecoration
import kotlin.math.roundToInt
import kotlinx.android.synthetic.main.activity_main.recyclerView
import kotlinx.android.synthetic.main.view_toolbar.toolbar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SamplesAdapter(SAMPLES, this::onItemClick)

        val spacing = resources.getDimension(R.dimen.default_space).roundToInt()
        recyclerView.addItemDecoration(EqualSpacingItemDecoration(spacing))
    }

    private fun onItemClick(sample: Sample) {
        val intent = Intent(this, sample.activity)
        startActivity(intent)
    }

    private companion object {
        private val SAMPLES = listOf(
            Sample(R.string.title_activity_basic, "Asynchronous events fetching\nThreeTenABP extension", BasicActivity::class.java),
            Sample(R.string.title_activity_static, "Static week without horizontal scrolling", StaticActivity::class.java),
            Sample(R.string.title_activity_limited, "Shows only the current month\nLimits days from 8AM to 8PM", LimitedActivity::class.java),
            Sample(R.string.title_activity_custom_font, "Custom font in WeekView", CustomFontActivity::class.java),
            Sample(R.string.title_activity_asynchronous, "Asynchronous events fetching from an API", AsyncActivity::class.java),
            Sample(R.string.title_activity_with_fragment, "Displays WeekView within a Fragment", WithFragmentActivity::class.java),
            Sample(R.string.title_activity_legacy, "Implemented in Java", LegacyActivity::class.java)
        )
    }
}

package com.alamkanak.weekview.sample.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alamkanak.weekview.sample.R
import com.alamkanak.weekview.sample.databinding.ActivityMainBinding
import com.alamkanak.weekview.sample.ui.CustomFontActivity
import com.alamkanak.weekview.sample.ui.LegacyActivity
import com.alamkanak.weekview.sample.ui.LimitedActivity
import com.alamkanak.weekview.sample.ui.StaticActivity
import com.alamkanak.weekview.sample.ui.WithFragmentActivity
import com.alamkanak.weekview.sample.ui.async.AsyncActivity
import com.alamkanak.weekview.sample.ui.basic.BasicActivity
import com.alamkanak.weekview.sample.util.EqualSpacingItemDecoration
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarContainer.toolbar)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = SamplesAdapter(SAMPLES, this::onItemClick)

        val spacing = resources.getDimension(R.dimen.default_space).roundToInt()
        binding.recyclerView.addItemDecoration(EqualSpacingItemDecoration(spacing))
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
            Sample(R.string.title_activity_custom_font, "Custom font in WeekView\nAll-day events arranged horizontally", CustomFontActivity::class.java),
            Sample(R.string.title_activity_asynchronous, "Asynchronous events fetching from an API", AsyncActivity::class.java),
            Sample(R.string.title_activity_with_fragment, "Displays WeekView within a Fragment", WithFragmentActivity::class.java),
            Sample(R.string.title_activity_legacy, "Implemented in Java", LegacyActivity::class.java)
        )
    }
}

package com.alamkanak.weekview.sample.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alamkanak.weekview.sample.R
import com.alamkanak.weekview.sample.databinding.ActivityMainBinding
import com.alamkanak.weekview.sample.databinding.ListItemSampleBinding
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
            Sample(R.string.title_activity_with_fragment, "Displays WeekView within a Fragment", WithFragmentActivity::class.java)
        )
    }
}

private data class Sample(
    val labelResId: Int,
    val details: String,
    val activity: Class<out AppCompatActivity>
)

private class SamplesAdapter(
    private val samples: List<Sample>,
    private val onItemClick: (Sample) -> Unit
) : RecyclerView.Adapter<SamplesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_sample, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(samples[position], onItemClick)
    }

    override fun getItemCount() = samples.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding: ListItemSampleBinding by lazy {
            ListItemSampleBinding.bind(itemView)
        }

        fun bind(sample: Sample, onClick: (Sample) -> Unit) {
            binding.title.text = itemView.context.getString(sample.labelResId)
            binding.details.text = sample.details
            binding.root.setOnClickListener { onClick(sample) }
        }
    }
}

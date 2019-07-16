package com.alamkanak.weekview.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alamkanak.weekview.sample.util.EqualSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_main.recyclerView
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            Sample(R.string.title_activity_basic, BaseActivity::class.java),
            Sample(R.string.title_activity_static, StaticActivity::class.java),
            Sample(R.string.title_activity_constraint, ConstraintActivity::class.java),
            Sample(R.string.title_activity_limited, LimitedActivity::class.java),
            Sample(R.string.title_activity_custom_font, CustomFontActivity::class.java),
            Sample(R.string.title_activity_asynchronous, AsyncActivity::class.java)
        )

    }

}

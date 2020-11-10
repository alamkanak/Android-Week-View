package com.alamkanak.weekview.sample

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.text.style.TypefaceSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.sample.data.EventsDatabase
import com.alamkanak.weekview.sample.data.model.CalendarEntity
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import com.alamkanak.weekview.threetenabp.WeekViewPagingAdapterThreeTenAbp
import com.alamkanak.weekview.threetenabp.setDateFormatter
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.util.Locale
import kotlin.random.Random
import kotlinx.android.synthetic.main.activity_basic.weekView
import kotlinx.android.synthetic.main.view_toolbar.toolbar
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle.MEDIUM
import org.threeten.bp.format.FormatStyle.SHORT

data class LoadParams(val startDate: LocalDate, val endDate: LocalDate)

private data class BasicViewState(
    val entities: List<CalendarEntity> = emptyList(),
    val error: RangeLoadingException? = null
)

private class RangeLoadingException(val loadParams: LoadParams) : IOException()

private class BasicViewModel(
    private val database: EventsDatabase
) : ViewModel() {

    private val _viewState = MutableLiveData<BasicViewState>()
    val viewState: LiveData<BasicViewState> = _viewState

    fun retry(loadParams: LoadParams) {
        fetchEvents(loadParams)
    }

    fun fetchEvents(loadParams: LoadParams) {
        val shouldFail = Random.nextDouble()
        val existingEvents = _viewState.value?.entities.orEmpty()

        if (shouldFail < 0.1) {
            _viewState.value = BasicViewState(
                entities = existingEvents,
                error = RangeLoadingException(loadParams = loadParams)
            )
        } else {
            val newEvents = database.getEntitiesInRange(loadParams.startDate, loadParams.endDate)
            _viewState.value = BasicViewState(
                entities = existingEvents + newEvents,
                error = null
            )
        }
    }
}

class BasicActivity : AppCompatActivity() {

    private val weekdayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())

    private val viewModel: BasicViewModel by lazy {
        BasicViewModel(database = EventsDatabase(this))
    }

    private val snackbar: Snackbar by lazy {
        Snackbar.make(weekView, "Something went wrong", Snackbar.LENGTH_INDEFINITE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic)

        toolbar.setupWithWeekView(weekView)

        val adapter = BasicActivityWeekViewAdapter(
            loadMoreHandler = { params -> viewModel.fetchEvents(params) }
        )
        weekView.adapter = adapter

        weekView.setDateFormatter { date: LocalDate ->
            val weekdayLabel = weekdayFormatter.format(date)
            val dateLabel = dateFormatter.format(date)
            weekdayLabel + "\n" + dateLabel
        }

        viewModel.viewState.observe(this) { viewState ->
            if (viewState.error != null) {
                val params = viewState.error.loadParams
                snackbar.showWithRetryAction(onRetry = { viewModel.retry(params) })
            } else {
                snackbar.dismiss()
            }

            adapter.submitList(viewState.entities)
        }
    }
}

private fun Snackbar.showWithRetryAction(onRetry: () -> Unit) {
    setAction("Retry") { onRetry() }
        .show()
}

private class BasicActivityWeekViewAdapter(
    private val loadMoreHandler: (LoadParams) -> Unit
) : WeekViewPagingAdapterThreeTenAbp<CalendarEntity>() {

    private val formatter = DateTimeFormatter.ofLocalizedDateTime(MEDIUM, SHORT)

    override fun onCreateEntity(item: CalendarEntity): WeekViewEntity {
        return when (item) {
            is CalendarEntity.Event -> createForEvent(item)
            is CalendarEntity.BlockedTimeSlot -> createForBlockedTimeSlot(item)
        }
    }

    private fun createForEvent(event: CalendarEntity.Event): WeekViewEntity {
        val backgroundColor = if (!event.isCanceled) event.color else Color.WHITE
        val textColor = if (!event.isCanceled) Color.WHITE else event.color
        val borderWidthResId = if (!event.isCanceled) R.dimen.no_border_width else R.dimen.border_width

        val style = WeekViewEntity.Style.Builder()
            .setTextColor(textColor)
            .setBackgroundColor(backgroundColor)
            .setBorderWidthResource(borderWidthResId)
            .setBorderColor(event.color)
            .build()

        val title = SpannableStringBuilder(event.title).apply {
            val titleSpan = TypefaceSpan("sans-serif-medium")
            setSpan(titleSpan, 0, event.title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (event.isCanceled) {
                setSpan(StrikethroughSpan(), 0, event.title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        val subtitle = SpannableStringBuilder(event.location).apply {
            if (event.isCanceled) {
                setSpan(StrikethroughSpan(), 0, event.location.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        return WeekViewEntity.Event.Builder(event)
            .setId(event.id)
            .setTitle(title)
            .setStartTime(event.startTime)
            .setEndTime(event.endTime)
            .setSubtitle(subtitle)
            .setAllDay(event.isAllDay)
            .setStyle(style)
            .build()
    }

    private fun createForBlockedTimeSlot(
        blockedTimeSlot: CalendarEntity.BlockedTimeSlot
    ): WeekViewEntity {
        val pattern = WeekViewEntity.Style.Pattern.Lined(
            color = ContextCompat.getColor(context, R.color.gray_500),
            strokeWidth = context.resources.getDimensionPixelSize(R.dimen.line_width),
            spacing = context.resources.getDimensionPixelSize(R.dimen.line_spacing),
            direction = WeekViewEntity.Style.Pattern.Lined.Direction.EndToStart
        )

        val style = WeekViewEntity.Style.Builder()
            .setPattern(pattern)
            .setBackgroundColorResource(R.color.gray_alpha10)
            .setCornerRadius(0)
            .build()

        return WeekViewEntity.BlockedTime.Builder()
            .setId(blockedTimeSlot.id)
            .setStartTime(blockedTimeSlot.startTime)
            .setEndTime(blockedTimeSlot.endTime)
            .setStyle(style)
            .build()
    }

    override fun onEventClick(data: CalendarEntity) {
        if (data is CalendarEntity.Event) {
            context.showToast("Clicked ${data.title}")
        }
    }

    override fun onEmptyViewClick(time: LocalDateTime) {
        context.showToast("Empty view clicked at ${formatter.format(time)}")
    }

    override fun onEventLongClick(data: CalendarEntity) {
        if (data is CalendarEntity.Event) {
            context.showToast("Long-clicked ${data.title}")
        }
    }

    override fun onEmptyViewLongClick(time: LocalDateTime) {
        context.showToast("Empty view long-clicked at ${formatter.format(time)}")
    }

    override fun onLoadMore(startDate: LocalDate, endDate: LocalDate) {
        val loadParams = LoadParams(startDate, endDate)
        loadMoreHandler(loadParams)
    }
}

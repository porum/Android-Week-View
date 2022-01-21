package com.alamkanak.weekview.sample.ui

import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.WeekViewItem
import com.alamkanak.weekview.jsr310.WeekViewPagingAdapterJsr310
import com.alamkanak.weekview.jsr310.setDateFormatter
import com.alamkanak.weekview.sample.data.model.CalendarItem
import com.alamkanak.weekview.sample.data.model.toWeekViewItem
import com.alamkanak.weekview.sample.databinding.ActivityBasicBinding
import com.alamkanak.weekview.sample.util.GenericAction.ShowSnackbar
import com.alamkanak.weekview.sample.util.defaultDateTimeFormatter
import com.alamkanak.weekview.sample.util.defaultTimeFormatter
import com.alamkanak.weekview.sample.util.genericViewModel
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import com.alamkanak.weekview.sample.util.subscribeToEvents
import com.alamkanak.weekview.sample.util.yearMonthsBetween
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class BasicActivity : AppCompatActivity() {

    private val weekdayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())

    private val binding: ActivityBasicBinding by lazy {
        ActivityBasicBinding.inflate(layoutInflater)
    }

    private val viewModel by genericViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.toolbarContainer.toolbar.setupWithWeekView(binding.weekView)

        val adapter = BasicActivityWeekViewAdapter(
            dragHandler = viewModel::handleDrag,
            loadMoreHandler = viewModel::fetchEvents,
        )
        binding.weekView.adapter = adapter

        binding.weekView.setDateFormatter { date: LocalDate ->
            val weekdayLabel = weekdayFormatter.format(date)
            val dateLabel = dateFormatter.format(date)
            weekdayLabel + "\n" + dateLabel
        }

        viewModel.viewState.observe(this) { viewState ->
            adapter.submitList(viewState.items)
        }

        viewModel.actions.subscribeToEvents(this) { action ->
            when (action) {
                is ShowSnackbar -> {
                    Snackbar
                        .make(binding.weekView, action.message, Snackbar.LENGTH_SHORT)
                        .setAction("Undo") { action.undoAction() }
                        .show()
                }
            }
        }
    }
}

class BasicActivityWeekViewAdapter(
    private val dragHandler: (Long, LocalDateTime, LocalDateTime) -> Unit,
    private val loadMoreHandler: (List<YearMonth>) -> Unit
) : WeekViewPagingAdapterJsr310<CalendarItem>() {

    override fun onCreateItem(item: CalendarItem): WeekViewItem = item.toWeekViewItem(context)

    override fun onEventClick(data: CalendarItem, bounds: RectF) {
        val message = when (data) {
            is CalendarItem.Event -> {
                "Clicked event ${data.title}"
            }
            is CalendarItem.BlockedTimeSlot -> {
                val formattedStart = defaultTimeFormatter.format(data.startTime)
                val formattedEnd = defaultTimeFormatter.format(data.endTime)
                "Clicked blocked time ($formattedStart–$formattedEnd)"
            }
        }
        context.showToast(message)
    }

    override fun onEmptyViewClick(time: LocalDateTime) {
        context.showToast("Empty view clicked at ${defaultDateTimeFormatter.format(time)}")
    }

    override fun onEmptyViewLongClick(time: LocalDateTime) {
        context.showToast("Empty view long-clicked at ${defaultDateTimeFormatter.format(time)}")
    }

    override fun onLoadMore(startDate: LocalDate, endDate: LocalDate) {
        loadMoreHandler(yearMonthsBetween(startDate, endDate))
    }

    override fun onDragAndDropFinished(data: CalendarItem, newStartTime: LocalDateTime, newEndTime: LocalDateTime) {
        if (data is CalendarItem.Event) {
            dragHandler(data.id, newStartTime, newEndTime)
        }
    }

    override fun onVerticalScrollPositionChanged(currentOffset: Float, distance: Float) {
        Log.d("BasicActivity", "Scrolling vertically (distance: ${distance.toInt()}, current offset ${currentOffset.toInt()})")
    }

    override fun onVerticalScrollFinished(currentOffset: Float) {
        Log.d("BasicActivity", "Vertical scroll finished (current offset ${currentOffset.toInt()})")
    }
}

package com.alamkanak.weekview.sample.ui

import android.graphics.RectF
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.WeekViewItem
import com.alamkanak.weekview.jsr310.WeekViewPagingAdapterJsr310
import com.alamkanak.weekview.sample.data.model.CalendarItem
import com.alamkanak.weekview.sample.data.model.toWeekViewItem
import com.alamkanak.weekview.sample.databinding.ActivityCustomFontBinding
import com.alamkanak.weekview.sample.util.defaultDateTimeFormatter
import com.alamkanak.weekview.sample.util.genericViewModel
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import com.alamkanak.weekview.sample.util.yearMonthsBetween
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class CustomFontActivity : AppCompatActivity() {

    private val binding: ActivityCustomFontBinding by lazy {
        ActivityCustomFontBinding.inflate(layoutInflater)
    }

    private val viewModel by genericViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.toolbarContainer.toolbar.setupWithWeekView(binding.weekView)

        val adapter = CustomFontActivityWeekViewAdapter(loadMoreHandler = this::onLoadMore)
        binding.weekView.adapter = adapter

        viewModel.viewState.observe(this) { viewState ->
            adapter.submitList(viewState.items)
        }
    }

    private fun onLoadMore(yearMonths: List<YearMonth>) {
        viewModel.fetchEvents(yearMonths)
    }
}

private class CustomFontActivityWeekViewAdapter(
    private val loadMoreHandler: (List<YearMonth>) -> Unit
) : WeekViewPagingAdapterJsr310<CalendarItem>() {

    override fun onCreateItem(item: CalendarItem): WeekViewItem = item.toWeekViewItem(context)

    override fun onEventClick(data: CalendarItem, bounds: RectF) {
        if (data is CalendarItem.Event) {
            context.showToast("Clicked ${data.title}")
        }
    }

    override fun onEmptyViewClick(time: LocalDateTime) {
        context.showToast("Empty view clicked at ${defaultDateTimeFormatter.format(time)}")
    }

    override fun onEventLongClick(data: CalendarItem, bounds: RectF): Boolean {
        if (data is CalendarItem.Event) {
            context.showToast("Long-clicked ${data.title}")
        }

        // Disabling drag-&-drop by considering the long-click handled.
        return true
    }

    override fun onEmptyViewLongClick(time: LocalDateTime) {
        context.showToast("Empty view long-clicked at ${defaultDateTimeFormatter.format(time)}")
    }

    override fun onLoadMore(startDate: LocalDate, endDate: LocalDate) {
        loadMoreHandler(yearMonthsBetween(startDate, endDate))
    }
}

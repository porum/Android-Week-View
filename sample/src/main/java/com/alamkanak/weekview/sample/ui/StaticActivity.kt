package com.alamkanak.weekview.sample.ui

import android.graphics.RectF
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.WeekViewItem
import com.alamkanak.weekview.jsr310.WeekViewPagingAdapterJsr310
import com.alamkanak.weekview.jsr310.firstVisibleDateAsLocalDate
import com.alamkanak.weekview.jsr310.scrollToDate
import com.alamkanak.weekview.sample.R
import com.alamkanak.weekview.sample.data.model.CalendarItem
import com.alamkanak.weekview.sample.data.model.toWeekViewItem
import com.alamkanak.weekview.sample.databinding.ActivityStaticBinding
import com.alamkanak.weekview.sample.util.defaultDateTimeFormatter
import com.alamkanak.weekview.sample.util.genericViewModel
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import com.alamkanak.weekview.sample.util.yearMonthsBetween
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class StaticActivity : AppCompatActivity() {

    private val binding: ActivityStaticBinding by lazy {
        ActivityStaticBinding.inflate(layoutInflater)
    }

    private val viewModel by genericViewModel()

    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

    private val adapter: StaticActivityWeekViewAdapter by lazy {
        StaticActivityWeekViewAdapter(
            loadMoreHandler = this::onLoadMore,
            rangeChangeHandler = this::onRangeChanged
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.toolbarContainer.toolbar.setupWithWeekView(binding.weekView)
        binding.weekView.adapter = adapter

        binding.leftNavigationButton.setOnClickListener {
            val firstDate = binding.weekView.firstVisibleDateAsLocalDate
            val newFirstDate = firstDate.minusDays(7)
            binding.weekView.scrollToDate(newFirstDate)
        }

        binding.rightNavigationButton.setOnClickListener {
            val firstDate = binding.weekView.firstVisibleDateAsLocalDate
            val newFirstDate = firstDate.plusDays(7)
            binding.weekView.scrollToDate(newFirstDate)
        }

        viewModel.viewState.observe(this) { viewState ->
            adapter.submitList(viewState.items)
        }
    }

    private fun onLoadMore(yearMonths: List<YearMonth>) {
        viewModel.fetchEvents(yearMonths)
    }

    private fun onRangeChanged(startDate: LocalDate, endDate: LocalDate) {
        binding.dateRangeTextView.text = buildDateRangeText(startDate, endDate)
    }

    private fun buildDateRangeText(startDate: LocalDate, endDate: LocalDate): String {
        val formattedFirstDay = dateFormatter.format(startDate)
        val formattedLastDay = dateFormatter.format(endDate)
        return getString(R.string.date_infos, formattedFirstDay, formattedLastDay)
    }
}

private class StaticActivityWeekViewAdapter(
    private val rangeChangeHandler: (LocalDate, LocalDate) -> Unit,
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

    override fun onEmptyViewLongClick(time: LocalDateTime) {
        context.showToast("Empty view long-clicked at ${defaultDateTimeFormatter.format(time)}")
    }

    override fun onLoadMore(startDate: LocalDate, endDate: LocalDate) {
        loadMoreHandler(yearMonthsBetween(startDate, endDate))
    }

    override fun onRangeChanged(firstVisibleDate: LocalDate, lastVisibleDate: LocalDate) {
        rangeChangeHandler(firstVisibleDate, lastVisibleDate)
    }
}

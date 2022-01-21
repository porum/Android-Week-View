package com.alamkanak.weekview.sample.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.jsr310.WeekViewPagingAdapterJsr310
import com.alamkanak.weekview.sample.data.model.CalendarEntity
import com.alamkanak.weekview.sample.data.model.toWeekViewEntity
import com.alamkanak.weekview.sample.databinding.ActivityCustomFontBinding
import com.alamkanak.weekview.sample.util.GenericAction
import com.alamkanak.weekview.sample.util.defaultDateTimeFormatter
import com.alamkanak.weekview.sample.util.genericViewModel
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import com.alamkanak.weekview.sample.util.yearMonthsBetween
import java.time.LocalDate
import java.time.LocalDateTime

class CustomFontActivity : AppCompatActivity() {

    private val binding: ActivityCustomFontBinding by lazy {
        ActivityCustomFontBinding.inflate(layoutInflater)
    }

    private val adapter: CustomFontActivityWeekViewAdapter by lazy {
        CustomFontActivityWeekViewAdapter(actionHandler = viewModel::handleAction)
    }

    private val viewModel by genericViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.toolbarContainer.toolbar.setupWithWeekView(binding.weekView)
        binding.weekView.adapter = adapter

        viewModel.viewState.observe(this) { viewState ->
            adapter.submitList(viewState.entities)
        }
    }
}

private class CustomFontActivityWeekViewAdapter(
    private val actionHandler: (GenericAction) -> Unit,
) : WeekViewPagingAdapterJsr310<CalendarEntity>() {

    override fun onCreateEntity(item: CalendarEntity): WeekViewEntity = item.toWeekViewEntity()

    override fun onEventClick(data: CalendarEntity) {
        if (data is CalendarEntity.Event) {
            context.showToast("Clicked ${data.title}")
        }
    }

    override fun onEmptyViewClick(time: LocalDateTime) {
        context.showToast("Empty view clicked at ${defaultDateTimeFormatter.format(time)}")
    }

    override fun onEventLongClick(data: CalendarEntity) {
        if (data is CalendarEntity.Event) {
            context.showToast("Long-clicked ${data.title}")
        }
    }

    override fun onEmptyViewLongClick(time: LocalDateTime) {
        context.showToast("Empty view long-clicked at ${defaultDateTimeFormatter.format(time)}")
    }

    override fun onLoadInitial(startDate: LocalDate, endDate: LocalDate) {
        val yearMonths = yearMonthsBetween(startDate, endDate)
        actionHandler(GenericAction.LoadEvents(yearMonths, clearExisting = true))
    }

    override fun onLoadMore(startDate: LocalDate, endDate: LocalDate) {
        val yearMonths = yearMonthsBetween(startDate, endDate)
        actionHandler(GenericAction.LoadEvents(yearMonths, clearExisting = false))
    }
}

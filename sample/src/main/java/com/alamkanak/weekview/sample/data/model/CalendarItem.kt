package com.alamkanak.weekview.sample.data.model

import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.text.style.TypefaceSpan
import androidx.core.content.ContextCompat
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.WeekViewItem
import com.alamkanak.weekview.jsr310.setEndTime
import com.alamkanak.weekview.jsr310.setStartTime
import com.alamkanak.weekview.sample.R
import com.alamkanak.weekview.sample.util.toCalendar
import java.time.LocalDateTime

sealed class CalendarItem {

    data class Event(
        val id: Long,
        val title: CharSequence,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val location: CharSequence,
        val color: Int,
        val isAllDay: Boolean,
        val isCanceled: Boolean,
    ) : CalendarItem()

    data class BlockedTimeSlot(
        val id: Long,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
    ) : CalendarItem()
}

fun CalendarItem.toWeekViewItem(context: Context): WeekViewItem {
    return when (this) {
        is CalendarItem.Event -> toWeekViewItem(context)
        is CalendarItem.BlockedTimeSlot -> toWeekViewItem(context)
    }
}

fun CalendarItem.Event.toWeekViewItem(context: Context): WeekViewItem {
    val backgroundColor = if (!isCanceled) color else Color.WHITE
    val textColor = if (!isCanceled) Color.WHITE else color
    val borderWidthResId = if (!isCanceled) R.dimen.no_border_width else R.dimen.border_width
    val borderWidth = context.resources.getDimensionPixelSize(borderWidthResId)

    val title = SpannableStringBuilder(title).apply {
        val titleSpan = TypefaceSpan("sans-serif-medium")
        setSpan(titleSpan, 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (isCanceled) {
            setSpan(StrikethroughSpan(), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    val subtitle = SpannableStringBuilder(location).apply {
        if (isCanceled) {
            setSpan(StrikethroughSpan(), 0, location.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    val timing = if (isAllDay) {
        WeekViewItem.Timing.AllDay(date = startTime.toLocalDate().toCalendar())
    } else {
        WeekViewItem.Timing.Bounded(
            startTime = startTime.toCalendar(),
            endTime = endTime.toCalendar(),
        )
    }

    return WeekViewItem(
        id = id,
        title = title,
        subtitle = subtitle,
        timing = timing,
        style = WeekViewItem.Style(
            textColor = textColor,
            backgroundColor = backgroundColor,
            borderWidth = borderWidth,
            borderColor = color,
        ),
        configuration = WeekViewItem.Configuration.defaultForegroundConfig(),
        data = this,
    )
}

fun CalendarItem.BlockedTimeSlot.toWeekViewItem(context: Context): WeekViewItem {
    return WeekViewItem(
        id = id,
        title = "Unavailable",
        timing = WeekViewItem.Timing.Bounded(
            startTime = startTime.toCalendar(),
            endTime = endTime.toCalendar(),
        ),
        style = WeekViewItem.Style(
            backgroundColor = ContextCompat.getColor(context, R.color.gray_alpha10),
            cornerRadius = context.resources.getDimensionPixelSize(R.dimen.no_corner_radius),
        ),
        configuration = WeekViewItem.Configuration.defaultBackgroundConfig(),
        data = this,
    )
}

fun CalendarItem.toWeekViewEntity(): WeekViewEntity {
    return when (this) {
        is CalendarItem.Event -> toWeekViewEntity()
        is CalendarItem.BlockedTimeSlot -> toWeekViewEntity()
    }
}

fun CalendarItem.Event.toWeekViewEntity(): WeekViewEntity {
    val backgroundColor = if (!isCanceled) color else Color.WHITE
    val textColor = if (!isCanceled) Color.WHITE else color
    val borderWidthResId = if (!isCanceled) R.dimen.no_border_width else R.dimen.border_width

    val style = WeekViewEntity.Style.Builder()
        .setTextColor(textColor)
        .setBackgroundColor(backgroundColor)
        .setBorderWidthResource(borderWidthResId)
        .setBorderColor(color)
        .build()

    val title = SpannableStringBuilder(title).apply {
        val titleSpan = TypefaceSpan("sans-serif-medium")
        setSpan(titleSpan, 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (isCanceled) {
            setSpan(StrikethroughSpan(), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    val subtitle = SpannableStringBuilder(location).apply {
        if (isCanceled) {
            setSpan(StrikethroughSpan(), 0, location.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    return WeekViewEntity.Event.Builder(this)
        .setId(id)
        .setTitle(title)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setSubtitle(subtitle)
        .setAllDay(isAllDay)
        .setStyle(style)
        .build()
}

fun CalendarItem.BlockedTimeSlot.toWeekViewEntity(): WeekViewEntity {
    val style = WeekViewEntity.Style.Builder()
        .setBackgroundColorResource(R.color.gray_alpha10)
        .setCornerRadius(0)
        .build()

    return WeekViewEntity.BlockedTime.Builder()
        .setId(id)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setStyle(style)
        .build()
}

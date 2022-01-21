package com.alamkanak.weekview.sample.data.model

import android.graphics.Color
import com.google.gson.annotations.SerializedName
import java.time.DateTimeException
import java.time.LocalTime
import java.time.YearMonth

interface ApiResult {
    fun toCalendarItem(yearMonth: YearMonth, index: Int): CalendarItem?
}

data class ApiEvent(
    @SerializedName("title") val title: String,
    @SerializedName("location") val location: String,
    @SerializedName("day_of_month") val dayOfMonth: Int,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("color") val color: String,
    @SerializedName("is_canceled") val isCanceled: Boolean,
    @SerializedName("is_all_day") val isAllDay: Boolean,
) : ApiResult {

    override fun toCalendarItem(yearMonth: YearMonth, index: Int): CalendarItem? {
        return try {
            val startTime = LocalTime.parse(startTime)
            val startDateTime = yearMonth.atDay(dayOfMonth).atTime(startTime)
            val endDateTime = startDateTime.plusMinutes(duration.toLong())
            CalendarItem.Event(
                id = generateId(yearMonth, index),
                title = title,
                location = location,
                startTime = startDateTime,
                endTime = endDateTime,
                color = Color.parseColor(color),
                isAllDay = isAllDay,
                isCanceled = isCanceled
            )
        } catch (e: DateTimeException) {
            null
        }
    }
}

data class ApiBlockedTime(
    @SerializedName("day_of_month") val dayOfMonth: Int,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("duration") val duration: Int,
) : ApiResult {

    override fun toCalendarItem(yearMonth: YearMonth, index: Int): CalendarItem? {
        return try {
            val startTime = LocalTime.parse(startTime)
            val startDateTime = yearMonth.atDay(dayOfMonth).atTime(startTime)
            val endDateTime = startDateTime.plusMinutes(duration.toLong())
            CalendarItem.BlockedTimeSlot(
                id = generateId(yearMonth, index),
                startTime = startDateTime,
                endTime = endDateTime
            )
        } catch (e: DateTimeException) {
            null
        }
    }
}

private fun generateId(yearMonth: YearMonth, index: Int): Long {
    val eventNumber = index.toString().padStart(length = 4, padChar = '0')
    val year = yearMonth.year * 1_000_000
    val month = yearMonth.monthValue * 1_000
    return "$year$month$eventNumber".toLong()
}

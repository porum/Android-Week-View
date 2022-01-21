package com.alamkanak.weekview.jsr310

import com.alamkanak.weekview.WeekViewItem
import java.time.LocalDate
import java.time.LocalDateTime

fun WeekViewItem.Builder.setAllDayDuration(date: LocalDate): WeekViewItem.Builder {
    return setAllDayDuration(date.toCalendar())
}

fun WeekViewItem.Builder.setBoundedDuration(
    startTime: LocalDateTime,
    endTime: LocalDateTime,
): WeekViewItem.Builder {
    return setBoundedDuration(startTime.toCalendar(), endTime.toCalendar())
}

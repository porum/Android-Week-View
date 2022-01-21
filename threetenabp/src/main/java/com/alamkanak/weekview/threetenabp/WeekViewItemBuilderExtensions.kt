package com.alamkanak.weekview.threetenabp

import com.alamkanak.weekview.WeekViewItem
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

fun WeekViewItem.Builder.setAllDayDuration(date: LocalDate): WeekViewItem.Builder {
    return setAllDayDuration(date.toCalendar())
}

fun WeekViewItem.Builder.setBoundedDuration(
    startTime: LocalDateTime,
    endTime: LocalDateTime,
): WeekViewItem.Builder {
    return setBoundedDuration(startTime.toCalendar(), endTime.toCalendar())
}

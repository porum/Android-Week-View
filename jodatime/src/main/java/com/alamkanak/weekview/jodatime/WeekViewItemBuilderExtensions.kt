package com.alamkanak.weekview.jodatime

import com.alamkanak.weekview.WeekViewItem
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

fun WeekViewItem.Builder.setAllDayDuration(date: LocalDate): WeekViewItem.Builder {
    return setAllDayDuration(date.toCalendar())
}

fun WeekViewItem.Builder.setBoundedDuration(
    startTime: LocalDateTime,
    endTime: LocalDateTime,
): WeekViewItem.Builder {
    return setBoundedDuration(startTime.toCalendar(), endTime.toCalendar())
}

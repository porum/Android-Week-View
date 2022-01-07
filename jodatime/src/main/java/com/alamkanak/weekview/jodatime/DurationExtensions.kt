package com.alamkanak.weekview.jodatime

import com.alamkanak.weekview.PublicApi
import com.alamkanak.weekview.WeekViewItem
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

/**
 * Creates an [WeekViewItem.Duration.AllDay] with the receiving [LocalDate] as the date.
 */
@PublicApi
fun LocalDate.toAllDayDuration(): WeekViewItem.Duration.AllDay {
    return WeekViewItem.Duration.AllDay(date = this.toCalendar())
}

/**
 * Creates an [WeekViewItem.Duration.AllDay] with the receiving [LocalDateTime.toLocalDate] as the
 * date.
 */
@PublicApi
fun LocalDateTime.toAllDayDuration(): WeekViewItem.Duration.AllDay {
    return toLocalDate().toAllDayDuration()
}

/**
 * Creates an [WeekViewItem.Duration.Bounded] with the receiving [LocalDateTime] as the start time
 * and the provided parameter as the end time.
 */
@PublicApi
fun LocalDateTime.toBoundedDurationUntil(endTime: LocalDateTime): WeekViewItem.Duration.Bounded {
    return WeekViewItem.Duration.Bounded(
        startTime = this.toCalendar(),
        endTime = endTime.toCalendar(),
    )
}

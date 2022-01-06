package com.alamkanak.weekview

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

internal fun defaultDateFormatter(
    numberOfDays: Int
): DateTimeFormatter {
    val pattern = when (numberOfDays) {
        1 -> "EEEE M/dd" // full weekday
        in 2..6 -> "EEE M/dd" // first three characters
        else -> "EEEEE M/dd" // first character
    }
    return DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
}

internal fun defaultTimeFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("hh a", Locale.getDefault())

internal val LocalDate.isBeforeToday: Boolean
    get() = isBefore(LocalDate.now())

internal val LocalDate.isToday: Boolean
    get() = isEqual(LocalDate.now())

internal val LocalDate.isWeekend: Boolean
    get() = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY

internal val LocalDate.daysFromToday: Int
    get() = Duration.between(this, LocalDate.now()).toDays().toInt()

internal fun LocalDate.plusDays(days: Int) = plusDays(days.toLong())

internal fun LocalDate.minusDays(days: Int) = minusDays(days.toLong())

internal fun LocalTime.minusMinutes(minutes: Int) = minusMinutes(minutes.toLong())

internal fun LocalDateTime.plusMinutes(minutes: Int) = plusMinutes(minutes.toLong())

internal fun LocalDateTime.minusMinutes(minutes: Int) = minusMinutes(minutes.toLong())

internal fun LocalDateTime.atStartOfDay() = withTimeAtStartOfPeriod(hour = 0)

internal fun LocalDateTime.atEndOfDay() = withTimeAtEndOfPeriod(hour = 24)

internal fun LocalDate.differenceWithFirstDayOfWeek(): Int {
    val firstDayOfCurrentWeek = previousFirstDayOfWeek()
    return Duration.between(firstDayOfCurrentWeek, this).toDays().toInt()
}

internal fun LocalDate.previousFirstDayOfWeek(): LocalDate {
    val dayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    val adjuster = TemporalAdjusters.previousOrSame(dayOfWeek)
    return with(adjuster)
}

internal fun LocalDate.nextFirstDayOfWeek(): LocalDate {
    val dayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    val adjuster = TemporalAdjusters.nextOrSame(dayOfWeek)
    return with(adjuster)
}

internal fun LocalDateTime.withTimeAtStartOfPeriod(hour: Int): LocalDateTime {
    return withHour(hour).truncatedTo(ChronoUnit.HOURS)
}

internal fun LocalDateTime.withTimeAtEndOfPeriod(hour: Int): LocalDateTime {
    return withHour(hour - 1)
        .withMinute(59)
        .withSecond(59)
        .withNano(999_999_999)
}

internal fun LocalDateTime.isAtStartOfPeriod(hour: Int): Boolean {
    return isEqual(withTimeAtStartOfPeriod(hour))
}

internal fun LocalDateTime.isAtEndOfPeriod(hour: Int): Boolean {
    return isEqual(withTimeAtEndOfPeriod(hour))
}

internal fun List<LocalDate>.validate(viewState: ViewState): List<LocalDate> {
    val minDate = viewState.minDate
    val maxDate = viewState.maxDate

    if (minDate == null && maxDate == null) {
        return this
    }

    val firstDate = firstOrNull() ?: return this
    val lastDate = lastOrNull() ?: return this
    val numberOfDays = size

    val mustAdjustStart = minDate != null && firstDate < minDate
    val mustAdjustEnd = maxDate != null && lastDate > maxDate

    if (mustAdjustStart && mustAdjustEnd) {
        // The date range is longer than the range from min date to max date.
        throw IllegalStateException(
            "Can't render $numberOfDays days between the provided minDate and maxDate."
        )
    }

    return when {
        mustAdjustStart -> {
            viewState.createDateRange(minDate!!)
        }
        mustAdjustEnd -> {
            val start = maxDate!!.minusDays(viewState.numberOfVisibleDays - 1)
            viewState.createDateRange(start)
        }
        else -> {
            this
        }
    }
}

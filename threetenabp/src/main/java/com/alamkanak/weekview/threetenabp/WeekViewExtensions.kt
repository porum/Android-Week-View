package com.alamkanak.weekview.threetenabp

import com.alamkanak.weekview.PublicApi
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEntity
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

@PublicApi
@Deprecated(
    message = "Switch to using the Java 8 DateTime API via core-library desugaring. Support for ThreeTenABP will be dropped in a future release.",
)
fun <T : Any> WeekViewEntity.Event.Builder<T>.setStartTime(
    startTime: LocalDateTime
) = setStartTime(startTime.toJsrLocalDateTime())

@PublicApi
@Deprecated(
    message = "Switch to using the Java 8 DateTime API via core-library desugaring. Support for ThreeTenABP will be dropped in a future release.",
)
fun <T : Any> WeekViewEntity.Event.Builder<T>.setEndTime(
    endTime: LocalDateTime
) = setEndTime(endTime.toJsrLocalDateTime())

@PublicApi
@Deprecated(
    message = "Switch to using the Java 8 DateTime API via core-library desugaring. Support for ThreeTenABP will be dropped in a future release.",
)
fun WeekViewEntity.BlockedTime.Builder.setStartTime(
    startTime: LocalDateTime
) = setStartTime(startTime.toJsrLocalDateTime())

@PublicApi
@Deprecated(
    message = "Switch to using the Java 8 DateTime API via core-library desugaring. Support for ThreeTenABP will be dropped in a future release.",
)
fun WeekViewEntity.BlockedTime.Builder.setEndTime(
    endTime: LocalDateTime
) = setEndTime(endTime.toJsrLocalDateTime())

/**
 * Returns the minimum date that [WeekView] will display as a [LocalDate], or null if none is set.
 * Events before this date will not be shown.
 */
@PublicApi
@Deprecated(
    message = "Switch to using the Java 8 DateTime API via core-library desugaring. Support for ThreeTenABP will be dropped in a future release.",
)
var WeekView.minDateAsLocalDate: LocalDate?
    get() = minDate?.toThreeTenLocalDate()
    set(value) {
        minDate = value?.toJsrLocalDate()
    }

/**
 * Returns the maximum date that [WeekView] will display as a [LocalDate], or null if none is set.
 * Events after this date will not be shown.
 */
@PublicApi
@Deprecated(
    message = "Switch to using the Java 8 DateTime API via core-library desugaring. Support for ThreeTenABP will be dropped in a future release.",
)
var WeekView.maxDateAsLocalDate: LocalDate?
    get() = maxDate?.toThreeTenLocalDate()
    set(value) {
        maxDate = value?.toJsrLocalDate()
    }

/**
 * Returns the first visible date as a [LocalDate].
 */
@PublicApi
@Deprecated(
    message = "Switch to using the Java 8 DateTime API via core-library desugaring. Support for ThreeTenABP will be dropped in a future release.",
)
val WeekView.firstVisibleDateAsLocalDate: LocalDate
    get() = firstVisibleDate.toThreeTenLocalDate()

/**
 * Returns the last visible date as a [LocalDate].
 */
@PublicApi
@Deprecated(
    message = "Switch to using the Java 8 DateTime API via core-library desugaring. Support for ThreeTenABP will be dropped in a future release.",
)
val WeekView.lastVisibleDateAsLocalDate: LocalDate
    get() = lastVisibleDate.toThreeTenLocalDate()

/**
 * Scrolls to the specified date. Any provided [LocalDate] that falls outside the range of
 * [WeekView.minDate] and [WeekView.maxDate] will be adjusted to fit into this range.
 *
 * @param date The [LocalDate] to scroll to.
 */
@PublicApi
@Deprecated(
    message = "Switch to using the Java 8 DateTime API via core-library desugaring. Support for ThreeTenABP will be dropped in a future release.",
)
fun WeekView.scrollToDate(date: LocalDate) {
    scrollToDate(date.toJsrLocalDate())
}

/**
 * Scrolls to the specified date time. Any provided [LocalDateTime] that falls outside the range of
 * [WeekView.minDate] and [WeekView.maxDate], or [WeekView.minHour] and [WeekView.maxHour], will be
 * adjusted to fit into these ranges.
 *
 * @param dateTime The [LocalDateTime] to scroll to.
 */
@PublicApi
@Deprecated(
    message = "Switch to using the Java 8 DateTime API via core-library desugaring. Support for ThreeTenABP will be dropped in a future release.",
)
fun WeekView.scrollToDateTime(dateTime: LocalDateTime) {
    scrollToDateTime(dateTime.toJsrLocalDateTime())
}

/**
 * Scrolls to the specified time. Any provided [LocalTime] that falls outside the range of
 * [WeekView.minHour] and [WeekView.maxHour] will be adjusted to fit into these ranges.
 *
 * @param time The [LocalTime] to scroll to.
 */
@PublicApi
@Deprecated(
    message = "Switch to using the Java 8 DateTime API via core-library desugaring. Support for ThreeTenABP will be dropped in a future release.",
)
fun WeekView.scrollToTime(time: LocalTime) {
    scrollToTime(time.toJsrLocalTime())
}

@PublicApi
@Deprecated(
    message = "Switch to using the Java 8 DateTime API via core-library desugaring. Support for ThreeTenABP will be dropped in a future release.",
)
fun WeekView.setDateFormatter(formatter: (LocalDate) -> String) {
    setDateFormatter { formatter(it.toThreeTenLocalDate()) }
}

package com.alamkanak.weekview.jsr310

import com.alamkanak.weekview.PublicApi
import com.alamkanak.weekview.WeekView
import java.time.LocalDate

/**
 * Returns the minimum date that [WeekView] will display as a [LocalDate], or null if none is set.
 * Events before this date will not be shown.
 */
@PublicApi
@Deprecated(
    message = "Use minDate instead.",
    replaceWith = ReplaceWith(
        expression = "minDate",
    ),
)
var WeekView.minDateAsLocalDate: LocalDate?
    get() = minDate
    set(value) {
        minDate = value
    }

/**
 * Returns the maximum date that [WeekView] will display as a [LocalDate], or null if none is set.
 * Events after this date will not be shown.
 */
@PublicApi
@Deprecated(
    message = "Use maxDate instead.",
    replaceWith = ReplaceWith(
        expression = "maxDate",
    ),
)
var WeekView.maxDateAsLocalDate: LocalDate?
    get() = maxDate
    set(value) {
        maxDate = value
    }

/**
 * Returns the first visible date as a [LocalDate].
 */
@PublicApi
@Deprecated(
    message = "Use firstVisibleDate instead.",
    replaceWith = ReplaceWith(
        expression = "firstVisibleDate",
    ),
)
val WeekView.firstVisibleDateAsLocalDate: LocalDate
    get() = firstVisibleDate

/**
 * Returns the last visible date as a [LocalDate].
 */
@PublicApi
@Deprecated(
    message = "Use lastVisibleDate instead.",
    replaceWith = ReplaceWith(
        expression = "lastVisibleDate",
    ),
)
val WeekView.lastVisibleDateAsLocalDate: LocalDate
    get() = lastVisibleDate

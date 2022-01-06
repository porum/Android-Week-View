package com.alamkanak.weekview.jsr310

import com.alamkanak.weekview.PublicApi
import com.alamkanak.weekview.WeekView
import java.time.LocalDate
import java.util.Calendar

/**
 * An abstract implementation of [WeekView.Adapter] that allows to submit a list of new elements to
 * [WeekView] and uses [LocalDate] instead of [Calendar].
 *
 * Newly submitted events are processed on a background thread and then presented in [WeekView].
 * Previously submitted events are replaced completely. If you require a paginated approach, you
 * might want to use [WeekView.PagingAdapter].
 *
 * @param T The type of elements that are displayed in the corresponding [WeekView].
 */
@PublicApi
@Deprecated(
    message = "Switch to using WeekView.SimpleAdapter<T>. Support for JSR310-specific adapters will be dropped in a future release.",
    replaceWith = ReplaceWith(
        expression = "WeekView.SimpleAdapter<T>",
    )
)
abstract class WeekViewSimpleAdapterJsr310<T> : WeekView.SimpleAdapter<T>()

/**
 * An abstract implementation of [WeekView.Adapter] that allows to submit a list of new elements to
 * [WeekView] in a paginated way and uses [LocalDate] instead of [Calendar].
 *
 * This adapter keeps a cache of the submitted elements grouped by month. Whenever the user scrolls
 * to a different month, this adapter will check whether that month's events are present in the
 * cache. If not, it will dispatch a callback to [onLoadMore] with the start and end dates of the
 * months that need to be fetched.
 *
 * Newly submitted events are processed on a background thread and then presented in [WeekView]. To
 * clear the cache and thus refresh all events, you can call [refresh].
 *
 * @param T The type of elements that are displayed in the corresponding [WeekView].
 */
@PublicApi
@Deprecated(
    message = "Switch to using WeekView.PagingAdapter<T>. Support for JSR310-specific adapters will be dropped in a future release.",
    replaceWith = ReplaceWith(
        expression = "WeekView.PagingAdapter<T>",
    )
)
abstract class WeekViewPagingAdapterJsr310<T> : WeekView.PagingAdapter<T>()

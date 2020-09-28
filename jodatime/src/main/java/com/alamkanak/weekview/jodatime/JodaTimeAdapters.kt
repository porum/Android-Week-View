package com.alamkanak.weekview.jodatime

import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import java.util.Calendar
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

/**
 * An implementation of [WeekView.Adapter] that allows to submit a list of new elements to
 * [WeekView] and uses [LocalDate] instead of [Calendar].
 *
 * Newly submitted events are processed on a background thread and then presented in
 * [WeekView]. Previously submitted events are replaced completely. If you require a paginated
 * approach, you might want to use [WeekView.PagingAdapter].
 *
 * @param T The type of elements that are displayed in the corresponding [WeekView].
 */
open class WeekViewSimpleAdapterJodaTime<T> : WeekView.SimpleAdapter<T>() {
    final override fun onEmptyViewClick(time: Calendar) {
        onEmptyViewClick(time.toLocalDateTime())
    }

    final override fun onEmptyViewLongClick(time: Calendar) {
        onEmptyViewLongClick(time.toLocalDateTime())
    }

    final override fun onRangeChanged(firstVisibleDate: Calendar, lastVisibleDate: Calendar) {
        onRangeChanged(firstVisibleDate.toLocalDate(), lastVisibleDate.toLocalDate())
    }

    /**
     * Returns the date and time of the location that the user clicked on.
     *
     * @param time A [LocalDateTime] with the date and time
     */
    open fun onEmptyViewClick(time: LocalDateTime) = Unit

    /**
     * Returns the date and time of the location that the user long-clicked on.
     *
     * @param time A [LocalDateTime] with the date and time
     */
    open fun onEmptyViewLongClick(time: LocalDateTime) = Unit

    /**
     * Called whenever the range of dates visible in [WeekView] changes. The list of dates is
     * typically as long as [WeekView.numberOfVisibleDays], though it might contain an additional
     * date if [WeekView] is currently scrolling.
     *
     * @param firstVisibleDate A [LocalDate] representing the first visible date
     * @param lastVisibleDate A [LocalDate] representing the last visible date
     */
    open fun onRangeChanged(firstVisibleDate: LocalDate, lastVisibleDate: LocalDate) = Unit
}

/**
 * An implementation of [WeekView.Adapter] that allows to submit a list of new elements to
 * [WeekView] in a paginated way and uses [LocalDate] instead of [Calendar].
 *
 * This adapter keeps a cache of [WeekViewDisplayable] elements grouped by month. Whenever the
 * user scrolls to a different month, this adapter will check whether that month's events are
 * present in the cache. If not, it will dispatch a callback to [onLoadMore] with the start and
 * end dates of the months that need to be fetched.
 *
 * Newly submitted events are processed on a background thread and then presented in
 * [WeekView]. To clear the cache and thus refresh all events, you can call [refresh].
 *
 * @param T The type of elements that are displayed in the corresponding [WeekView].
 */
open class WeekViewPagingAdapterJodaTime<T> : WeekView.PagingAdapter<T>() {

    final override fun onEmptyViewClick(time: Calendar) {
        onEmptyViewClick(time.toLocalDateTime())
    }

    final override fun onEmptyViewLongClick(time: Calendar) {
        onEmptyViewLongClick(time.toLocalDateTime())
    }

    final override fun onRangeChanged(firstVisibleDate: Calendar, lastVisibleDate: Calendar) {
        onRangeChanged(firstVisibleDate.toLocalDate(), lastVisibleDate.toLocalDate())
    }

    final override fun onLoadMore(startDate: Calendar, endDate: Calendar) {
        onLoadMore(startDate.toLocalDate(), endDate.toLocalDate())
    }

    /**
     * Returns the date and time of the location that the user clicked on.
     *
     * @param time A [LocalDateTime] with the date and time
     */
    open fun onEmptyViewClick(time: LocalDateTime) = Unit

    /**
     * Returns the date and time of the location that the user long-clicked on.
     *
     * @param time A [LocalDateTime] with the date and time
     */
    open fun onEmptyViewLongClick(time: LocalDateTime) = Unit

    /**
     * Called whenever the range of dates visible in [WeekView] changes. The list of dates is
     * typically as long as [WeekView.numberOfVisibleDays], though it might contain an additional
     * date if [WeekView] is currently scrolling.
     *
     * @param firstVisibleDate A [LocalDate] representing the first visible date
     * @param lastVisibleDate A [LocalDate] representing the last visible date
     */
    open fun onRangeChanged(firstVisibleDate: LocalDate, lastVisibleDate: LocalDate) = Unit

    /**
     * Called whenever [WeekView] needs to fetch [WeekViewDisplayable] elements of a given
     * month in order to allow for a smooth scrolling experience.
     *
     * This adapter caches [WeekViewDisplayable] elements of the current month as well as its
     * previous and next month. If [WeekView] scrolls to a new month, that month as well as its
     * surrounding months need to potentially be fetched.
     *
     * @param startDate A [LocalDate] of the first date of the month that needs to be fetched
     * @param endDate A [LocalDate] of the last date of the month that needs to be fetched
     */
    open fun onLoadMore(startDate: LocalDate, endDate: LocalDate) = Unit
}

package com.alamkanak.weekview

import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import java.util.Calendar

/**
 * Represents an item that is displayed in [WeekView]. The corresponding domain object can be passed
 * as the [data] property and will be passed to the caller's implementation of
 * [WeekView.Adapter.onEventClick].
 *
 * The item is rendered based on the information provided in its [style] and [configuration]
 * properties.
 */
data class WeekViewItem(
    val id: Long = 0L,
    val title: CharSequence,
    val subtitle: CharSequence? = null,
    val duration: Duration,
    val style: Style = Style(),
    val configuration: Configuration = Configuration(),
    val data: Any,
) {

    /**
     * Encapsulates styling information for a [WeekViewItem]. If a property is not provided,
     * [WeekView] will use its global default values (such as [WeekView.defaultEventTextColor]).
     */
    data class Style(
        @ColorInt val textColor: Int? = null,
        @ColorInt val backgroundColor: Int? = null,
        @ColorInt val borderColor: Int? = null,
        @Dimension val borderWidth: Int? = null,
        @Dimension val cornerRadius: Int? = null,
    )

    /**
     * Encapsulates information about a [WeekViewItem].
     *
     * @param respectDayGap Whether the item should respect [WeekView.columnGap]. If not, it will
     *                      occupy the full width of a day.
     * @param arrangement Whether the item should be rendered in the foreground or background.
     * @param canBeDragged Whether the item can be dragged around by the user.
     */
    data class Configuration(
        val respectDayGap: Boolean = true,
        val arrangement: Arrangement = Arrangement.Foreground,
        val canBeDragged: Boolean = true,
    ) {
        companion object {

            fun foreground(
                respectDayGap: Boolean = true,
                canBeDragged: Boolean = true,
            ) = Configuration(
                respectDayGap = respectDayGap,
                canBeDragged = canBeDragged,
                arrangement = Arrangement.Foreground,
            )

            fun background(
                respectDayGap: Boolean = false,
                canBeDragged: Boolean = false,
            ) = Configuration(
                respectDayGap = respectDayGap,
                canBeDragged = canBeDragged,
                arrangement = Arrangement.Background,
            )
        }
    }

    /**
     * The arrangement of a [WeekViewItem], specifying whether it should be rendered in the
     * foreground or background.
     */
    enum class Arrangement {
        Background,
        Foreground,
    }

    /**
     * The duration information of a [WeekViewItem], which can either be an [AllDay] event or a
     * [Bounded] event.
     */
    sealed class Duration {

        abstract val startTime: Calendar
        abstract val endTime: Calendar

        data class Bounded(
            override val startTime: Calendar,
            override val endTime: Calendar,
        ) : Duration()

        data class AllDay(
            val date: Calendar,
        ) : Duration() {
            override val startTime: Calendar = date.atStartOfDay
            override val endTime: Calendar = date.atEndOfDay
        }
    }

    val isAllDay: Boolean by lazy {
        duration is Duration.AllDay
    }

    val isNotAllDay: Boolean by lazy {
        !isAllDay
    }

    internal val isMultiDay: Boolean by lazy {
        !duration.startTime.isSameDate(duration.endTime)
    }

    internal val period: Period by lazy {
        when (duration) {
            is Duration.Bounded -> Period.fromDate(duration.startTime)
            is Duration.AllDay -> Period.fromDate(duration.date)
        }
    }

    internal val durationInMinutes: Int by lazy {
        (duration.endTime minutesUntil duration.startTime).minutes
    }

    internal fun isWithin(
        minHour: Int,
        maxHour: Int
    ): Boolean = duration.startTime.hour >= minHour && duration.endTime.hour <= maxHour

    internal fun copyWith(startTime: Calendar, endTime: Calendar): WeekViewItem {
        return when (duration) {
            is Duration.Bounded -> copy(duration = duration.copy(startTime = startTime, endTime = endTime))
            is Duration.AllDay -> this
        }
    }

    internal fun limitTo(minHour: Int, maxHour: Int): WeekViewItem {
        return copyWith(
            startTime = duration.startTime.limitToMinHour(minHour),
            endTime = duration.endTime.limitToMaxHour(maxHour),
        )
    }

    internal fun collidesWith(other: WeekViewItem): Boolean = duration.overlapsWith(other.duration)
}

private fun WeekViewItem.Duration.overlapsWith(other: WeekViewItem.Duration): Boolean {
    if (this is WeekViewItem.Duration.AllDay || other is WeekViewItem.Duration.AllDay) {
        return false
    }

    if (startTime.isEqual(other.startTime) && endTime.isEqual(other.endTime)) {
        // Complete overlap
        return true
    }

    // Resolve collisions by shortening the preceding event by 1 ms
    if (endTime.isEqual(other.startTime)) {
        endTime -= Millis(1)
        return false
    } else if (startTime.isEqual(other.endTime)) {
        other.endTime -= Millis(1)
    }

    return !startTime.isAfter(other.endTime) && !endTime.isBefore(other.startTime)
}

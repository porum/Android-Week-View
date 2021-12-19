package com.alamkanak.weekview

import android.content.Context
import java.util.Calendar
import kotlin.math.roundToInt

@Deprecated(message = "Remove this soon.")
internal sealed class ResolvedWeekViewEntity {

    internal abstract val id: Long
    internal abstract val title: CharSequence
    internal abstract val subtitle: CharSequence?
    internal abstract val startTime: Calendar
    internal abstract val endTime: Calendar
    internal abstract val isAllDay: Boolean
    internal abstract val style: Style

    internal val period: Period by lazy {
        Period.fromDate(startTime)
    }

    @Deprecated(message = "Remove this soon.")
    data class Event<T>(
        override val id: Long,
        override val title: CharSequence,
        override val startTime: Calendar,
        override val endTime: Calendar,
        override val subtitle: CharSequence?,
        override val isAllDay: Boolean,
        override val style: Style,
        val data: T?
    ) : ResolvedWeekViewEntity()

    @Deprecated(message = "Remove this soon.")
    data class BlockedTime(
        override val id: Long,
        override val title: CharSequence,
        override val subtitle: CharSequence?,
        override val startTime: Calendar,
        override val endTime: Calendar,
        override val style: Style
    ) : ResolvedWeekViewEntity() {
        override val isAllDay: Boolean = false
    }

    @Deprecated(message = "Remove this soon.")
    data class Style(
        val textColor: Int? = null,
        val backgroundColor: Int? = null,
        val pattern: WeekViewEntity.Style.Pattern? = null,
        val borderColor: Int? = null,
        val borderWidth: Int? = null,
        val cornerRadius: Int? = null
    )

    internal val isNotAllDay: Boolean
        get() = isAllDay.not()

    internal val durationInMinutes: Int
        get() = ((endTime.timeInMillis - startTime.timeInMillis).toFloat() / 60_000).roundToInt()

    internal val isMultiDay: Boolean
        get() = startTime.isSameDate(endTime).not()

    internal fun isWithin(
        minHour: Int,
        maxHour: Int
    ): Boolean = startTime.hour >= minHour && endTime.hour <= maxHour

    internal fun collidesWith(other: ResolvedWeekViewEntity): Boolean {
        if (isAllDay != other.isAllDay) {
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

    internal fun createCopy(
        startTime: Calendar = this.startTime,
        endTime: Calendar = this.endTime
    ): ResolvedWeekViewEntity = when (this) {
        is Event<*> -> copy(startTime = startTime, endTime = endTime)
        is BlockedTime -> copy(startTime = startTime, endTime = endTime)
    }
}

internal fun WeekViewEntity.resolve(
    context: Context
): ResolvedWeekViewEntity = when (this) {
    is WeekViewEntity.Event<*> -> ResolvedWeekViewEntity.Event(
        id = id,
        title = titleResource.resolve(context, semibold = true).processed,
        startTime = startTime.withLocalTimeZone(),
        endTime = endTime.withLocalTimeZone(),
        subtitle = subtitleResource?.resolve(context, semibold = false)?.processed,
        isAllDay = isAllDay,
        style = style.resolve(context),
        data = data
    )
    is WeekViewEntity.BlockedTime -> ResolvedWeekViewEntity.BlockedTime(
        id = id,
        title = titleResource.resolve(context, semibold = true).processed,
        subtitle = subtitleResource?.resolve(context, semibold = false)?.processed,
        startTime = startTime.withLocalTimeZone(),
        endTime = endTime.withLocalTimeZone(),
        style = style.resolve(context)
    )
}

internal fun WeekViewEntity.Style.resolve(
    context: Context
) = ResolvedWeekViewEntity.Style(
    textColor = textColorResource?.resolve(context),
    backgroundColor = backgroundColorResource?.resolve(context),
    pattern = pattern,
    borderColor = borderColorResource?.resolve(context),
    borderWidth = borderWidthResource?.resolve(context),
    cornerRadius = cornerRadiusResource?.resolve(context)
)

internal fun ResolvedWeekViewEntity.toWeekViewItem(): WeekViewItem {
    return WeekViewItem(
        id = id,
        title = title,
        subtitle = subtitle,
        timing = if (isAllDay) {
            WeekViewItem.Timing.AllDay(
                date = startTime.atStartOfDay,
            )
        } else {
            WeekViewItem.Timing.Bounded(
                startTime = startTime,
                endTime = endTime,
            )
        },
        style = WeekViewItem.Style(
            textColor = style.textColor,
            backgroundColor = style.backgroundColor,
            borderColor = style.borderColor,
            borderWidth = style.borderWidth,
            cornerRadius = style.cornerRadius,
        ),
        configuration = WeekViewItem.Configuration(
            respectDayGap = this is WeekViewEntity.Event<*>,
            arrangement = when (this) {
                is ResolvedWeekViewEntity.Event<*> -> WeekViewItem.Arrangement.Foreground
                is ResolvedWeekViewEntity.BlockedTime -> WeekViewItem.Arrangement.Background
            },
            canBeDragged = this is WeekViewEntity.Event<*>,
        ),
        data = (this as? ResolvedWeekViewEntity.Event<*>)?.data,
    )
}

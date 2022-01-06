package com.alamkanak.weekview

import android.content.Context
import java.time.Duration
import java.time.LocalDateTime
import java.time.YearMonth

internal sealed class ResolvedWeekViewEntity {

    internal abstract val id: Long
    internal abstract val title: CharSequence
    internal abstract val subtitle: CharSequence?
    internal abstract var startTime: LocalDateTime
    internal abstract var endTime: LocalDateTime
    internal abstract val isAllDay: Boolean
    internal abstract val style: Style

    internal val period: YearMonth by lazy {
        YearMonth.of(startTime.year, startTime.month)
    }

    data class Event<T>(
        override val id: Long,
        override val title: CharSequence,
        override var startTime: LocalDateTime,
        override var endTime: LocalDateTime,
        override val subtitle: CharSequence?,
        override val isAllDay: Boolean,
        override val style: Style,
        val data: T?
    ) : ResolvedWeekViewEntity()

    data class BlockedTime(
        override val id: Long,
        override val title: CharSequence,
        override val subtitle: CharSequence?,
        override var startTime: LocalDateTime,
        override var endTime: LocalDateTime,
        override val style: Style
    ) : ResolvedWeekViewEntity() {
        override val isAllDay: Boolean = false
    }

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
        get() = Duration.between(startTime, endTime).toMinutes().toInt()

    internal val isMultiDay: Boolean
        get() = startTime.toLocalDate() != endTime.toLocalDate()

    internal fun isWithin(
        minHour: Int,
        maxHour: Int
    ): Boolean = startTime.hour >= minHour && endTime.hour <= maxHour

    internal fun collidesWith(other: ResolvedWeekViewEntity): Boolean {
        if (isAllDay != other.isAllDay) {
            return false
        }

        // Resolve collisions by shortening the preceding event by 1 ms
        if (endTime.isEqual(other.startTime)) {
            endTime = endTime.minusNanos(1)
            return false
        } else if (startTime.isEqual(other.endTime)) {
            other.endTime = other.endTime.minusNanos(1)
        }

        return !startTime.isAfter(other.endTime) && !endTime.isBefore(other.startTime)
    }

    internal fun createCopy(
        startTime: LocalDateTime = this.startTime,
        endTime: LocalDateTime = this.endTime,
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
        startTime = startTime,
        endTime = endTime,
        subtitle = subtitleResource?.resolve(context, semibold = false)?.processed,
        isAllDay = isAllDay,
        style = style.resolve(context),
        data = data
    )
    is WeekViewEntity.BlockedTime -> ResolvedWeekViewEntity.BlockedTime(
        id = id,
        title = titleResource.resolve(context, semibold = true).processed,
        subtitle = subtitleResource?.resolve(context, semibold = false)?.processed,
        startTime = startTime,
        endTime = endTime,
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

package com.alamkanak.weekview.util

import com.alamkanak.weekview.ResolvedWeekViewEntity
import java.time.LocalDateTime
import kotlin.random.Random

internal object MockFactory {

    fun resolvedWeekViewEntities(count: Int): List<ResolvedWeekViewEntity.Event<Event>> {
        return (0 until count).map { resolvedWeekViewEntity() }
    }

    fun resolvedWeekViewEntity(
        startTime: LocalDateTime = LocalDateTime.now(),
        endTime: LocalDateTime = startTime.plusHours(1),
        isAllDay: Boolean = false,
    ): ResolvedWeekViewEntity.Event<Event> {
        val id = Random.nextLong()
        return ResolvedWeekViewEntity.Event(
            id = id,
            title = "Title $id",
            startTime = startTime,
            endTime = endTime,
            subtitle = null,
            isAllDay = isAllDay,
            style = ResolvedWeekViewEntity.Style(),
            data = Event(startTime, endTime),
        )
    }
}

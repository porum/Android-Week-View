package com.alamkanak.weekview.util

import com.alamkanak.weekview.ResolvedWeekViewEntity
import java.time.LocalDateTime
import kotlin.random.Random

internal fun createResolvedWeekViewEvent(
    startTime: LocalDateTime,
    endTime: LocalDateTime
): ResolvedWeekViewEntity = ResolvedWeekViewEntity.Event(
    id = 0,
    title = "Title",
    startTime = startTime,
    endTime = endTime,
    subtitle = null,
    isAllDay = false,
    style = ResolvedWeekViewEntity.Style(),
    data = Unit
)

internal fun ResolvedWeekViewEntity.Event<*>.withDifferentId() = copy(id = Random.nextLong())

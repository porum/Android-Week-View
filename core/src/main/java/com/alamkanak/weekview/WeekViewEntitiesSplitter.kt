package com.alamkanak.weekview

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

internal fun ResolvedWeekViewEntity.split(viewState: ViewState): List<ResolvedWeekViewEntity> {
    if (startTime >= endTime) {
        return emptyList()
    }

    val entities = if (isMultiDay) {
        splitByDates(minHour = viewState.minHour, maxHour = viewState.maxHour)
    } else {
        listOf(limitTo(minHour = viewState.minHour, maxHour = viewState.maxHour))
    }

    return entities.filter { it.startTime < it.endTime }
}

private fun ResolvedWeekViewEntity.splitByDates(
    minHour: Int,
    maxHour: Int,
): List<ResolvedWeekViewEntity> {
    val firstEvent = createCopy(
        startTime = startTime.limitToMinHour(minHour),
        endTime = startTime.atEndOfDay().limitToMaxHour(maxHour)
    )

    val results = mutableListOf<ResolvedWeekViewEntity>()
    results += firstEvent

    val daysInBetween = ChronoUnit.DAYS.between(startTime.toLocalDate(), endTime.toLocalDate())

    if (daysInBetween > 0) {
        var currentDate = startTime.atStartOfDay().plusDays(1)
        while (currentDate.toLocalDate() < endTime.toLocalDate()) {
            val intermediateStart = currentDate.withTimeAtStartOfPeriod(minHour)
            val intermediateEnd = currentDate.withTimeAtEndOfPeriod(maxHour)
            results += createCopy(startTime = intermediateStart, endTime = intermediateEnd)
            currentDate = currentDate.plusDays(1)
        }
    }

    val lastEvent = createCopy(
        startTime = endTime.atStartOfDay().limitToMinHour(minHour),
        endTime = endTime.limitToMaxHour(maxHour)
    )
    results += lastEvent

    return results.sortedWith(compareBy({ it.startTime }, { it.endTime }))
}

private fun ResolvedWeekViewEntity.limitTo(minHour: Int, maxHour: Int) = createCopy(
    startTime = startTime.limitToMinHour(minHour),
    endTime = endTime.limitToMaxHour(maxHour)
)

private fun LocalDateTime.limitToMinHour(minHour: Int): LocalDateTime {
    return if (hour < minHour) {
        withTimeAtStartOfPeriod(hour = minHour)
    } else {
        this
    }
}

private fun LocalDateTime.limitToMaxHour(maxHour: Int): LocalDateTime {
    return if (hour >= maxHour) {
        withTimeAtEndOfPeriod(hour = maxHour)
    } else {
        this
    }
}

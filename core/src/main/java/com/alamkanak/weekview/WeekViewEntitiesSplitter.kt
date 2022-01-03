package com.alamkanak.weekview

internal fun WeekViewItem.split(viewState: ViewState): List<WeekViewItem> {
    if (duration.startTime >= duration.endTime) {
        return emptyList()
    }

    val entities = if (isMultiDay) {
        splitByDates(minHour = viewState.minHour, maxHour = viewState.maxHour)
    } else {
        listOf(limitTo(minHour = viewState.minHour, maxHour = viewState.maxHour))
    }

    return entities.filter { it.duration.startTime < it.duration.endTime }
}

private fun WeekViewItem.splitByDates(
    minHour: Int,
    maxHour: Int,
): List<WeekViewItem> {
    val firstEvent = copyWith(
        startTime = duration.startTime.limitToMinHour(minHour),
        endTime = duration.startTime.atEndOfDay.limitToMaxHour(maxHour)
    )

    val results = mutableListOf<WeekViewItem>()
    results += firstEvent

    val daysInBetween = duration.endTime.toEpochDays() - duration.startTime.toEpochDays() - 1

    if (daysInBetween > 0) {
        val currentDate = duration.startTime.atStartOfDay.plusDays(1)
        while (currentDate.toEpochDays() < duration.endTime.toEpochDays()) {
            val intermediateStart = currentDate.withTimeAtStartOfPeriod(minHour)
            val intermediateEnd = currentDate.withTimeAtEndOfPeriod(maxHour)
            results += copyWith(startTime = intermediateStart, endTime = intermediateEnd)
            currentDate.addDays(1)
        }
    }

    val lastEvent = copyWith(
        startTime = duration.endTime.atStartOfDay.limitToMinHour(minHour),
        endTime = duration.endTime.limitToMaxHour(maxHour)
    )
    results += lastEvent

    return results.sortedWith(compareBy({ it.duration.startTime }, { it.duration.endTime }))
}

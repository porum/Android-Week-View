package com.alamkanak.weekview

internal fun WeekViewItem.split(viewState: ViewState): List<WeekViewItem> {
    if (timing.startTime >= timing.endTime) {
        return emptyList()
    }

    val entities = if (isMultiDay) {
        splitByDates(minHour = viewState.minHour, maxHour = viewState.maxHour)
    } else {
        listOf(limitTo(minHour = viewState.minHour, maxHour = viewState.maxHour))
    }

    return entities.filter { it.timing.startTime < it.timing.endTime }
}

private fun WeekViewItem.splitByDates(
    minHour: Int,
    maxHour: Int,
): List<WeekViewItem> {
    val firstEvent = copyWith(
        startTime = timing.startTime.limitToMinHour(minHour),
        endTime = timing.startTime.atEndOfDay.limitToMaxHour(maxHour)
    )

    val results = mutableListOf<WeekViewItem>()
    results += firstEvent

    val daysInBetween = timing.endTime.toEpochDays() - timing.startTime.toEpochDays() - 1

    if (daysInBetween > 0) {
        val currentDate = timing.startTime.atStartOfDay + Days(1)
        while (currentDate.toEpochDays() < timing.endTime.toEpochDays()) {
            val intermediateStart = currentDate.withTimeAtStartOfPeriod(minHour)
            val intermediateEnd = currentDate.withTimeAtEndOfPeriod(maxHour)
            results += copyWith(startTime = intermediateStart, endTime = intermediateEnd)
            currentDate += Days(1)
        }
    }

    val lastEvent = copyWith(
        startTime = timing.endTime.atStartOfDay.limitToMinHour(minHour),
        endTime = timing.endTime.limitToMaxHour(maxHour)
    )
    results += lastEvent

    return results.sortedWith(compareBy({ it.timing.startTime }, { it.timing.endTime }))
}

package com.alamkanak.weekview

import java.time.LocalDate
import java.time.YearMonth

internal data class FetchRange(
    val previous: YearMonth,
    val current: YearMonth,
    val next: YearMonth
) {

    val periods: List<YearMonth> = listOf(previous, current, next)

    internal companion object {
        fun create(firstVisibleDate: LocalDate): FetchRange {
            val current = YearMonth.of(firstVisibleDate.year, firstVisibleDate.month)
            return FetchRange(current.previous, current, current.next)
        }
    }
}

internal val YearMonth.previous: YearMonth
    get() = this.minusMonths(1)

internal val YearMonth.next: YearMonth
    get() = this.plusMonths(1)

internal val YearMonth.startDate: LocalDate
    get() = this.atDay(1)

internal val YearMonth.endDate: LocalDate
    get() = this.atDay(lengthOfMonth())

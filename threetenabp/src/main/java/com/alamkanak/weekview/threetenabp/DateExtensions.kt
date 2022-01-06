package com.alamkanak.weekview.threetenabp

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

internal typealias JsrLocalDate = java.time.LocalDate
internal typealias JsrLocalTime = java.time.LocalTime
internal typealias JsrLocalDateTime = java.time.LocalDateTime

internal fun LocalDate.toJsrLocalDate(): JsrLocalDate {
    return JsrLocalDate.of(year, monthValue, dayOfMonth)
}

internal fun LocalTime.toJsrLocalTime(): JsrLocalTime {
    return JsrLocalTime.of(hour, minute, second, nano)
}

internal fun LocalDateTime.toJsrLocalDateTime(): JsrLocalDateTime {
    return JsrLocalDateTime.of(year, monthValue, dayOfMonth, hour, minute, second, nano)
}

internal fun JsrLocalDate.toThreeTenLocalDate(): LocalDate {
    return LocalDate.of(year, monthValue, dayOfMonth)
}

internal fun JsrLocalDateTime.toThreeTenLocalDateTime(): LocalDateTime {
    return LocalDateTime.of(year, monthValue, dayOfMonth, hour, minute, second, nano)
}

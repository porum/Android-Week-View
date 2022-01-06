package com.alamkanak.weekview.jodatime

import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime

internal typealias JsrLocalDate = java.time.LocalDate
internal typealias JsrLocalTime = java.time.LocalTime
internal typealias JsrLocalDateTime = java.time.LocalDateTime

internal fun LocalDate.toJsrLocalDate(): JsrLocalDate {
    return JsrLocalDate.of(year, monthOfYear, dayOfMonth)
}

internal fun LocalTime.toJsrLocalTime(): JsrLocalTime {
    return JsrLocalTime.of(hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond * 1_000_000)
}

internal fun LocalDateTime.toJsrLocalDateTime(): JsrLocalDateTime {
    return JsrLocalDateTime.of(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond * 1_000_000)
}

internal fun JsrLocalDate.toJodaLocalDate(): LocalDate {
    return LocalDate(year, monthValue, dayOfMonth)
}

internal fun JsrLocalDateTime.toJodaLocalDateTime(): LocalDateTime {
    return LocalDateTime(year, monthValue, dayOfMonth, hour, minute, second, nano / 1_000_000)
}

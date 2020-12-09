package com.procurement.access.domain.util.extension

import com.procurement.access.domain.fail.error.DataTimeError
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asFailure
import com.procurement.access.lib.functional.asSuccess
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

private const val FORMAT_PATTERN = "uuuu-MM-dd'T'HH:mm:ss'Z'"
private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT_PATTERN)
    .withResolverStyle(ResolverStyle.STRICT)

fun nowDefaultUTC(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

fun LocalDateTime.asString(): String = this.format(formatter)

fun String.toLocalDateTime(): Result<LocalDateTime, DataTimeError> = try {
    LocalDateTime.parse(this, formatter).asSuccess()
} catch (expected: Exception) {
    if (expected.cause == null)
        DataTimeError.InvalidFormat(value = this, pattern = FORMAT_PATTERN, reason = expected).asFailure()
    else
        DataTimeError.InvalidDateTime(value = this, reason = expected).asFailure()
}

fun LocalDateTime.toMilliseconds(): Long = this.toInstant(ZoneOffset.UTC).toEpochMilli()

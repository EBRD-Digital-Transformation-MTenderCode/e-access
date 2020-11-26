package com.procurement.access.domain.model.date

import com.procurement.access.lib.functional.Result
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

private const val formatPattern = "uuuu-MM-dd'T'HH:mm:ss'Z'"
private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(formatPattern)
    .withResolverStyle(ResolverStyle.STRICT)

fun LocalDateTime.format(): String = this.format(formatter)

fun String.parseLocalDateTime(): LocalDateTime = LocalDateTime.parse(this, formatter)

fun String.tryParseLocalDateTime(): Result<LocalDateTime, String> = try {
    Result.success(this.parseLocalDateTime())
} catch (ignore: Exception) {
    Result.failure(formatPattern)
}

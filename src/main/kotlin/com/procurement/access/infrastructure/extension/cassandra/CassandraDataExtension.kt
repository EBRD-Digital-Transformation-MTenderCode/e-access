package com.procurement.access.infrastructure.extension.cassandra

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

fun Date.toLocalDateTime(zoneId: ZoneId = ZoneOffset.UTC): LocalDateTime = toInstant().atZone(zoneId).toLocalDateTime()

fun LocalDateTime.toCassandraTimestamp(zoneId: ZoneId = ZoneOffset.UTC): Date = Date.from(atZone(zoneId).toInstant())

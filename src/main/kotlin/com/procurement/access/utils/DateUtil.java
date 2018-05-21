package com.procurement.access.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class DateUtil {

    public Date nowDateTime() {
        return localToDate(nowUTCLocalDateTime());
    }

    public LocalDateTime nowUTCLocalDateTime() {
        return LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
    }

    public long milliNowUTC() {
        return nowUTCLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public Date localToDate(final LocalDateTime startDate) {
        return Date.from(startDate.toInstant(ZoneOffset.UTC));
    }
}

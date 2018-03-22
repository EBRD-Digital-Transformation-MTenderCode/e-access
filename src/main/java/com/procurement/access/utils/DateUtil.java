package com.procurement.access.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class DateUtil {

    public LocalDateTime getNowUTC() {
        return LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
    }

    public long getMilliNowUTC() {
        return getNowUTC().toInstant(ZoneOffset.UTC)
                .toEpochMilli();
    }

    public long getMilliUTC(final LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC)
                .toEpochMilli();
    }

    public LocalDateTime dateToLocal(final Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
    }

    public Date localToDate(final LocalDateTime startDate) {
        return Date.from(startDate.toInstant(ZoneOffset.UTC));
    }
}

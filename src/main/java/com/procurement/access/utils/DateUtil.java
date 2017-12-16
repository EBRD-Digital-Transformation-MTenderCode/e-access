package com.procurement.access.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

@Component
public class DateUtil {

    public LocalDateTime getNowUTC() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public long getMilliUTC(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC)
                            .toEpochMilli();
    }
}

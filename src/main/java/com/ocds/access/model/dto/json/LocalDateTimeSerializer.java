package com.ocds.access.model.dto.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {
    private final static DateTimeFormatter formatter =
        new DateTimeFormatterBuilder().parseCaseInsensitive()
                                      .append(DateTimeFormatter.ISO_LOCAL_DATE)
                                      .appendLiteral('T')
                                      .append(DateTimeFormatter.ofPattern("HH:mm:ss.nnnnnn"))
                                      .appendLiteral('Z')
                                      .toFormatter();

    public LocalDateTimeSerializer() {
        super(LocalDateTime.class);
    }

    @Override
    public void serialize(final LocalDateTime value,
                          final JsonGenerator jsonGenerator,
                          final SerializerProvider provider) throws IOException {
        jsonGenerator.writeString(value.format(formatter));
    }

    @Override
    public Class<LocalDateTime> handledType() {
        return LocalDateTime.class;
    }
}

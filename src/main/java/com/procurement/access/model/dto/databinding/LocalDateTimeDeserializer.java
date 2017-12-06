package com.procurement.access.model.dto.databinding;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral('T')
        .append(DateTimeFormatter.ISO_LOCAL_TIME)
        .appendLiteral('Z')
        .toFormatter();

    protected LocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(final JsonParser jsonParser,
                                     final DeserializationContext deserializationContext) throws IOException {
        final String dateTime = jsonParser.getText();
        return LocalDateTime.parse(dateTime, FORMATTER);
    }
}


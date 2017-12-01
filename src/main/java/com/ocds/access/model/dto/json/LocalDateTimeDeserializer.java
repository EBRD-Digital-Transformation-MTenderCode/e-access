package com.ocds.access.model.dto.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {
    private final static DateTimeFormatter formatter =
        new DateTimeFormatterBuilder().parseCaseInsensitive()
                                      .append(DateTimeFormatter.ISO_LOCAL_DATE)
                                      .appendLiteral('T')
                                      .append(DateTimeFormatter.ofPattern("HH:mm:ss.nnnnnn"))
                                      .appendLiteral('Z')
                                      .toFormatter();

    public LocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(final JsonParser jsonParser,
                                     final DeserializationContext ctxt) throws IOException {
        final String date = jsonParser.getText();
        return LocalDateTime.parse(date, formatter);
    }
}

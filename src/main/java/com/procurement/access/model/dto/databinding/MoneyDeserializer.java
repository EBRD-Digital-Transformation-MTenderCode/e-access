package com.procurement.access.model.dto.databinding;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneyDeserializer extends JsonDeserializer<BigDecimal> {

    private NumberDeserializers.BigDecimalDeserializer delegate = NumberDeserializers.BigDecimalDeserializer.instance;

    @Override
    public BigDecimal deserialize(final JsonParser jsonParser,
                                  final DeserializationContext deserializationContext) throws IOException {
        BigDecimal bd = delegate.deserialize(jsonParser, deserializationContext);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd;
    }
}

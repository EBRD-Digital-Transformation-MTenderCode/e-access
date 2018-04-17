package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "amount",
        "currency"
})
public class Value {

    @NotNull
    @JsonProperty("amount")
    private final Double amount;

    @NotNull
    @JsonProperty("currency")
    private final Currency currency;

    @JsonCreator
    public Value(@JsonProperty("amount") final Double amount,
                 @JsonProperty("currency") final Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(amount)
                .append(currency)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Value)) {
            return false;
        }
        final Value rhs = (Value) other;
        return new EqualsBuilder().append(amount, rhs.amount)
                .append(currency, rhs.currency)
                .isEquals();
    }
}

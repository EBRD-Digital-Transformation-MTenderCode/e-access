package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Variant {

    @NotNull
    @JsonProperty("hasVariants")
    private final Boolean hasVariants;

    @JsonCreator
    public Variant(@JsonProperty("hasVariants") final Boolean hasVariants) {
        this.hasVariants = hasVariants;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(hasVariants)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Variant)) {
            return false;
        }
        final Variant rhs = (Variant) other;
        return new EqualsBuilder()
                .append(hasVariants, rhs.hasVariants)
                .isEquals();
    }
}

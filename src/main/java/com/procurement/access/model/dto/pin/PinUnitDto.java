package com.procurement.access.model.dto.pin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder("name")
public class PinUnitDto {
    @JsonProperty("name")
    @JsonPropertyDescription("Name of the unit.")
    private final String name;

    @JsonCreator
    public PinUnitDto(@JsonProperty("name") final String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PinUnitDto)) {
            return false;
        }
        final PinUnitDto rhs = (PinUnitDto) other;
        return new EqualsBuilder().append(name, rhs.name)
                .isEquals();
    }
}

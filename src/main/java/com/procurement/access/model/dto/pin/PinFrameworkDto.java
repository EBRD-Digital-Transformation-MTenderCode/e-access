package com.procurement.access.model.dto.pin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder("isAFramework")
public class PinFrameworkDto {
    @JsonProperty("isAFramework")
    @JsonPropertyDescription("A True/False field to indicate whether a framework agreement has been established as " +
            "part of this procurement")
    private final Boolean isAFramework;

    @JsonCreator
    public PinFrameworkDto(@JsonProperty("isAFramework") final Boolean isAFramework) {
        this.isAFramework = isAFramework;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(isAFramework)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PinFrameworkDto)) {
            return false;
        }
        final PinFrameworkDto rhs = (PinFrameworkDto) other;
        return new EqualsBuilder().append(isAFramework, rhs.isAFramework)
                .isEquals();
    }
}

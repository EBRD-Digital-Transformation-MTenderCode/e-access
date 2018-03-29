package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
public class Option {

    @JsonProperty("hasOptions")
    private final Boolean hasOptions;

    @JsonCreator
    public Option(@JsonProperty("hasOptions") final Boolean hasOptions) {
        this.hasOptions = hasOptions;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(hasOptions)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Option)) {
            return false;
        }
        final Option rhs = (Option) other;
        return new EqualsBuilder()
                .append(hasOptions, rhs.hasOptions)
                .isEquals();
    }
}

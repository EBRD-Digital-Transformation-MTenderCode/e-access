package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder("hasOptions")
public class CnOptionDto {
    @JsonProperty("hasOptions")
    @JsonPropertyDescription("A True/False field to indicate if lot options will be accepted. Required by the EU")
    private final Boolean hasOptions;

    @JsonCreator
    public CnOptionDto(@JsonProperty("hasOptions") final Boolean hasOptions) {
        this.hasOptions = hasOptions;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hasOptions)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof CnOptionDto)) {
            return false;
        }
        final CnOptionDto rhs = (CnOptionDto) other;
        return new EqualsBuilder().append(hasOptions, rhs.hasOptions)
                                  .isEquals();
    }
}

package com.procurement.access.model.dto.pn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder("name")
public class PnUnitDto {
    @JsonProperty("name")
    @JsonPropertyDescription("Name of the unit.")
    @NotNull
    private final String name;

    @JsonCreator
    public PnUnitDto(@JsonProperty("name") final String name) {
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
        if (!(other instanceof PnUnitDto)) {
            return false;
        }
        final PnUnitDto rhs = (PnUnitDto) other;
        return new EqualsBuilder().append(name, rhs.name)
                                  .isEquals();
    }
}

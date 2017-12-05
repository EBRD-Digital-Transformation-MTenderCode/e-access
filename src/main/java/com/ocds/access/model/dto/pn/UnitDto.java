package com.ocds.access.model.dto.pn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "name"
})
public class UnitDto {
    @JsonProperty("name")
    @JsonPropertyDescription("Name of the unit.")
    @NotNull
    private final String name;

    @JsonCreator
    public UnitDto(@JsonProperty("name") final String name) {
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
        if (!(other instanceof UnitDto)) {
            return false;
        }
        final UnitDto rhs = (UnitDto) other;
        return new EqualsBuilder().append(name, rhs.name)
                                  .isEquals();
    }

}

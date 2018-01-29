package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "property",
        "former_value"
})
public class Change {
    @JsonProperty("property")
    private final String property;

    @JsonProperty("former_value")
    private final Object formerValue;

    @JsonCreator
    public Change(@JsonProperty("property") final String property,
                  @JsonProperty("former_value") final Object formerValue) {
        this.property = property;
        this.formerValue = formerValue;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(property)
                .append(formerValue)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Change)) {
            return false;
        }
        final Change rhs = (Change) other;
        return new EqualsBuilder().append(property, rhs.property)
                .append(formerValue, rhs.formerValue)
                .isEquals();
    }
}

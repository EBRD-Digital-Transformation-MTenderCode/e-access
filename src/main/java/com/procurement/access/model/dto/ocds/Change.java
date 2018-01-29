
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
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
    @JsonPropertyDescription("The property name that has been changed relative to the place the amendment is. For " +
        "example if the contract value has changed, then the property under changes within the contract.amendment " +
        "would be value.amount. (Deprecated in 1.1)")
    private final String property;

    @JsonProperty("former_value")
    @JsonPropertyDescription("The previous value of the changed property, in whatever type the property is. " +
        "(Deprecated in 1.1)")
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

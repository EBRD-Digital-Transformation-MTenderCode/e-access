
package com.ocds.access.model.dto.pin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "hasVariants"
})
public class Variant {
    @JsonProperty("hasVariants")
    @JsonPropertyDescription("A True/False field to indicate if lot variants will be accepted. Required by the EU")
    @NotNull
    private final Boolean hasVariants;

    @JsonCreator
    public Variant(@JsonProperty("hasVariants") final Boolean hasVariants) {
        super();
        this.hasVariants = hasVariants;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hasVariants)
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
        final Variant rhs = ((Variant) other);
        return new EqualsBuilder().append(hasVariants, rhs.hasVariants)
                                  .isEquals();
    }
}

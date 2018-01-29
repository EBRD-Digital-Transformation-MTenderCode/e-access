
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "hasOptions",
    "optionDetails"
})
public class Option {
    @JsonProperty("hasOptions")
    @JsonPropertyDescription("A True/False field to indicate if lot options will be accepted. Required by the EU")
    private final Boolean hasOptions;

    @JsonProperty("optionDetails")
    @JsonPropertyDescription("Further information about the lot options that will be accepted. Required by the EU")
    private final String optionDetails;

    @JsonCreator
    public Option(@JsonProperty("hasOptions") final Boolean hasOptions,
                  @JsonProperty("optionDetails") final String optionDetails) {
        this.hasOptions = hasOptions;
        this.optionDetails = optionDetails;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hasOptions)
                                    .append(optionDetails)
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
        return new EqualsBuilder().append(hasOptions, rhs.hasOptions)
                                  .append(optionDetails, rhs.optionDetails)
                                  .isEquals();
    }
}


package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import java.util.List;
import javax.validation.Valid;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "relatedLots",
    "optionToCombine",
    "maximumValue"
})
public class LotGroup {
    @JsonProperty("id")
    @JsonPropertyDescription("A local identifier for this group of lots.")
    private final String id;

    @JsonProperty("relatedLots")
    @JsonPropertyDescription("A list of the identifiers of the lots that form this group. Lots may appear in more " +
        "than one group.")
    private final List<String> relatedLots;

    @JsonProperty("optionToCombine")
    @JsonPropertyDescription("The buyer reserves the right to combine the lots in this group when awarding a contract.")
    private final Boolean optionToCombine;

    @JsonProperty("maximumValue")
    @Valid
    private final Value maximumValue;

    @JsonCreator
    public LotGroup(@JsonProperty("id") final String id,
                    @JsonProperty("relatedLots") final List<String> relatedLots,
                    @JsonProperty("optionToCombine") final Boolean optionToCombine,
                    @JsonProperty("maximumValue") final Value maximumValue) {
        this.id = id;
        this.relatedLots = relatedLots;
        this.optionToCombine = optionToCombine;
        this.maximumValue = maximumValue;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(relatedLots)
                                    .append(optionToCombine)
                                    .append(maximumValue)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof LotGroup)) {
            return false;
        }
        final LotGroup rhs = (LotGroup) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(relatedLots, rhs.relatedLots)
                                  .append(optionToCombine, rhs.optionToCombine)
                                  .append(maximumValue, rhs.maximumValue)
                                  .isEquals();
    }
}

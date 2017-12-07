
package com.procurement.access.model.dto.pn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.validation.Valid;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "description",
    "classification",
    "additionalClassifications",
    "quantity",
    "unit"
})
public class PnItemDto {

    @JsonProperty("description")
    @JsonPropertyDescription("A description of the goods, services to be provided.")
    private final String description;

    @JsonProperty("classification")
    @Valid
    private final PnClassificationDto classification;

    @JsonProperty("additionalClassifications")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("An array of additional classifications for the item. See the [itemClassificationScheme]" +
        "(http://standard.open-contracting.org/latest/en/schema/codelists/#item-classification-scheme) codelist for " +
        "common options to use in OCDS. This may also be used to present codes from an internal classification scheme.")
    @Valid
    private final Set<PnClassificationDto> additionalClassifications;

    @JsonProperty("quantity")
    @JsonPropertyDescription("The number of units required")
    private final Double quantity;

    @JsonProperty("unit")
    @JsonPropertyDescription("A description of the unit in which the supplies, services or works are provided (e.g. " +
        "hours, kilograms) and the unit-price. For comparability, an established list of units can be used.  ")
    @Valid
    private final PnUnitDto unit;

    @JsonCreator
    public PnItemDto(@JsonProperty("description") final String description,
                     @JsonProperty("classification") final PnClassificationDto classification,
                     @JsonProperty("additionalClassifications") final LinkedHashSet<PnClassificationDto>
                         additionalClassifications,
                     @JsonProperty("quantity") final Double quantity,
                     @JsonProperty("unit") final PnUnitDto unit) {

        this.description = description;
        this.classification = classification;
        this.additionalClassifications = additionalClassifications;
        this.quantity = quantity;
        this.unit = unit;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(description)
                                    .append(classification)
                                    .append(additionalClassifications)
                                    .append(quantity)
                                    .append(unit)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PnItemDto)) {
            return false;
        }
        final PnItemDto rhs = (PnItemDto) other;
        return new EqualsBuilder().append(description, rhs.description)
                                  .append(classification, rhs.classification)
                                  .append(additionalClassifications, rhs.additionalClassifications)
                                  .append(quantity, rhs.quantity)
                                  .append(unit, rhs.unit)
                                  .isEquals();
    }
}

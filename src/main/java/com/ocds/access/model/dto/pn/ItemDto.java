
package com.ocds.access.model.dto.pn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
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
public class ItemDto {

    @JsonProperty("description")
    @JsonPropertyDescription("A description of the goods, services to be provided.")
    @Pattern(regexp = "^(description_(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5," +
        "8})(-([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z]" +
        "(-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)))$")
    private final String description;

    @JsonProperty("classification")
    @Valid
    private final ClassificationDto classification;

    @JsonProperty("additionalClassifications")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("An array of additional classifications for the item. See the [itemClassificationScheme]" +
        "(http://standard.open-contracting.org/latest/en/schema/codelists/#item-classification-scheme) codelist for " +
        "common options to use in OCDS. This may also be used to present codes from an internal classification scheme.")
    @Valid
    private final Set<ClassificationDto> additionalClassifications;

    @JsonProperty("quantity")
    @JsonPropertyDescription("The number of units required")
    private final Double quantity;

    @JsonProperty("unit")
    @JsonPropertyDescription("A description of the unit in which the supplies, services or works are provided (e.g. " +
        "hours, kilograms) and the unit-price. For comparability, an established list of units can be used.  ")
    @Valid
    private final UnitDto unit;


    @JsonCreator
    public ItemDto(@JsonProperty("description") final String description,
                   @JsonProperty("classification") final ClassificationDto classification,
                   @JsonProperty("additionalClassifications") final LinkedHashSet<ClassificationDto>
                    additionalClassifications,
                   @JsonProperty("quantity") final Double quantity,
                   @JsonProperty("unit") final UnitDto unit) {

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
        if (!(other instanceof ItemDto)) {
            return false;
        }
        final ItemDto rhs = (ItemDto) other;
        return new EqualsBuilder().append(description, rhs.description)
                                  .append(classification, rhs.classification)
                                  .append(additionalClassifications, rhs.additionalClassifications)
                                  .append(quantity, rhs.quantity)
                                  .append(unit, rhs.unit)
                                  .isEquals();
    }
}

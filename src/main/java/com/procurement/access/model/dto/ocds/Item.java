
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "description",
    "classification",
    "additionalClassifications",
    "quantity",
    "unit",
    "relatedLot"
})
public class Item {
    @JsonProperty("id")
    @JsonPropertyDescription("A local identifier to reference and merge the items by. Must be unique within a given " +
        "array of items.")
    private  String id;

    @JsonProperty("description")
    @JsonPropertyDescription("A description of the goods, services to be provided.")
    @Pattern(regexp = "^(description_(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5," +
        "8})(-([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z]" +
        "(-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)))$")
    private final String description;

    @JsonProperty("classification")
    @Valid
    private final Classification classification;

    @JsonProperty("additionalClassifications")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("An array of additional classifications for the item. See the [itemClassificationScheme]" +
        "(http://standard.open-contracting.org/latest/en/schema/codelists/#item-classification-scheme) codelist for " +
        "common options to use in OCDS. This may also be used to present codes from an internal classification scheme.")
    @Valid
    private final Set<Classification> additionalClassifications;

    @JsonProperty("quantity")
    @JsonPropertyDescription("The number of units required")
    private final Double quantity;

    @JsonProperty("unit")
    @JsonPropertyDescription("A description of the unit in which the supplies, services or works are provided (e.g. " +
        "hours, kilograms) and the unit-price. For comparability, an established list of units can be used.  ")
    @Valid
    private final Unit unit;

    @JsonProperty("relatedLot")
    @JsonPropertyDescription("If this item belongs to a lot, provide the identifier(s) of the related lot(s) here.")
    private String relatedLot;

    @JsonCreator
    public Item(@JsonProperty("id") final String id,
                @JsonProperty("description") final String description,
                @JsonProperty("classification") final Classification classification,
                @JsonProperty("additionalClassifications") final LinkedHashSet<Classification>
                    additionalClassifications,
                @JsonProperty("quantity") final Double quantity,
                @JsonProperty("unit") final Unit unit,
                @JsonProperty("relatedLot") final String relatedLot) {
        this.id = id;
        this.description = description;
        this.classification = classification;
        this.additionalClassifications = additionalClassifications;
        this.quantity = quantity;
        this.unit = unit;
        this.relatedLot = relatedLot;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(description)
                                    .append(classification)
                                    .append(additionalClassifications)
                                    .append(quantity)
                                    .append(unit)
                                    .append(relatedLot)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Item)) {
            return false;
        }
        final Item rhs = (Item) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(description, rhs.description)
                                  .append(classification, rhs.classification)
                                  .append(additionalClassifications, rhs.additionalClassifications)
                                  .append(quantity, rhs.quantity)
                                  .append(unit, rhs.unit)
                                  .append(relatedLot, rhs.relatedLot)
                                  .isEquals();
    }
}

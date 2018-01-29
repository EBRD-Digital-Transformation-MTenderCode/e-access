package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.validation.Valid;
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
    @JsonProperty("description")
    private final String description;
    @JsonProperty("classification")
    @Valid
    private final Classification classification;
    @JsonProperty("additionalClassifications")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private final Set<Classification> additionalClassifications;
    @JsonProperty("quantity")
    private final Double quantity;
    @JsonProperty("unit")
    @Valid
    private final Unit unit;
    @JsonProperty("id")
    private String id;
    @JsonProperty("relatedLot")
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

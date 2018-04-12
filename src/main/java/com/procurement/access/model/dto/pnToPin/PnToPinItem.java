package com.procurement.access.model.dto.pnToPin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.procurement.access.model.dto.ocds.Classification;
import com.procurement.access.model.dto.ocds.Unit;
import java.util.HashSet;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "classification",
    "additionalClassifications",
    "quantity",
    "unit",
    "description",
    "relatedLot"
})
public class PnToPinItem {
    @NotNull
    @JsonProperty("id")
    private String id;

    @Valid
    @NotNull
    @JsonProperty("classification")
    private final Classification classification;

    @Valid
    @JsonProperty("additionalClassifications")
    private final Set<Classification> additionalClassifications;

    @NotNull
    @JsonProperty("quantity")
    private final Double quantity;

    @Valid
    @NotNull
    @JsonProperty("unit")
    private final Unit unit;

    @JsonProperty("description")
    private final String description;

    @NotNull
    @JsonProperty("relatedLot")
    private String relatedLot;

    @JsonCreator
    public PnToPinItem(@JsonProperty("id") final String id,
                       @JsonProperty("description") final String description,
                       @JsonProperty("classification") final Classification classification,
                       @JsonProperty("additionalClassifications") final HashSet<Classification>
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
        if (!(other instanceof PnToPinItem)) {
            return false;
        }
        final PnToPinItem rhs = (PnToPinItem) other;
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

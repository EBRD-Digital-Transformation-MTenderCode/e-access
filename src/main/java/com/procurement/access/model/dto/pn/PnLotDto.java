
package com.procurement.access.model.dto.pn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "id",
    "title",
    "description",
    "value"
})
public class PnLotDto {
    @JsonProperty("id")
    @JsonPropertyDescription("A local identifier for this lot, such as a lot number. This is used in relatedLot " +
        "references at the item, document and award level.")
    @NotNull
    private final String id;

    @JsonProperty("title")
    @JsonPropertyDescription("A title for this lot.")
    @NotNull
    private final String title;

    @JsonProperty("description")
    @JsonPropertyDescription("A description of this lot.")
    @NotNull
    private final String description;

    @JsonProperty("value")
    @Valid
    @NotNull
    private final PnValueDto value;

    @JsonCreator
    public PnLotDto(@JsonProperty("id") final String id,
                    @JsonProperty("title") final String title,
                    @JsonProperty("description") final String description,
                    @JsonProperty("value") final PnValueDto value) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.value = value;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(title)
                                    .append(description)
                                    .append(value)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PnLotDto)) {
            return false;
        }
        final PnLotDto rhs = (PnLotDto) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(title, rhs.title)
                                  .append(description, rhs.description)
                                  .append(value, rhs.value)
                                  .isEquals();
    }
}

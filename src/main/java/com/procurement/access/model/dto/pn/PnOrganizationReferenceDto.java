package com.procurement.access.model.dto.pn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
        "name",
        "id"
})
public class PnOrganizationReferenceDto {
    @JsonProperty("id")
    @JsonPropertyDescription("The id of the party being referenced. This must match the id of an entry in the parties" +
            " section.")
    @NotNull
    private final String id;

    @JsonProperty("name")
    @JsonPropertyDescription("The name of the party being referenced. This must match the name of an entry in the " +
            "parties section.")
    @Size(min = 1)
    @NotNull
    private final String name;

    @JsonCreator
    public PnOrganizationReferenceDto(@JsonProperty("name") final String name,
                                      @JsonProperty("id") final String id) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name)
                .append(id)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PnOrganizationReferenceDto)) {
            return false;
        }
        final PnOrganizationReferenceDto rhs = (PnOrganizationReferenceDto) other;
        return new EqualsBuilder().append(name, rhs.name)
                .append(id, rhs.id)
                .isEquals();
    }
}

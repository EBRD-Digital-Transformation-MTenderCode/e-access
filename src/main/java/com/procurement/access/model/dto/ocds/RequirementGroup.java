
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "description",
    "requirements"
})
public class RequirementGroup {
    @JsonProperty("id")
    @JsonPropertyDescription("The identifier for this requirement group. It must be unique and cannot change within " +
        "the Open Contracting Process it is part of (defined by a single ocid). See the [identifier guidance]" +
        "(http://standard.open-contracting.org/latest/en/schema/identifiers/) for further details.")
    @NotNull
    private final String id;

    @JsonProperty("description")
    @JsonPropertyDescription("Requirement group description")
    private final String description;

    @JsonProperty("requirements")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("A list requirements which must all be satisified for the requirement group to be met.")
    @Valid
    private final Set<Requirement> requirements;

    @JsonCreator
    public RequirementGroup(@JsonProperty("id") final String id,
                            @JsonProperty("description") final String description,
                            @JsonProperty("requirements") final LinkedHashSet<Requirement> requirements) {
        this.id = id;
        this.description = description;
        this.requirements = requirements;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(description)
                                    .append(requirements)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof RequirementGroup)) {
            return false;
        }
        final RequirementGroup rhs = (RequirementGroup) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(description, rhs.description)
                                  .append(requirements, rhs.requirements)
                                  .isEquals();
    }
}

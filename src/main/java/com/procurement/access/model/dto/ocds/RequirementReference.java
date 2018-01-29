
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "title"
})
public class RequirementReference {
    @JsonProperty("id")
    @JsonPropertyDescription("The id of the requirement which the response is applicable to")
    @NotNull
    private final String id;

    @JsonProperty("title")
    @JsonPropertyDescription("The title of the requirement which the response is applicable to")
    private final String title;

    @JsonCreator
    public RequirementReference(@JsonProperty("id") final String id,
                                @JsonProperty("title") final String title) {
        this.id = id;
        this.title = title;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(title)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof RequirementReference)) {
            return false;
        }
        final RequirementReference rhs = (RequirementReference) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(title, rhs.title)
                                  .isEquals();
    }
}

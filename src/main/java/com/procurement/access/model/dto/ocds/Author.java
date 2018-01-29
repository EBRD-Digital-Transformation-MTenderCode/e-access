
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name"
})
public class Author {
    @JsonProperty("id")
    @JsonPropertyDescription("A unique identifier for the author.")
    private final String id;

    @JsonProperty("name")
    @JsonPropertyDescription("The name of the author.")
    private final String name;

    @JsonCreator
    public Author(@JsonProperty("id") final String id,
                  @JsonProperty("name") final String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(name)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Author)) {
            return false;
        }
        final Author rhs = (Author) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(name, rhs.name)
                                  .isEquals();
    }
}

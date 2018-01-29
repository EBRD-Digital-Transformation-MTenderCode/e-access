package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
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
    @NotNull
    private final String id;

    @JsonProperty("name")
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

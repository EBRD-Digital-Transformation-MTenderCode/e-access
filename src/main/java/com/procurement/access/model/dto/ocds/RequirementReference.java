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
        "title"
})
public class RequirementReference {
    @JsonProperty("id")
    @NotNull
    private final String id;

    @JsonProperty("title")
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

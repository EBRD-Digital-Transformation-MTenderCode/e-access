package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
public class Unit {

    @NotNull
    @JsonProperty("id")
    private final String id;

    @NotNull
    @JsonProperty("name")
    private final String name;

    @JsonCreator
    public Unit(@JsonProperty("id") final String id,
                @JsonProperty("name") final String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(name)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Unit)) {
            return false;
        }
        final Unit rhs = (Unit) other;
        return new EqualsBuilder()
                .append(id, rhs.id)
                .append(name, rhs.name)
                .isEquals();
    }
}

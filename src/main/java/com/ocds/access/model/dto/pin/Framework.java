
package com.ocds.access.model.dto.pin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "isAFramework"
})
public class Framework {
    @JsonProperty("isAFramework")
    @JsonPropertyDescription("A True/False field to indicate whether a framework agreement has been established as " +
        "part of this procurement")
    private final Boolean isAFramework;

    @JsonCreator
    public Framework(@JsonProperty("isAFramework") final Boolean isAFramework) {
        this.isAFramework = isAFramework;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(isAFramework)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Framework)) {
            return false;
        }
        final Framework rhs = (Framework) other;
        return new EqualsBuilder().append(isAFramework, rhs.isAFramework)
                                  .isEquals();
    }
}

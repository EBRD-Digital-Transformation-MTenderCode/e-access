package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
public class Framework {

    @NotNull
    @JsonProperty("isAFramework")
    private final Boolean isAFramework;

    @JsonCreator
    public Framework(@JsonProperty("isAFramework") final Boolean isAFramework) {
        this.isAFramework = isAFramework;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(isAFramework)
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
        return new EqualsBuilder()
                .append(isAFramework, rhs.isAFramework)
                .isEquals();
    }
}

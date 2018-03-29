package com.procurement.access.model.dto.lots;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
public class LotDto {

    @JsonProperty(value = "id")
    private String id;

    public LotDto(@JsonProperty("id") final String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof LotDto)) {
            return false;
        }
        final LotDto rhs = (LotDto) other;
        return new EqualsBuilder()
                .append(id, rhs.id)
                .isEquals();
    }
}

package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
public class LotGroup {

    @NotNull
    @JsonProperty("optionToCombine")
    private final Boolean optionToCombine;

    @JsonCreator
    public LotGroup(@JsonProperty("optionToCombine") final Boolean optionToCombine) {
        this.optionToCombine = optionToCombine;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(optionToCombine)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof LotGroup)) {
            return false;
        }
        final LotGroup rhs = (LotGroup) other;
        return new EqualsBuilder()
                .append(optionToCombine, rhs.optionToCombine)
                .isEquals();
    }
}

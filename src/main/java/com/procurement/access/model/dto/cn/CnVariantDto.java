package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder("hasVariants")
public class CnVariantDto {
    @JsonProperty("hasVariants")
    @JsonPropertyDescription("A True/False field to indicate if lot variants will be accepted. Required by the EU")
    @NotNull
    private final Boolean hasVariants;

    @JsonCreator
    public CnVariantDto(@JsonProperty("hasVariants") final Boolean hasVariants) {
        super();
        this.hasVariants = hasVariants;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hasVariants)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof CnVariantDto)) {
            return false;
        }
        final CnVariantDto rhs = (CnVariantDto) other;
        return new EqualsBuilder().append(hasVariants, rhs.hasVariants)
                                  .isEquals();
    }
}

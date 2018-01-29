package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.validation.Valid;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "relatedLots",
        "optionToCombine",
        "maximumValue"
})
public class LotGroup {
    @JsonProperty("id")
    private final String id;

    @JsonProperty("relatedLots")
    private final List<String> relatedLots;

    @JsonProperty("optionToCombine")
    private final Boolean optionToCombine;

    @JsonProperty("maximumValue")
    @Valid
    private final Value maximumValue;

    @JsonCreator
    public LotGroup(@JsonProperty("id") final String id,
                    @JsonProperty("relatedLots") final List<String> relatedLots,
                    @JsonProperty("optionToCombine") final Boolean optionToCombine,
                    @JsonProperty("maximumValue") final Value maximumValue) {
        this.id = id;
        this.relatedLots = relatedLots;
        this.optionToCombine = optionToCombine;
        this.maximumValue = maximumValue;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                .append(relatedLots)
                .append(optionToCombine)
                .append(maximumValue)
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
        return new EqualsBuilder().append(id, rhs.id)
                .append(relatedLots, rhs.relatedLots)
                .append(optionToCombine, rhs.optionToCombine)
                .append(maximumValue, rhs.maximumValue)
                .isEquals();
    }
}

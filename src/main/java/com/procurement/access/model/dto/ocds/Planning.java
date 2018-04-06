package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "budget",
        "rationale"
})
public class Planning {

    @JsonProperty("rationale")
    private final String rationale;

    @Valid
    @NotNull
    @JsonProperty("budget")
    private final Budget budget;

    @JsonCreator
    public Planning(@JsonProperty("budget") final Budget budget,
                    @JsonProperty("rationale") final String rationale) {
        this.budget = budget;
        this.rationale = rationale;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(budget)
                .append(rationale)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Planning)) {
            return false;
        }
        final Planning rhs = (Planning) other;
        return new EqualsBuilder()
                .append(rationale, rhs.rationale)
                .append(budget, rhs.budget)
                .isEquals();
    }
}

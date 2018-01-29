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
        "statistics",
        "details"
})
public class Bids {
    @JsonProperty("statistics")
    private final List<BidsStatistic> statistics;

    @JsonProperty("details")
    @Valid
    private final List<Bid> details;


    @JsonCreator
    public Bids(@JsonProperty("statistics") final List<BidsStatistic> statistics,
                @JsonProperty("details") final List<Bid> details) {
        this.statistics = statistics;
        this.details = details;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(statistics)
                .append(details)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Bids)) {
            return false;
        }
        final Bids rhs = (Bids) other;
        return new EqualsBuilder().append(statistics, rhs.statistics)
                .append(details, rhs.details)
                .isEquals();
    }
}

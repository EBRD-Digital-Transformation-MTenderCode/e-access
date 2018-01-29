package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "buyerProcedureReview",
        "reviewBodyChallenge",
        "legalProcedures"
})
public class ReviewProceedings {
    @JsonProperty("buyerProcedureReview")
    private final Boolean buyerProcedureReview;

    @JsonProperty("reviewBodyChallenge")
    private final Boolean reviewBodyChallenge;

    @JsonProperty("legalProcedures")
    @JsonDeserialize(as = LinkedHashSet.class)
    private final Set<LegalProceedings> legalProcedures;

    public ReviewProceedings(@JsonProperty("buyerProcedureReview") final Boolean buyerProcedureReview,
                             @JsonProperty("reviewBodyChallenge") final Boolean reviewBodyChallenge,
                             @JsonProperty("legalProcedures") final LinkedHashSet<LegalProceedings> legalProcedures) {
        this.buyerProcedureReview = buyerProcedureReview;
        this.reviewBodyChallenge = reviewBodyChallenge;
        this.legalProcedures = legalProcedures;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(buyerProcedureReview)
                .append(reviewBodyChallenge)
                .append(legalProcedures)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ReviewProceedings)) {
            return false;
        }
        final ReviewProceedings rhs = (ReviewProceedings) other;
        return new EqualsBuilder().append(buyerProcedureReview, rhs.buyerProcedureReview)
                .append(reviewBodyChallenge, rhs.reviewBodyChallenge)
                .append(legalProcedures, rhs.legalProcedures)
                .isEquals();
    }
}

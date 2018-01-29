package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.validation.Valid;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "transactions",
        "milestones",
        "documents"
})
public class Implementation {
    @JsonProperty("transactions")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private final Set<Transaction> transactions;

    @JsonProperty("milestones")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private final Set<Milestone> milestones;

    @JsonProperty("documents")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private final Set<Document> documents;

    @JsonCreator
    public Implementation(@JsonProperty("transactions") final LinkedHashSet<Transaction> transactions,
                          @JsonProperty("milestones") final LinkedHashSet<Milestone> milestones,
                          @JsonProperty("documents") final LinkedHashSet<Document> documents) {
        this.transactions = transactions;
        this.milestones = milestones;
        this.documents = documents;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(transactions)
                .append(milestones)
                .append(documents)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Implementation)) {
            return false;
        }
        final Implementation rhs = (Implementation) other;
        return new EqualsBuilder().append(transactions, rhs.transactions)
                .append(milestones, rhs.milestones)
                .append(documents, rhs.documents)
                .isEquals();
    }
}

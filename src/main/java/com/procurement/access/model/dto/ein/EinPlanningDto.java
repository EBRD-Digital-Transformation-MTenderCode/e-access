package com.procurement.access.model.dto.ein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.procurement.access.model.dto.ocds.Budget;
import com.procurement.access.model.dto.ocds.Document;
import com.procurement.access.model.dto.ocds.Milestone;
import java.util.List;
import javax.validation.Valid;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "budget",
        "rationale",
        "documents",
        "milestones"
})
public class EinPlanningDto {
    @JsonProperty("rationale")
    private final String rationale;

    @JsonProperty("budget")
    @Valid
    private final EinBudgetDto budget;

    @JsonProperty("documents")
    @Valid
    private final List<Document> documents;

    @JsonProperty("milestones")
    @Valid
    private final List<Milestone> milestones;

    @JsonCreator
    public EinPlanningDto(@JsonProperty("budget") final EinBudgetDto budget,
                          @JsonProperty("rationale") final String rationale,
                          @JsonProperty("documents") final List<Document> documents,
                          @JsonProperty("milestones") final List<Milestone> milestones) {
        this.budget = budget;
        this.rationale = rationale;
        this.documents = documents;
        this.milestones = milestones;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(budget)
                .append(rationale)
                .append(documents)
                .append(milestones)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof EinPlanningDto)) {
            return false;
        }
        final EinPlanningDto rhs = (EinPlanningDto) other;
        return new EqualsBuilder().append(rationale, rhs.rationale)
                .append(budget, rhs.budget)
                .append(documents, rhs.documents)
                .append(milestones, rhs.milestones)
                .isEquals();
    }
}

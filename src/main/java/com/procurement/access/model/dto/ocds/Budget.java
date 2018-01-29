package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.util.List;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "description",
        "amount",
        "project",
        "projectID",
        "uri",
        "source",
        "europeanUnionFunding",
        "isEuropeanUnionFunded",
        "budgetBreakdown"
})
public class Budget {
    @JsonProperty("description")
    private final String description;
    @JsonProperty("amount")
    @Valid
    private final Value amount;
    @JsonProperty("project")
    private final String project;
    @JsonProperty("projectID")
    private final String projectID;
    @JsonProperty("uri")
    private final String uri;
    @JsonProperty("source")
    private final String source;
    @JsonProperty("europeanUnionFunding")
    @Valid
    private final EuropeanUnionFunding europeanUnionFunding;
    @JsonProperty("isEuropeanUnionFunded")
    private final Boolean isEuropeanUnionFunded;
    @JsonProperty("budgetBreakdown")
    private final List<BudgetBreakdown> budgetBreakdown;
    @JsonProperty("id")
    private String id;

    @JsonCreator
    public Budget(@JsonProperty("id") final String id,
                  @JsonProperty("description") final String description,
                  @JsonProperty("amount") final Value amount,
                  @JsonProperty("project") final String project,
                  @JsonProperty("projectID") final String projectID,
                  @JsonProperty("uri") final String uri,
                  @JsonProperty("source") final String source,
                  @JsonProperty("europeanUnionFunding") final EuropeanUnionFunding europeanUnionFunding,
                  @JsonProperty("isEuropeanUnionFunded") final Boolean isEuropeanUnionFunded,
                  @JsonProperty("budgetBreakdown") final List<BudgetBreakdown> budgetBreakdown) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.project = project;
        this.projectID = projectID;
        this.uri = uri;
        this.source = source;
        this.europeanUnionFunding = europeanUnionFunding;
        this.isEuropeanUnionFunded = isEuropeanUnionFunded;
        this.budgetBreakdown = budgetBreakdown;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                .append(description)
                .append(amount)
                .append(project)
                .append(projectID)
                .append(uri)
                .append(source)
                .append(europeanUnionFunding)
                .append(isEuropeanUnionFunded)
                .append(budgetBreakdown)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Budget)) {
            return false;
        }
        final Budget rhs = (Budget) other;
        return new EqualsBuilder().append(id, rhs.id)
                .append(description, rhs.description)
                .append(amount, rhs.amount)
                .append(project, rhs.project)
                .append(projectID, rhs.projectID)
                .append(uri, rhs.uri)
                .append(source, rhs.source)
                .append(europeanUnionFunding, rhs.europeanUnionFunding)
                .append(isEuropeanUnionFunded, rhs.isEuropeanUnionFunded)
                .append(budgetBreakdown, rhs.budgetBreakdown)
                .isEquals();
    }
}

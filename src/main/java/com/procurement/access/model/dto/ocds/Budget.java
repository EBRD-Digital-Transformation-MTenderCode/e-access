
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import java.net.URI;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
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
    @JsonProperty("id")
    @JsonPropertyDescription("An identifier for the budget line item which provides funds for this contracting " +
        "process. This identifier should be possible to cross-reference against the provided data source.")
    private final String id;

    @JsonProperty("description")
    @JsonPropertyDescription("A short free text description of the budget source. May be used to provide the title of" +
        " the budget line, or the programme used to fund this project.")
    @Pattern(regexp = "^(description_(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5," +
        "8})(-([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z]" +
        "(-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)))$")
    private final String description;

    @JsonProperty("amount")
    @Valid
    private final Value amount;

    @JsonProperty("project")
    @JsonPropertyDescription("The name of the project that through which this contracting process is funded (if " +
        "applicable). Some organizations maintain a registry of projects, and the data should use the name by which " +
        "the project is known in that registry. No translation option is offered for this string, as translated " +
        "values can be provided in third-party data, linked from the data source above.")
    @Pattern(regexp = "^(project_(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5,8})(-" +
        "([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z]" +
        "(-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)))$")
    private final String project;

    @JsonProperty("projectID")
    @JsonPropertyDescription("An external identifier for the project that this contracting process forms part of, or " +
        "is funded via (if applicable). Some organizations maintain a registry of projects, and the data should use " +
        "the identifier from the relevant registry of projects.")
    private final String projectID;

    @JsonProperty("uri")
    @JsonPropertyDescription("A URI pointing directly to a machine-readable record about the budget line-item or " +
        "line-items that fund this contracting process. Information may be provided in a range of formats, including " +
        "using IATI, the Open Fiscal Data Standard or any other standard which provides structured data on budget " +
        "sources. Human readable documents can be included using the planning.documents block.")
    private final URI uri;

    @JsonProperty("source")
    @JsonPropertyDescription("(Deprecated in 1.1) Used to point either to a corresponding Budget Data Package, or to " +
        "a machine or human-readable source where users can find further information on the budget line item " +
        "identifiers, or project identifiers, provided here.")
    @Pattern(regexp = "^(source_(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5,8})(-" +
        "([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z]" +
        "(-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)))$")
    private final URI source;

    @JsonProperty("europeanUnionFunding")
    @Valid
    private final EuropeanUnionFunding europeanUnionFunding;

    @JsonProperty("isEuropeanUnionFunded")
    @JsonPropertyDescription("A True or False field to indicate whether this procurement is related to a project " +
        "and/or programme financed by European Union funds.")
    private final Boolean isEuropeanUnionFunded;

    @JsonProperty("budgetBreakdown")
    @JsonPropertyDescription("A detailed breakdown of the budget by period and/or participating funders.")
    private final List<BudgetBreakdown> budgetBreakdown;

    @JsonCreator
    public Budget(@JsonProperty("id") final String id,
                  @JsonProperty("description") final String description,
                  @JsonProperty("amount") final Value amount,
                  @JsonProperty("project") final String project,
                  @JsonProperty("projectID") final String projectID,
                  @JsonProperty("uri") final URI uri,
                  @JsonProperty("source") final URI source,
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

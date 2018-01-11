package com.procurement.access.model.dto.fs;

import com.fasterxml.jackson.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@Setter
@JsonPropertyOrder({
        "id",
        "description",
        "period",
        "amount",
        "sourceEntity",
        "isEuropeanUnionFunded",
        "project",
        "projectID",
        "uri",
        "verified",
        "verificationDetails"
})
public class FsBudgetDto {
    @JsonProperty("description")
    @JsonPropertyDescription("A short free text description of the budget source. May be used to provide the title of" +
            " the budget line, or the programme used to fund this project.")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final String description;
    @JsonProperty("period")
    @JsonPropertyDescription("The period covered by this budget entry.")
    @Valid
    @NotNull
    private final FsPeriodDto period;
    @JsonProperty("amount")
    @Valid
    @NotNull
    private final FsValueDto amount;
    @JsonProperty("sourceEntity")
    @JsonPropertyDescription("The organization or other party related to this budget entry. If the budget amount is " +
            "positive, this indicates a flow of resources from the party to the contracting process. If the budget " +
            "amount" +
            " is negative, it indicates a payment from the contracting process to this party.")
    @Valid
    @NotNull
    private final FsOrganizationReferenceDto sourceEntity;
    @JsonProperty("isEuropeanUnionFunded")
    @JsonPropertyDescription("A True or False field to indicate whether this procurement is related to a project " +
            "and/or programme financed by European Union funds.")
    @NotNull
    private final Boolean isEuropeanUnionFunded;
    @JsonProperty("project")
    @JsonPropertyDescription("The name of the project that through which this contracting process is funded (if " +
            "applicable). Some organizations maintain a registry of projects, and the data should use the name by " +
            "which " +
            "the project is known in that registry. No translation option is offered for this string, as translated " +
            "values can be provided in third-party data, linked from the data source above.")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final String project;
    @JsonProperty("projectID")
    @JsonPropertyDescription("An external identifier for the project that this contracting process forms part of, or " +
            "is funded via (if applicable). Some organizations maintain a registry of projects, and the data should " +
            "use " +
            "the identifier from the relevant registry of projects.")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final String projectID;
    @JsonProperty("uri")
    @JsonPropertyDescription("A URI pointing directly to a machine-readable record about the budget line-item or " +
            "line-items that fund this contracting process. Information may be provided in a range of formats, " +
            "including " +
            "using IATI, the Open Fiscal Data Standard or any other standard which provides structured data on budget" +
            " " +
            "sources. Human readable documents can be included using the planning.documents block.")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final String uri;
    @JsonProperty("verified")
    @NotNull
    private final Boolean verifed;
    @JsonProperty("verificationDetails")
    @NotNull
    private final String verificationDetails;
    @JsonProperty("id")
    @JsonPropertyDescription("An identifier for the budget line item which provides funds for this contracting " +
            "process. This identifier should be possible to cross-reference against the provided data source.")
    private String id;

    @JsonCreator
    public FsBudgetDto(@JsonProperty("id") final String id,
                       @JsonProperty("description") final String description,
                       @JsonProperty("amount") final FsValueDto amount,
                       @JsonProperty("project") final String project,
                       @JsonProperty("projectID") final String projectID,
                       @JsonProperty("uri") final String uri,
                       @JsonProperty("isEuropeanUnionFunded") final Boolean isEuropeanUnionFunded,
                       @JsonProperty("sourceEntity") final FsOrganizationReferenceDto sourceEntity,
                       @JsonProperty("period") final FsPeriodDto period,
                       @JsonProperty("verified") final Boolean verified,
                       @JsonProperty("verificationDetails") final String verificationDetails) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.project = project;
        this.projectID = projectID;
        this.uri = uri;
        this.isEuropeanUnionFunded = isEuropeanUnionFunded;
        this.sourceEntity = sourceEntity;
        this.period = period;
        this.verifed = verified;
        this.verificationDetails = verificationDetails;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                .append(description)
                .append(amount)
                .append(project)
                .append(projectID)
                .append(uri)
                .append(isEuropeanUnionFunded)
                .append(sourceEntity)
                .append(period)
                .append(verifed)
                .append(verificationDetails)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof FsBudgetDto)) {
            return false;
        }
        final FsBudgetDto rhs = (FsBudgetDto) other;
        return new EqualsBuilder().append(id, rhs.id)
                .append(description, rhs.description)
                .append(amount, rhs.amount)
                .append(project, rhs.project)
                .append(projectID, rhs.projectID)
                .append(uri, rhs.uri)
                .append(isEuropeanUnionFunded, rhs.isEuropeanUnionFunded)
                .append(sourceEntity, rhs.sourceEntity)
                .append(period, rhs.period)
                .append(verifed, rhs.verifed)
                .append(verificationDetails, rhs.verificationDetails)
                .isEquals();
    }
}

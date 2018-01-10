package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder({
        "id",
        "description",
        "amount",
        "period",
        "sourceParty"
})
public class CnBudgetBreakdownDto {
    @JsonProperty("id")
    @JsonPropertyDescription("An identifier for this particular budget entry.")
    @NotNull
    private final String id;

    @JsonProperty("description")
    @JsonPropertyDescription("A short free text description of this budget entry.")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final String description;

    @JsonProperty("amount")
    @Valid
    @NotNull
    private final CnValueDto amount;

    @JsonProperty("period")
    @Valid
    @NotNull
    private final CnPeriodDto period;

    @JsonProperty("sourceParty")
    @JsonPropertyDescription("The id and name of the party being referenced. Used to cross-reference to the parties " +
            "section")
    @Valid
    @NotNull
    private final CnOrganizationReferenceDto sourceParty;

    @JsonCreator
    public CnBudgetBreakdownDto(@JsonProperty("id") final String id,
                                @JsonProperty("description") final String description,
                                @JsonProperty("amount") final CnValueDto amount,
                                @JsonProperty("period") final CnPeriodDto period,
                                @JsonProperty("sourceParty") final CnOrganizationReferenceDto sourceParty) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.period = period;
        this.sourceParty = sourceParty;
    }
}

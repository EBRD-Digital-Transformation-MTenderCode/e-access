package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.*;
import java.util.List;
import javax.validation.Valid;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "description",
        "amount",
        "isEuropeanUnionFunded",
        "europeanUnionFunding",
        "budgetBreakdown"
})
public class CnBudgetDto {
    @JsonProperty("id")
    @JsonPropertyDescription("An identifier for the budget line item which provides funds for this contracting " +
            "process. This identifier should be possible to cross-reference against the provided data source.")
    private final String id;

    @JsonProperty("description")
    @JsonPropertyDescription("A short free text description of the budget source. May be used to provide the title of" +
            " the budget line, or the programme used to fund this project.")
    private final String description;

    @JsonProperty("amount")
    @Valid
    private final CnValueDto amount;

    @JsonProperty("isEuropeanUnionFunded")
    @JsonPropertyDescription("A True or False field to indicate whether this procurement is related to a project " +
            "and/or programme financed by European Union funds.")
    private final Boolean isEuropeanUnionFunded;

    @JsonProperty("europeanUnionFunding")
    @Valid
    private final CnEuropeanUnionFundingDto europeanUnionFunding;

    @JsonProperty("budgetBreakdown")
    @JsonPropertyDescription("A detailed breakdown of the budget by period and/or participating funders.")
    private final List<CnBudgetBreakdownDto> budgetBreakdown;

    @JsonCreator
    public CnBudgetDto(@JsonProperty("id") final String id,
                       @JsonProperty("description") final String description,
                       @JsonProperty("amount") final CnValueDto amount,
                       @JsonProperty("europeanUnionFunding") final CnEuropeanUnionFundingDto europeanUnionFunding,
                       @JsonProperty("isEuropeanUnionFunded") final Boolean isEuropeanUnionFunded,
                       @JsonProperty("budgetBreakdown") final List<CnBudgetBreakdownDto> budgetBreakdown) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.europeanUnionFunding = europeanUnionFunding;
        this.isEuropeanUnionFunded = isEuropeanUnionFunded;
        this.budgetBreakdown = budgetBreakdown;
    }

}

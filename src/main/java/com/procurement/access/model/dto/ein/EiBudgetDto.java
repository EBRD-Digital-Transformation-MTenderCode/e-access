package com.procurement.access.model.dto.ein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.procurement.access.model.dto.ocds.Period;
import com.procurement.access.model.dto.ocds.Value;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "period",
        "amount"
})
public class EiBudgetDto {
    @JsonProperty("id")
    private String id;
    @Valid
    @JsonProperty("period")
    private final Period period;
    @JsonProperty("amount")
    @Valid
    private final Value amount;

    @JsonCreator
    public EiBudgetDto(@JsonProperty("id") final String id,
                       @JsonProperty("period") final Period period,
                       @JsonProperty("amount") final Value amount
    ) {
        this.id = id;
        this.period = period;
        this.amount = amount;
    }
}

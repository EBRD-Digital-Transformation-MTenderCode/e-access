package com.procurement.access.model.dto.fs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.procurement.access.model.dto.ocds.Document;
import com.procurement.access.model.dto.ocds.Milestone;
import java.util.List;
import javax.validation.Valid;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "budget",
        "rationale"
})
public class FsPlanningDto {
    @JsonProperty("budget")
    @Valid
    private final FsBudgetDto budget;
    @JsonProperty("rationale")
    private final String rationale;

    @JsonCreator
    public FsPlanningDto(@JsonProperty("budget") final FsBudgetDto budget,
                         @JsonProperty("rationale") final String rationale) {
        this.budget = budget;
        this.rationale = rationale;
    }
}

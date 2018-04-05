package com.procurement.access.model.dto.tender;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.procurement.access.model.dto.ocds.Planning;
import com.procurement.access.model.dto.ocds.Tender;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({
        "planning",
        "tender"
})
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class TenderProcessRequestDto {

    @Valid
    @NotNull
    @JsonProperty("planning")
    private Planning planning;

    @Valid
    @NotNull
    @JsonProperty("tender")
    private TenderRequest tender;

    @JsonCreator
    public TenderProcessRequestDto(@JsonProperty("planning") final Planning planning,
                                   @JsonProperty("tender") final TenderRequest tender) {
        this.planning = planning;
        this.tender = tender;
    }
}

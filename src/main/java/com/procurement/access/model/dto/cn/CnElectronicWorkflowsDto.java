package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder({
        "useOrdering",
        "usePayment",
        "acceptInvoicing"
})
public class CnElectronicWorkflowsDto {
    @JsonProperty("useOrdering")
    @JsonPropertyDescription("A True/False field to indicate if electronic ordering will be used. Required by the EU")
    @NotNull
    private final Boolean useOrdering;

    @JsonProperty("usePayment")
    @JsonPropertyDescription("A True/False field to indicate if electronic payment will be used. Required by the EU")
    @NotNull
    private final Boolean usePayment;

    @JsonProperty("acceptInvoicing")
    @JsonPropertyDescription("A True/False field to indicate if electronic invoicing will be accepted. Required by " +
            "the EU")
    @NotNull
    private final Boolean acceptInvoicing;

    @JsonCreator
    public CnElectronicWorkflowsDto(@JsonProperty("useOrdering") final Boolean useOrdering,
                                    @JsonProperty("usePayment") final Boolean usePayment,
                                    @JsonProperty("acceptInvoicing") final Boolean acceptInvoicing) {
        this.useOrdering = useOrdering;
        this.usePayment = usePayment;
        this.acceptInvoicing = acceptInvoicing;
    }
}

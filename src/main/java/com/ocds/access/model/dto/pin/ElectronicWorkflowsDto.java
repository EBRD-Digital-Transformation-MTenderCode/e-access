
package com.ocds.access.model.dto.pin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "useOrdering",
    "usePayment",
    "acceptInvoicing"
})
public class ElectronicWorkflowsDto {
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
    public ElectronicWorkflowsDto(@JsonProperty("useOrdering") final Boolean useOrdering,
                                  @JsonProperty("usePayment") final Boolean usePayment,
                                  @JsonProperty("acceptInvoicing") final Boolean acceptInvoicing) {
        this.useOrdering = useOrdering;
        this.usePayment = usePayment;
        this.acceptInvoicing = acceptInvoicing;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(useOrdering)
                                    .append(usePayment)
                                    .append(acceptInvoicing)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ElectronicWorkflowsDto)) {
            return false;
        }
        final ElectronicWorkflowsDto rhs = (ElectronicWorkflowsDto) other;
        return new EqualsBuilder().append(useOrdering, rhs.useOrdering)
                                  .append(usePayment, rhs.usePayment)
                                  .append(acceptInvoicing, rhs.acceptInvoicing)
                                  .isEquals();
    }
}

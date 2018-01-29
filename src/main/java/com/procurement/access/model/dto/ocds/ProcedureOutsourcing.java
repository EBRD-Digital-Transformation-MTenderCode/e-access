package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.Valid;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "procedureOutsourced",
        "outsourcedTo"
})
public class ProcedureOutsourcing {
    @JsonProperty("procedureOutsourced")
    private final Boolean procedureOutsourced;

    @JsonProperty("outsourcedTo")
    @Valid
    private final Organization outsourcedTo;

    @JsonCreator
    public ProcedureOutsourcing(@JsonProperty("procedureOutsourced") final Boolean procedureOutsourced,
                                @JsonProperty("outsourcedTo") final Organization outsourcedTo) {
        this.procedureOutsourced = procedureOutsourced;
        this.outsourcedTo = outsourcedTo;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(procedureOutsourced)
                .append(outsourcedTo)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ProcedureOutsourcing)) {
            return false;
        }
        final ProcedureOutsourcing rhs = (ProcedureOutsourcing) other;
        return new EqualsBuilder().append(procedureOutsourced, rhs.procedureOutsourced)
                .append(outsourcedTo, rhs.outsourcedTo)
                .isEquals();
    }
}

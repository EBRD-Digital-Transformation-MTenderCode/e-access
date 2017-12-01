
package com.ocds.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "procedureOutsourced"
})
public class ProcedureOutsourcing {
    @JsonProperty("procedureOutsourced")
    @JsonPropertyDescription("A True/False field to indicate whether the procurement procedure has been outsourced")
    @NotNull
    private final Boolean procedureOutsourced;


    @JsonCreator
    public ProcedureOutsourcing(@JsonProperty("procedureOutsourced") final Boolean procedureOutsourced) {
        this.procedureOutsourced = procedureOutsourced;

    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(procedureOutsourced)
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
                                  .isEquals();
    }
}


package com.ocds.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "isRecurrent"
})
public class RecurrentProcurement {
    @JsonProperty("isRecurrent")
    @JsonPropertyDescription("A True/False field to indicate whether this is a recurrent procurement")
    @NotNull
    private final Boolean isRecurrent;


    @JsonCreator
    public RecurrentProcurement(@JsonProperty("isRecurrent") final Boolean isRecurrent) {
        this.isRecurrent = isRecurrent;

    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(isRecurrent)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof RecurrentProcurement)) {
            return false;
        }
        final RecurrentProcurement rhs = (RecurrentProcurement) other;
        return new EqualsBuilder().append(isRecurrent, rhs.isRecurrent)
                                  .isEquals();
    }
}


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
    "optionToCombine"
})
public class LotGroup {


    @JsonProperty("optionToCombine")
    @JsonPropertyDescription("The buyer reserves the right to combine the lots in this group when awarding a contract.")
    @NotNull
    private final Boolean optionToCombine;



    @JsonCreator
    public LotGroup(
                    @JsonProperty("optionToCombine") final Boolean optionToCombine) {

        this.optionToCombine = optionToCombine;

    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(optionToCombine)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof LotGroup)) {
            return false;
        }
        final LotGroup rhs = (LotGroup) other;
        return new EqualsBuilder().append(optionToCombine, rhs.optionToCombine)
                                  .isEquals();
    }
}

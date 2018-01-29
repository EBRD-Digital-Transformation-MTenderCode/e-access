
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "type",
    "description",
    "amount",
    "estimationMethod"
})
public class ValueBreakdown {
    @JsonProperty("id")
    @JsonPropertyDescription("The identifier of this value breakdown. Unique within this array.")
    @NotNull
    private final String id;

    @JsonProperty("type")
    @JsonPropertyDescription("A value from the [valueType codelist] (http://standard.open-contracting" +
        ".org/1.1-dev/en/schema/codelists/#value-type) that identifies the nature of the value in the breakdown. " +
        "Negative values indicate a revenue to the supplier.")
    @Valid
    private final List<ValueBreakdownType> type;

    @JsonProperty("description")
    @JsonPropertyDescription("The description of this value breakdown.")
    private final String description;

    @JsonProperty("amount")
    @Valid
    private final Value amount;

    @JsonProperty("estimationMethod")
    @Valid
    private final Value estimationMethod;

    @JsonCreator
    public ValueBreakdown(@JsonProperty("id") final String id,
                          @JsonProperty("type") final List<ValueBreakdownType> type,
                          @JsonProperty("description") final String description,
                          @JsonProperty("amount") final Value amount,
                          @JsonProperty("estimationMethod") final Value estimationMethod) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.estimationMethod = estimationMethod;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(type)
                                    .append(description)
                                    .append(amount)
                                    .append(estimationMethod)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ValueBreakdown)) {
            return false;
        }
        final ValueBreakdown rhs = (ValueBreakdown) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(type, rhs.type)
                                  .append(description, rhs.description)
                                  .append(amount, rhs.amount)
                                  .append(estimationMethod, rhs.estimationMethod)
                                  .isEquals();
    }

    public enum ValueBreakdownType {
        USER_FEES("userFees"),
        PUBLIC_AGENCY_FEES("publicAgencyFees");
        private final String value;
        private final static Map<String, ValueBreakdownType> CONSTANTS = new HashMap<>();

        static {
            for (final ValueBreakdownType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private ValueBreakdownType(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static ValueBreakdownType fromValue(final String value) {
            final ValueBreakdownType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            }
            return constant;
        }

    }
}

package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "value",
        "description",
        "methodOfPayment"
})
public class ParticipationFee {
    @JsonProperty("type")
    private final List<ParticipationFeeType> type;

    @JsonProperty("value")
    @Valid
    private final Value value;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("methodOfPayment")
    private final List<String> methodOfPayment;

    public ParticipationFee(@JsonProperty("type") final List<ParticipationFeeType> type,
                            @JsonProperty("value") final Value value,
                            @JsonProperty("description") final String description,
                            @JsonProperty("methodOfPayment") final List<String> methodOfPayment) {
        this.type = type;
        this.value = value;
        this.description = description;
        this.methodOfPayment = methodOfPayment;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(type)
                .append(value)
                .append(description)
                .append(methodOfPayment)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ParticipationFee)) {
            return false;
        }
        final ParticipationFee rhs = (ParticipationFee) other;
        return new EqualsBuilder().append(type, rhs.type)
                .append(value, rhs.value)
                .append(description, rhs.description)
                .append(methodOfPayment, rhs.methodOfPayment)
                .isEquals();
    }

    public enum ParticipationFeeType {
        DOCUMENT("document"),
        DEPOSIT("deposit"),
        SUBMISSION("submission"),
        WIN("win");
        private final static Map<String, ParticipationFeeType> CONSTANTS = new HashMap<>();

        static {
            for (final ParticipationFeeType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        ParticipationFeeType(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static ParticipationFeeType fromValue(final String value) {
            final ParticipationFeeType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(
                        "Unknown enum type " + value + ", Allowed values are " + Arrays.toString(values()));
            }
            return constant;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }
    }
}

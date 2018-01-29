
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import java.util.HashMap;
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
    "title",
    "description",
    "dataType",
    "pattern",
    "expectedValue",
    "minValue",
    "maxValue",
    "period"
})
public class Requirement {
    @JsonProperty("id")
    @JsonPropertyDescription("The identifier for this requirement. It must be unique and cannot change within the " +
        "Open Contracting Process it is part of (defined by a single ocid). See the [identifier guidance]" +
        "(http://standard.open-contracting.org/latest/en/schema/identifiers/) for further details.")
    @NotNull
    private final String id;

    @JsonProperty("title")
    @JsonPropertyDescription("Requirement title")
    private final String title;

    @JsonProperty("description")
    @JsonPropertyDescription("Requirement description")
    private final String description;

    @JsonProperty("dataType")
    @JsonPropertyDescription("Requirement description")
    private final DataType dataType;

    @JsonProperty("pattern")
    @JsonPropertyDescription("A regular expression against which validate the requirement response")
    private final String pattern;

    @JsonProperty("expectedValue")
    @JsonPropertyDescription("Used to state the requirement when the response must be particular value")
    private final String expectedValue;

    @JsonProperty("minValue")
    @JsonPropertyDescription("Used to state the lower bound of the requirement when the response must be within a " +
        "certain range")
    private final Double minValue;

    @JsonProperty("maxValue")
    @JsonPropertyDescription("Used to state the upper bound of the requirement when the response must be within a " +
        "certain range")
    private final Double maxValue;

    @JsonProperty("period")
    @Valid
    private final Period period;

    @JsonCreator
    public Requirement(@JsonProperty("id") final String id,
                       @JsonProperty("title") final String title,
                       @JsonProperty("description") final String description,
                       @JsonProperty("dataType") final DataType dataType,
                       @JsonProperty("pattern") final String pattern,
                       @JsonProperty("expectedValue") final String expectedValue,
                       @JsonProperty("minValue") final Double minValue,
                       @JsonProperty("maxValue") final Double maxValue,
                       @JsonProperty("period") final Period period) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dataType = dataType;
        this.pattern = pattern;
        this.expectedValue = expectedValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.period = period;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(title)
                                    .append(description)
                                    .append(dataType)
                                    .append(pattern)
                                    .append(expectedValue)
                                    .append(minValue)
                                    .append(maxValue)
                                    .append(period)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Requirement)) {
            return false;
        }
        final Requirement rhs = (Requirement) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(title, rhs.title)
                                  .append(description, rhs.description)
                                  .append(dataType, rhs.dataType)
                                  .append(pattern, rhs.pattern)
                                  .append(expectedValue, rhs.expectedValue)
                                  .append(minValue, rhs.minValue)
                                  .append(maxValue, rhs.maxValue)
                                  .append(period, rhs.period)
                                  .isEquals();
    }

    public enum DataType {
        STRING("string"),
        DATE_TIME("date-time"),
        NUMBER("number"),
        INTEGER("integer");
        private final String value;
        private final static Map<String, DataType> CONSTANTS = new HashMap<>();

        static {
            for (final Requirement.DataType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private DataType(final String value) {
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
        public static Requirement.DataType fromValue(final String value) {
            final Requirement.DataType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            }
            return constant;
        }

    }
}

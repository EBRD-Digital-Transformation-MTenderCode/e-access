
package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "isAFramework"
})
public class FrameworkDto {
    @JsonProperty("isAFramework")
    @JsonPropertyDescription("A True/False field to indicate whether a framework agreement has been established as " +
        "part of this procurement")
    @NotNull
    private final Boolean isAFramework;


    @JsonCreator
    public FrameworkDto(@JsonProperty("isAFramework") final Boolean isAFramework) {
        this.isAFramework = isAFramework;

    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(isAFramework)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof FrameworkDto)) {
            return false;
        }
        final FrameworkDto rhs = (FrameworkDto) other;
        return new EqualsBuilder().append(isAFramework, rhs.isAFramework)
                                  .isEquals();
    }

    public enum TypeOfFramework {
        WITH_REOPENING_OF_COMPETITION("WITH_REOPENING_OF_COMPETITION"),
        WITHOUT_REOPENING_OF_COMPETITION("WITHOUT_REOPENING_OF_COMPETITION"),
        PARTLY_WITH_PARTLY_WITHOUT_REOPENING_OF_COMPETITION("PARTLY_WITH_PARTLY_WITHOUT_REOPENING_OF_COMPETITION");
        private final String value;
        private final static Map<String, TypeOfFramework> CONSTANTS = new HashMap<>();

        static {
            for (final FrameworkDto.TypeOfFramework c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private TypeOfFramework(final String value) {
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
        public static TypeOfFramework fromValue(final String value) {
            final TypeOfFramework constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            }
            return constant;
        }

    }
}

package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "isAFramework",
        "typeOfFramework",
        "maxSuppliers",
        "exceptionalDurationRationale",
        "additionalBuyerCategories"
})
public class Framework {
    @JsonProperty("isAFramework")
    private final Boolean isAFramework;

    @JsonProperty("typeOfFramework")
    private final TypeOfFramework typeOfFramework;

    @JsonProperty("maxSuppliers")
    private final Integer maxSuppliers;

    @JsonProperty("exceptionalDurationRationale")
    private final String exceptionalDurationRationale;

    @JsonProperty("additionalBuyerCategories")
    private final List<String> additionalBuyerCategories;

    @JsonCreator
    public Framework(@JsonProperty("isAFramework") final Boolean isAFramework,
                     @JsonProperty("typeOfFramework") final TypeOfFramework typeOfFramework,
                     @JsonProperty("maxSuppliers") final Integer maxSuppliers,
                     @JsonProperty("exceptionalDurationRationale") final String exceptionalDurationRationale,
                     @JsonProperty("additionalBuyerCategories") final List<String> additionalBuyerCategories) {
        this.isAFramework = isAFramework;
        this.typeOfFramework = typeOfFramework;
        this.maxSuppliers = maxSuppliers;
        this.exceptionalDurationRationale = exceptionalDurationRationale;
        this.additionalBuyerCategories = additionalBuyerCategories;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(isAFramework)
                .append(typeOfFramework)
                .append(maxSuppliers)
                .append(exceptionalDurationRationale)
                .append(additionalBuyerCategories)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Framework)) {
            return false;
        }
        final Framework rhs = (Framework) other;
        return new EqualsBuilder().append(isAFramework, rhs.isAFramework)
                .append(typeOfFramework, rhs.typeOfFramework)
                .append(maxSuppliers, rhs.maxSuppliers)
                .append(exceptionalDurationRationale, rhs.exceptionalDurationRationale)
                .append(additionalBuyerCategories, rhs.additionalBuyerCategories)
                .isEquals();
    }

    public enum TypeOfFramework {
        WITH_REOPENING_OF_COMPETITION("WITH_REOPENING_OF_COMPETITION"),
        WITHOUT_REOPENING_OF_COMPETITION("WITHOUT_REOPENING_OF_COMPETITION"),
        PARTLY_WITH_PARTLY_WITHOUT_REOPENING_OF_COMPETITION("PARTLY_WITH_PARTLY_WITHOUT_REOPENING_OF_COMPETITION");
        private final static Map<String, TypeOfFramework> CONSTANTS = new HashMap<>();

        static {
            for (final Framework.TypeOfFramework c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        TypeOfFramework(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static TypeOfFramework fromValue(final String value) {
            final TypeOfFramework constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
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

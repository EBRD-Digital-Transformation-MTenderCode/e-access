
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
    @JsonPropertyDescription("A True/False field to indicate whether a framework agreement has been established as " +
        "part of this procurement")
    private final Boolean isAFramework;

    @JsonProperty("typeOfFramework")
    @JsonPropertyDescription("A value from the [typeOfFramework codelist](http://standard.open-contracting" +
        ".org/1.1-dev/en/schema/codelists/#type-of-framework). The type of buyer taken from the EU's specified list " +
        "in its TED forms.")
    private final TypeOfFramework typeOfFramework;

    @JsonProperty("maxSuppliers")
    @JsonPropertyDescription("Maximum number of economic operators to be awarded a contract")
    private final Integer maxSuppliers;

    @JsonProperty("exceptionalDurationRationale")
    @JsonPropertyDescription("Justification when the framework agreement has an exceptional duration.")
    private final String exceptionalDurationRationale;

    @JsonProperty("additionalBuyerCategories")
    @JsonPropertyDescription("Any additonal categories of buyers participating in the framework agreement and not " +
        "mentioned in the buyer section of this notice")
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
        private final String value;
        private final static Map<String, TypeOfFramework> CONSTANTS = new HashMap<>();

        static {
            for (final Framework.TypeOfFramework c : values()) {
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

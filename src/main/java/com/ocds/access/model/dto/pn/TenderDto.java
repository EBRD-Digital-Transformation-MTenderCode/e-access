
package com.ocds.access.model.dto.pn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "id",
    "title",
    "description",
    "status",
    "items",
    "minValue",
    "value",
    "procurementMethod",
    "procurementMethodDetails",
    "mainProcurementCategory",
    "additionalProcurementCategories",
    "lots",
    "classification",
    "legalBasis"
})
public class TenderDto {
    @JsonProperty("id")
    @JsonPropertyDescription("An identifier for this tender process. This may be the same as the ocid, or may be " +
        "drawn from an internally held identifier for this tender.")
    @Size(min = 1)
    @NotNull
    private final String id;

    @JsonProperty("title")
    @JsonPropertyDescription("A title for this tender. This will often be used by applications as a headline to " +
        "attract interest, and to help analysts understand the nature of this procurement.")
    @Pattern(regexp = "^(title_(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5,8})(-" +
        "([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z]" +
        "(-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)))$")
    @NotNull
    private final String title;

    @JsonProperty("description")
    @JsonPropertyDescription("A summary description of the tender. This should complement structured information " +
        "provided using the items array. Descriptions should be short and easy to read. Avoid using ALL CAPS. ")
    @Pattern(regexp = "^(description_(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5," +
        "8})(-([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z]" +
        "(-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)))$")
    @NotNull
    private final String description;

    @JsonProperty("status")
    @JsonPropertyDescription("The current status of the tender based on the [tenderStatus codelist](http://standard" +
        ".open-contracting.org/latest/en/schema/codelists/#tender-status)")
    @NotNull
    private final TenderStatusDto status;

    @JsonProperty("items")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("The goods and services to be purchased, broken into line items wherever possible. Items" +
        " should not be duplicated, but a quantity of 2 specified instead.")
    @Valid
    private final Set<ItemDto> items;

    @JsonProperty("value")
    @Valid
    @NotNull
    private final ValueDto value;

    @JsonProperty("procurementMethod")
    @JsonPropertyDescription("Specify tendering method using the [method codelist](http://standard.open-contracting" +
        ".org/latest/en/schema/codelists/#method). This is a closed codelist. Local method types should be mapped to " +
        "this list.")
    @Valid
    @NotNull
    private final ProcurementMethod procurementMethod;

    @JsonProperty("procurementMethodDetails")
    @JsonPropertyDescription("Additional detail on the procurement method used. This field may be used to provide the" +
        " local name of the particular procurement method used.")
    @NotNull
    private final String procurementMethodDetails;

    @JsonProperty("mainProcurementCategory")
    @JsonPropertyDescription("The primary category describing the main object of this contracting process from the " +
        "[procurementCategory](http://standard.open-contracting.org/latest/en/schema/codelists/#procurement-category)" +
        " codelist. This is a closed codelist. Local classifications should be mapped to this list.")
    @Valid
    @NotNull
    private final MainProcurementCategory mainProcurementCategory;

    @JsonProperty("additionalProcurementCategories")
    @JsonPropertyDescription("Any additional categories which describe the objects of this contracting process, from " +
        "the [extendedProcurementCategory](http://standard.open-contracting" +
        ".org/latest/en/schema/codelists/#extended-procurement-category) codelist. This is an open codelist. Local " +
        "categories can be included in this list.")
    private final List<ExtendedProcurementCategory> additionalProcurementCategories;

    @JsonProperty("lots")
    @JsonPropertyDescription("A tender process may be divided into lots, where bidders can bid on one or more lots. " +
        "Details of each lot can be provided here. Items, documents and other features can then reference the lot " +
        "they are related to using relatedLot. Where no relatedLot identifier is given, the values should be " +
        "interpreted as applicable to the whole tender. Properties of tender can be overridden for a given LotDto " +
        "through their inclusion in the LotDto object.")
    @Valid
    private final List<LotDto> lots;

    @JsonProperty("classification")
    @Valid
    @NotNull
    private final ClassificationDto classification;

    @JsonProperty("legalBasis")
    @JsonPropertyDescription("The legal basis of the tender based on the [legalBasis codelist](http://standard" +
        ".open-contracting.org/......")
    @NotNull
    private final LegalBasis legalBasis;

    @JsonCreator
    public TenderDto(@JsonProperty("id") final String id,
                     @JsonProperty("title") final String title,
                     @JsonProperty("description") final String description,
                     @JsonProperty("status") final TenderStatusDto status,
                     @JsonProperty("items") final LinkedHashSet<ItemDto> items,
                     @JsonProperty("value") final ValueDto value,
                     @JsonProperty("procurementMethod") final ProcurementMethod procurementMethod,
                     @JsonProperty("procurementMethodDetails") final String procurementMethodDetails,
                     @JsonProperty("mainProcurementCategory") final MainProcurementCategory mainProcurementCategory,
                     @JsonProperty("additionalProcurementCategories") final List<ExtendedProcurementCategory>
                      additionalProcurementCategories,
                     @JsonProperty("lots") final List<LotDto> lots,
                     @JsonProperty("classification") final ClassificationDto classification,
                     @JsonProperty("legalBasis") final LegalBasis legalBasis) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.items = items;
        this.value = value;
        this.procurementMethod = procurementMethod;
        this.procurementMethodDetails = procurementMethodDetails;
        this.mainProcurementCategory = mainProcurementCategory;
        this.additionalProcurementCategories = additionalProcurementCategories;
        this.lots = lots;
        this.classification = classification;
        this.legalBasis = legalBasis;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(title)
                                    .append(description)
                                    .append(status)
                                    .append(items)
                                    .append(value)
                                    .append(procurementMethod)
                                    .append(procurementMethodDetails)
                                    .append(mainProcurementCategory)
                                    .append(additionalProcurementCategories)
                                    .append(lots)
                                    .append(classification)
                                    .append(legalBasis)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof TenderDto)) {
            return false;
        }
        final TenderDto rhs = (TenderDto) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(title, rhs.title)
                                  .append(description, rhs.description)
                                  .append(status, rhs.status)
                                  .append(items, rhs.items)
                                  .append(value, rhs.value)
                                  .append(procurementMethod, rhs.procurementMethod)
                                  .append(procurementMethodDetails, rhs.procurementMethodDetails)
                                  .append(mainProcurementCategory, rhs.mainProcurementCategory)
                                  .append(additionalProcurementCategories, rhs.additionalProcurementCategories)
                                  .append(lots, rhs.lots)
                                  .append(classification, rhs.classification)
                                  .append(legalBasis, rhs.legalBasis)
                                  .isEquals();
    }

    public enum MainProcurementCategory {
        GOODS("goods"),
        WORKS("works"),
        SERVICES("services");

        private final String value;
        private final static Map<String, MainProcurementCategory> CONSTANTS = new HashMap<>();

        static {
            for (final MainProcurementCategory c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private MainProcurementCategory(final String value) {
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
        public static MainProcurementCategory fromValue(final String value) {
            final MainProcurementCategory constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            }
            return constant;
        }
    }

    public enum ProcurementMethod {
        OPEN("open"),
        SELECTIVE("selective"),
        LIMITED("limited"),
        DIRECT("direct");

        private final String value;
        private final static Map<String, ProcurementMethod> CONSTANTS = new HashMap<>();

        static {
            for (final ProcurementMethod c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private ProcurementMethod(final String value) {
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
        public static ProcurementMethod fromValue(final String value) {
            final ProcurementMethod constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            }
            return constant;
        }
    }

    public enum ExtendedProcurementCategory {
        GOODS("goods"),
        WORKS("works"),
        SERVICES("services"),
        CONSULTING_SERVICES("consultingServices");

        private final String value;
        private final static Map<String, ExtendedProcurementCategory> CONSTANTS = new HashMap<>();

        static {
            for (final ExtendedProcurementCategory c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private ExtendedProcurementCategory(final String value) {
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
        public static ExtendedProcurementCategory fromValue(final String value) {
            final ExtendedProcurementCategory constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            }
            return constant;
        }
    }

    public enum LegalBasis {
        DIRECTIVE_2014_23_EU("DIRECTIVE_2014_23_EU"),
        DIRECTIVE_2014_24_EU("DIRECTIVE_2014_24_EU"),
        DIRECTIVE_2014_25_EU("DIRECTIVE_2014_25_EU"),
        DIRECTIVE_2009_81_EC("DIRECTIVE_2009_81_EC"),
        REGULATION_966_2012("REGULATION_966_2012"),
        NATIONAL_PROCUREMENT_LAW("NATIONAL_PROCUREMENT_LAW"),
        NULL("NULL");

        private final String value;
        private final static Map<String, LegalBasis> CONSTANTS = new HashMap<>();

        static {
            for (final LegalBasis c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private LegalBasis(final String value) {
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
        public static LegalBasis fromValue(final String value) {
            final LegalBasis constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            }
            return constant;
        }
    }
}

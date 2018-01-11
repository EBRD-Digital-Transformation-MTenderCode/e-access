package com.procurement.access.model.dto.pn;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.procurement.access.model.dto.enums.ExtendedProcurementCategoryTender;
import com.procurement.access.model.dto.enums.MainProcurementCategoryTender;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
public class PnTenderDto {
    @JsonProperty("id")
    @JsonPropertyDescription("An identifier for this tender process. This may be the same as the ocId, or may be " +
            "drawn from an internally held identifier for this tender.")
    @Size(min = 1)
    @NotNull
    private final String id;

    @JsonProperty("title")
    @JsonPropertyDescription("A title for this tender. This will often be used by applications as a headline to " +
            "attract interest, and to help analysts understand the nature of this procurement.")
    @NotNull
    private final String title;

    @JsonProperty("description")
    @JsonPropertyDescription("A summary description of the tender. This should complement structured information " +
            "provided using the items array. Descriptions should be short and easy to read. Avoid using ALL CAPS. ")
    @NotNull
    private final String description;

    @JsonProperty("status")
    @JsonPropertyDescription("The current status of the tender based on the [tenderStatus codelist](http://standard" +
            ".open-contracting.org/latest/en/schema/codelists/#tender-status)")
    @NotNull
    private final PnTenderStatusDto status;

    @JsonProperty("items")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("The goods and services to be purchased, broken into line items wherever possible. Items" +
            " should not be duplicated, but a quantity of 2 specified instead.")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    @Valid
    private final Set<PnItemDto> items;

    @JsonProperty("value")
    @Valid
    @NotNull
    private final PnValueDto value;

    @JsonProperty("procurementMethod")
    @JsonPropertyDescription("Specify tendering method using the [method codelist](http://standard.open-contracting" +
            ".org/latest/en/schema/codelists/#method). This is a closed codelist. Local method types should be mapped" +
            " to " +
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
            "[procurementCategory](http://standard.open-contracting" +
            ".org/latest/en/schema/codelists/#procurement-category)" +
            " codelist. This is a closed codelist. Local classifications should be mapped to this list.")
    @Valid
    @NotNull
    private final MainProcurementCategoryTender mainProcurementCategory;

    @JsonProperty("additionalProcurementCategories")
    @JsonPropertyDescription("Any additional categories which describe the objects of this contracting process, from " +
            "the [extendedProcurementCategory](http://standard.open-contracting" +
            ".org/latest/en/schema/codelists/#extended-procurement-category) codelist. This is an open codelist. " +
            "Local " +
            "categories can be included in this list.")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final List<ExtendedProcurementCategoryTender> additionalProcurementCategories;

    @JsonProperty("lots")
    @JsonPropertyDescription("A tender process may be divided into lots, where bidders can bid on one or more lots. " +
            "FsDetailsDto of each lot can be provided here. Items, documents and other features can then reference " +
            "the lot " +
            "they are related to using relatedLot. Where no relatedLot identifier is given, the values should be " +
            "interpreted as applicable to the whole tender. Properties of tender can be overridden for a given " +
            "CnLotDto " +
            "through their inclusion in the CnLotDto object.")
    @Valid
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final List<PnLotDto> lots;

    @JsonProperty("classification")
    @Valid
    @NotNull
    private final PnClassificationDto classification;

    @JsonProperty("legalBasis")
    @JsonPropertyDescription("The legal basis of the tender based on the [legalBasis codelist](http://standard" +
            ".open-contracting.org/......")
    @NotNull
    private final LegalBasis legalBasis;

    @JsonCreator
    public PnTenderDto(@JsonProperty("id") final String id,
                       @JsonProperty("title") final String title,
                       @JsonProperty("description") final String description,
                       @JsonProperty("status") final PnTenderStatusDto status,
                       @JsonProperty("items") final LinkedHashSet<PnItemDto> items,
                       @JsonProperty("value") final PnValueDto value,
                       @JsonProperty("procurementMethod") final ProcurementMethod procurementMethod,
                       @JsonProperty("procurementMethodDetails") final String procurementMethodDetails,
                       @JsonProperty("mainProcurementCategory") final MainProcurementCategoryTender
                               mainProcurementCategory,
                       @JsonProperty("additionalProcurementCategories") final List<ExtendedProcurementCategoryTender>
                               additionalProcurementCategories,
                       @JsonProperty("lots") final List<PnLotDto> lots,
                       @JsonProperty("classification") final PnClassificationDto classification,
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
        if (!(other instanceof PnTenderDto)) {
            return false;
        }
        final PnTenderDto rhs = (PnTenderDto) other;
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

    public enum ProcurementMethod {
        OPEN("open"),
        SELECTIVE("selective"),
        LIMITED("limited"),
        DIRECT("direct");

        private static final Map<String, ProcurementMethod> CONSTANTS = new HashMap<>();

        static {
            for (final ProcurementMethod c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        ProcurementMethod(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static ProcurementMethod fromValue(final String value) {
            final ProcurementMethod constant = CONSTANTS.get(value);
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

    public enum LegalBasis {
        DIRECTIVE_2014_23_EU("DIRECTIVE_2014_23_EU"),
        DIRECTIVE_2014_24_EU("DIRECTIVE_2014_24_EU"),
        DIRECTIVE_2014_25_EU("DIRECTIVE_2014_25_EU"),
        DIRECTIVE_2009_81_EC("DIRECTIVE_2009_81_EC"),
        REGULATION_966_2012("REGULATION_966_2012"),
        NATIONAL_PROCUREMENT_LAW("NATIONAL_PROCUREMENT_LAW"),
        NULL("NULL");

        private static final Map<String, LegalBasis> CONSTANTS = new HashMap<>();

        static {
            for (final LegalBasis c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        LegalBasis(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static LegalBasis fromValue(final String value) {
            final LegalBasis constant = CONSTANTS.get(value);
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

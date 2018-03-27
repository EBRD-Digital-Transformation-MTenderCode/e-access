package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.stereotype.Service;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "documentType",
        "title",
        "description",
        "url",
        "datePublished",
        "dateModified",
        "format",
        "language",
        "relatedLots"
})
public class Document {
    @JsonProperty("id")
    @NotNull
    private final String id;

    @JsonProperty("documentType")
    private final DocumentType documentType;

    @JsonProperty("title")
    private final String title;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("url")
    private final String url;

    @JsonProperty("datePublished")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime datePublished;

    @JsonProperty("dateModified")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime dateModified;

    @JsonProperty("format")
    private final String format;

    @JsonProperty("language")
    private final String language;

    @JsonProperty("relatedLots")
    private List<String> relatedLots;

    @JsonCreator
    public Document(@JsonProperty("id") final String id,
                    @JsonProperty("documentType") final DocumentType documentType,
                    @JsonProperty("title") final String title,
                    @JsonProperty("description") final String description,
                    @JsonProperty("url") final String url,
                    @JsonProperty("datePublished") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final
                    LocalDateTime datePublished,
                    @JsonProperty("dateModified") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final
                    LocalDateTime dateModified,
                    @JsonProperty("format") final String format,
                    @JsonProperty("language") final String language,
                    @JsonProperty("relatedLots") final List<String> relatedLots) {
        this.id = id;
        this.documentType = documentType;
        this.title = title;
        this.description = description;
        this.url = url;
        this.datePublished = datePublished;
        this.dateModified = dateModified;
        this.format = format;
        this.language = language;
        this.relatedLots = relatedLots;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                .append(documentType)
                .append(title)
                .append(description)
                .append(url)
                .append(datePublished)
                .append(dateModified)
                .append(format)
                .append(language)
                .append(relatedLots)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Document)) {
            return false;
        }
        final Document rhs = (Document) other;
        return new EqualsBuilder().append(id, rhs.id)
                .append(documentType, rhs.documentType)
                .append(title, rhs.title)
                .append(description, rhs.description)
                .append(url, rhs.url)
                .append(datePublished, rhs.datePublished)
                .append(dateModified, rhs.dateModified)
                .append(format, rhs.format)
                .append(language, rhs.language)
                .append(relatedLots, rhs.relatedLots)
                .isEquals();
    }

    public enum DocumentType {
        TENDER_NOTICE("tenderNotice"),
        AWARD_NOTICE("awardNotice"),
        CONTRACT_NOTICE("contractNotice"),
        COMPLETION_CERTIFICATE("completionCertificate"),
        PROCUREMENT_PLAN("procurementPlan"),
        BIDDING_DOCUMENTS("biddingDocuments"),
        TECHNICAL_SPECIFICATIONS("technicalSpecifications"),
        EVALUATION_CRITERIA("evaluationCriteria"),
        EVALUATION_REPORTS("evaluationReports"),
        CONTRACT_DRAFT("contractDraft"),
        CONTRACT_SIGNED("contractSigned"),
        CONTRACT_ARRANGEMENTS("contractArrangements"),
        CONTRACT_SCHEDULE("contractSchedule"),
        PHYSICAL_PROGRESS_REPORT("physicalProgressReport"),
        FINANCIAL_PROGRESS_REPORT("financialProgressReport"),
        FINAL_AUDIT("finalAudit"),
        HEARING_NOTICE("hearingNotice"),
        MARKET_STUDIES("marketStudies"),
        ELIGIBILITY_CRITERIA("eligibilityCriteria"),
        CLARIFICATIONS("clarifications"),
        SHORTLISTED_FIRMS("shortlistedFirms"),
        ENVIRONMENTAL_IMPACT("environmentalImpact"),
        ASSET_AND_LIABILITY_ASSESSMENT("assetAndLiabilityAssessment"),
        RISK_PROVISIONS("riskProvisions"),
        WINNING_BID("winningBid"),
        COMPLAINTS("complaints"),
        CONTRACT_ANNEXE("contractAnnexe"),
        CONTRACT_GUARANTEES("contractGuarantees"),
        SUB_CONTRACT("subContract"),
        NEEDS_ASSESSMENT("needsAssessment"),
        FEASIBILITY_STUDY("feasibilityStudy"),
        PROJECT_PLAN("projectPlan"),
        BILL_OF_QUANTITY("billOfQuantity"),
        BIDDERS("bidders"),
        CONFLICT_OF_INTEREST("conflictOfInterest"),
        DEBARMENTS("debarments"),
        ILLUSTRATION("illustration"),
        SUBMISSION_DOCUMENTS("submissionDocuments"),
        CONTRACT_SUMMARY("contractSummary"),
        CANCELLATION_DETAILS("cancellationDetails");

        private final static Map<String, DocumentType> CONSTANTS = new HashMap<>();

        static {
            for (final DocumentType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        DocumentType(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static DocumentType fromValue(final String value) {
            final DocumentType constant = CONSTANTS.get(value);
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

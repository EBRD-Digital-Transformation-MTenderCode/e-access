package com.procurement.access.model.dto.pnToPin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "documentType",
    "id",
    "datePublished",
    "url",
    "title",
    "description", 
    "language",
    "relatedLots"
})
public class PnToPinDocument {
    @NotNull
    @JsonProperty("documentType")
    private final DocumentType documentType;
    
    @JsonProperty("id")   
    @NotNull
    private final String id;

    @JsonProperty("datePublished")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime datePublished;

    @JsonProperty("url")
    private final URI url;

    @JsonProperty("title")
    private final String title;

    @JsonProperty("description")
    private final String description;

    @NotNull
    @JsonProperty("language")
    private final String language;

    @JsonProperty("relatedLots")
    private final Set<String> relatedLots;

    @JsonCreator
    public PnToPinDocument(@JsonProperty("id") final String id,
                           @JsonProperty("documentType") final DocumentType documentType,
                           @JsonProperty("title") final String title,
                           @JsonProperty("description") final String description,
                           @JsonProperty("url") final URI url,
                           @JsonProperty("datePublished") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final
                        LocalDateTime datePublished,
                           @JsonProperty("language") final String language,
                           @JsonProperty("relatedLots") final Set<String> relatedLots) {
        this.id = id;
        this.documentType = documentType;
        this.title = title;
        this.description = description;
        this.url = url;
        this.datePublished = datePublished;
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
                                    .append(language)
                                    .append(relatedLots)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PnToPinDocument)) {
            return false;
        }
        final PnToPinDocument rhs = (PnToPinDocument) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(documentType, rhs.documentType)
                                  .append(title, rhs.title)
                                  .append(description, rhs.description)
                                  .append(url, rhs.url)
                                  .append(datePublished, rhs.datePublished)
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

        private final String value;
        private final static Map<String, DocumentType> CONSTANTS = new HashMap<>();

        static {
            for (final DocumentType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private DocumentType(final String value) {
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
        public static DocumentType fromValue(final String value) {
            final DocumentType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            }
            return constant;
        }
    }
}

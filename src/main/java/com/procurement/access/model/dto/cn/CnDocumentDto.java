package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "id",
    "documentType"
})
public class CnDocumentDto {
    @JsonProperty("id")
    @JsonPropertyDescription("A local, unique identifier for this document. This field is used to keep track of " +
        "multiple revisions of a document through the compilation from release to record mechanism.")
    @Size(min = 1)
    @NotNull
    private final String id;

    @JsonProperty("documentType")
    @JsonPropertyDescription("A classification of the document described taken from the [documentType codelist]" +
        "(http://standard.open-contracting.org/latest/en/schema/codelists/#document-type). Values from the provided " +
        "codelist should be used wherever possible, though extended values can be provided if the codelist does not " +
        "have a relevant code.")
    private final DocumentType documentType;

    @JsonCreator
    public CnDocumentDto(@JsonProperty("id") final String id,
                         @JsonProperty("documentType") final DocumentType documentType) {
        this.id = id;
        this.documentType = documentType;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(documentType)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof CnDocumentDto)) {
            return false;
        }
        final CnDocumentDto rhs = (CnDocumentDto) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(documentType, rhs.documentType)
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

        private static final Map<String, DocumentType> CONSTANTS = new HashMap<>();

        private final String value;

        static {
            for (final DocumentType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        DocumentType(final String value) {
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

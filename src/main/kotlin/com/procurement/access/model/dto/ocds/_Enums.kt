package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException
import java.util.*


enum class ProcurementMethodModalities constructor(private val value: String) {
    ELECTRONIC_AUCTION("electronicAuction");

    @JsonValue
    fun value(): String {
        return this.value
    }

    override fun toString(): String {
        return this.value
    }

    companion object {

        private val CONSTANTS = HashMap<String, ProcurementMethodModalities>()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): ProcurementMethodModalities {
            return CONSTANTS[value]
                    ?: throw EnumException(ProcurementMethodModalities::class.java.name, value, Arrays.toString(values()))
        }
    }
}

enum class AwardCriteria constructor(private val value: String) {
    PRICE_ONLY("priceOnly"),
    COST_ONLY("costOnly"),
    QUALITY_ONLY("qualityOnly"),
    RATED_CRITERIA("ratedCriteria"),
    LOWEST_COST("lowestCost"),
    BEST_PROPOSAL("bestProposal"),
    BEST_VALUE_TO_GOVERNMENT("bestValueToGovernment"),
    SINGLE_BID_ONLY("singleBidOnly");

    @JsonValue
    fun value(): String {
        return this.value
    }

    override fun toString(): String {
        return this.value
    }

    companion object {

        private val CONSTANTS = HashMap<String, AwardCriteria>()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): AwardCriteria {
            return CONSTANTS[value]
                    ?: throw EnumException(AwardCriteria::class.java.name, value, Arrays.toString(values()))
        }
    }
}

enum class ExtendedProcurementCategory constructor(private val value: String) {
    GOODS("goods"),
    WORKS("works"),
    SERVICES("services"),
    CONSULTING_SERVICES("consultingServices");

    @JsonValue
    fun value(): String {
        return this.value
    }

    override fun toString(): String {
        return this.value
    }

    companion object {

        private val CONSTANTS = HashMap<String, ExtendedProcurementCategory>()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): ExtendedProcurementCategory {
            return CONSTANTS[value]
                    ?: throw EnumException(ExtendedProcurementCategory::class.java.name, value, Arrays.toString(values()))
        }
    }
}

enum class LegalBasis constructor(private val value: String) {
    DIRECTIVE_2014_23_EU("DIRECTIVE_2014_23_EU"),
    DIRECTIVE_2014_24_EU("DIRECTIVE_2014_24_EU"),
    DIRECTIVE_2014_25_EU("DIRECTIVE_2014_25_EU"),
    DIRECTIVE_2009_81_EC("DIRECTIVE_2009_81_EC"),
    REGULATION_966_2012("REGULATION_966_2012"),
    NATIONAL_PROCUREMENT_LAW("NATIONAL_PROCUREMENT_LAW"),
    NULL("NULL");

    @JsonValue
    fun value(): String {
        return this.value
    }

    override fun toString(): String {
        return this.value
    }

    companion object {

        private val CONSTANTS = HashMap<String, LegalBasis>()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): LegalBasis {
            return CONSTANTS[value]
                    ?: throw EnumException(LegalBasis::class.java.name, value, Arrays.toString(values()))
        }
    }
}

enum class MainProcurementCategory constructor(private val value: String) {
    GOODS("goods"),
    WORKS("works"),
    SERVICES("services");

    override fun toString(): String {
        return this.value
    }

    @JsonValue
    fun value(): String {
        return this.value
    }

    companion object {

        private val CONSTANTS = HashMap<String, MainProcurementCategory>()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): MainProcurementCategory {
            return CONSTANTS[value]
                    ?: throw EnumException(MainProcurementCategory::class.java.name, value, Arrays.toString(values()))
        }
    }
}

enum class ProcurementMethod constructor(private val value: String) {

    MV("open"),
    OT("open"),
    RT("selective"),
    SV("open"),
    DA("direct"),
    NP("selective"),
    FA("limited"),
    TEST_OT("open"),
    TEST_SV("open"),
    TEST_RT("selective");

    @JsonValue
    fun value(): String {
        return this.value
    }

    override fun toString(): String {
        return this.value
    }

    companion object {

        private val CONSTANTS = HashMap<String, ProcurementMethod>()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): ProcurementMethod {
            return CONSTANTS[value]
                    ?: throw EnumException(ProcurementMethod::class.java.name, value, Arrays.toString(values()))
        }
    }
}

enum class Scheme constructor(private val value: String) {
    CPV("CPV"),
    CPVS("CPVS"),
    GSIN("GSIN"),
    UNSPSC("UNSPSC"),
    CPC("CPC"),
    OKDP("OKDP"),
    OKPD("OKPD");

    override fun toString(): String {
        return this.value
    }

    @JsonValue
    fun value(): String {
        return this.value
    }

    companion object {

        private val CONSTANTS = HashMap<String, Scheme>()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): Scheme {
            return CONSTANTS[value] ?: throw EnumException(Scheme::class.java.name, value, Arrays.toString(values()))
        }
    }
}

enum class SubmissionLanguage constructor(private val value: String) {
    BG("bg"),
    ES("es"),
    CS("cs"),
    DA("da"),
    DE("de"),
    ET("et"),
    EL("el"),
    EN("en"),
    FR("fr"),
    GA("ga"),
    HR("hr"),
    IT("it"),
    LV("lv"),
    LT("lt"),
    HU("hu"),
    MT("mt"),
    NL("nl"),
    PL("pl"),
    PT("pt"),
    RO("ro"),
    SK("sk"),
    SL("sl"),
    FI("fi"),
    SV("sv");

    @JsonValue
    fun value(): String {
        return this.value
    }

    override fun toString(): String {
        return this.value
    }

    companion object {

        private val CONSTANTS = HashMap<String, SubmissionLanguage>()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): SubmissionLanguage {
            return CONSTANTS[value]
                    ?: throw EnumException(SubmissionLanguage::class.java.name, value, Arrays.toString(values()))
        }
    }
}

enum class SubmissionMethod constructor(private val value: String) {
    ELECTRONIC_SUBMISSION("electronicSubmission"),
    ELECTRONIC_AUCTION("electronicAuction"),
    WRITTEN("written"),
    IN_PERSON("inPerson");

    @JsonValue
    fun value(): String {
        return this.value
    }

    override fun toString(): String {
        return this.value
    }

    companion object {

        private val CONSTANTS = HashMap<String, SubmissionMethod>()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): SubmissionMethod {
            return CONSTANTS[value]
                    ?: throw EnumException(SubmissionMethod::class.java.name, value, Arrays.toString(values()))
        }
    }
}

enum class SubmissionMethodRationale constructor(private val value: String) {
    TOOLS_DEVICES_FILE_FORMATS_UNAVAILABLE("TOOLS_DEVICES_FILE_FORMATS_UNAVAILABLE"),
    IPR_ISSUES("IPR_ISSUES"),
    REQUIRES_SPECIALISED_EQUIPMENT("REQUIRES_SPECIALISED_EQUIPMENT"),
    PHYSICAL_MODEL("PHYSICAL_MODEL"),
    SENSITIVE_INFORMATION("SENSITIVE_INFORMATION");

    @JsonValue
    fun value(): String {
        return this.value
    }

    override fun toString(): String {
        return this.value
    }

    companion object {

        private val CONSTANTS = HashMap<String, SubmissionMethodRationale>()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): SubmissionMethodRationale {
            return CONSTANTS[value]
                    ?: throw EnumException(SubmissionMethodRationale::class.java.name, value, Arrays.toString(values()))
        }
    }
}

enum class TenderStatus constructor(private val value: String) {
    PLANNING("planning"),
    PLANNED("planned"),
    ACTIVE("active"),
    CANCELLED("cancelled"),
    UNSUCCESSFUL("unsuccessful"),
    COMPLETE("complete");

    override fun toString(): String {
        return this.value
    }

    @JsonValue
    fun value(): String {
        return this.value
    }

    companion object {

        private val CONSTANTS = HashMap<String, TenderStatus>()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): TenderStatus {
            return CONSTANTS[value]
                    ?: throw EnumException(TenderStatus::class.java.name, value, Arrays.toString(values()))
        }
    }
}

enum class TenderStatusDetails constructor(private val value: String) {
    //    UNSUCCESSFUL("unsuccessful"),
//    AWARDED("awarded"),
//    //**//
    PLANNING("planning"),
    PLANNED("planned"),
    CLARIFICATION("clarification"),
    TENDERING("tendering"),
    CANCELLATION("cancellation"),
    SUSPENDED("suspended"),
    AWARDING("awarding"),
    AWARDED_STANDSTILL("awardedStandStill"),
    AWARDED_SUSPENDED("awardedSuspended"),
    AWARDED_CONTRACT_PREPARATION("awardedContractPreparation"),
    COMPLETE("complete"),
    EMPTY("empty");

    override fun toString(): String {
        return this.value
    }

    @JsonValue
    fun value(): String {
        return this.value
    }

    companion object {

        private val CONSTANTS = HashMap<String, TenderStatusDetails>()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): TenderStatusDetails {
            return CONSTANTS[value]
                    ?: throw EnumException(TenderStatusDetails::class.java.name, value, Arrays.toString(values()))
        }
    }
}

enum class LotStatus constructor(private val value: String) {
    PLANNING("planning"),
    PLANNED("planned"),
    ACTIVE("active"),
    CANCELLED("cancelled"),
    UNSUCCESSFUL("unsuccessful"),
    COMPLETE("complete");

    override fun toString(): String {
        return this.value
    }

    @JsonValue
    fun value(): String {
        return this.value
    }

    companion object {

        private val CONSTANTS = HashMap<String, LotStatus>()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): LotStatus {
            return CONSTANTS[value]
                    ?: throw EnumException(LotStatus::class.java.name, value, Arrays.toString(values()))
        }
    }
}

enum class LotStatusDetails constructor(private val value: String) {
    UNSUCCESSFUL("unsuccessful"),
    AWARDED("awarded"),
    CANCELLED("cancelled"),
    EMPTY("empty");

    override fun toString(): String {
        return this.value
    }

    @JsonValue
    fun value(): String {
        return this.value
    }

    companion object {

        private val CONSTANTS = HashMap<String, LotStatusDetails>()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): LotStatusDetails {
            return CONSTANTS[value]
                    ?: throw EnumException(LotStatusDetails::class.java.name, value, Arrays.toString(values()))
        }
    }
}


enum class DocumentType constructor(private val value: String) {

    EVALUATION_CRITERIA("evaluationCriteria"),
    ELIGIBILITY_CRITERIA("eligibilityCriteria"),
    BILL_OF_QUANTITY("billOfQuantity"),
    ILLUSTRATION("illustration"),
    MARKET_STUDIES("marketStudies"),
    TENDER_NOTICE("tenderNotice"),
    BIDDING_DOCUMENTS("biddingDocuments"),
    PROCUREMENT_PLAN("procurementPlan"),
    TECHNICAL_SPECIFICATIONS("technicalSpecifications"),
    CONTRACT_DRAFT("contractDraft"),
    HEARING_NOTICE("hearingNotice"),
    CLARIFICATIONS("clarifications"),
    ENVIRONMENTAL_IMPACT("environmentalImpact"),
    ASSET_AND_LIABILITY_ASSESSMENT("assetAndLiabilityAssessment"),
    RISK_PROVISIONS("riskProvisions"),
    COMPLAINTS("complaints"),
    NEEDS_ASSESSMENT("needsAssessment"),
    FEASIBILITY_STUDY("feasibilityStudy"),
    PROJECT_PLAN("projectPlan"),
    CONFLICT_OF_INTEREST("conflictOfInterest"),
    CANCELLATION_DETAILS("cancellationDetails"),
    SHORTLISTED_FIRMS("shortlistedFirms"),
    EVALUATION_REPORTS("evaluationReports"),
    CONTRACT_ARRANGEMENTS("contractArrangements"),
    CONTRACT_GUARANTEES("contractGuarantees");


    override fun toString(): String {
        return this.value
    }

    @JsonValue
    fun value(): String {
        return this.value
    }

    companion object {

        private val CONSTANTS = HashMap<String, DocumentType>()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): DocumentType {
            return CONSTANTS[value]
                    ?: throw EnumException(DocumentType::class.java.name, value, Arrays.toString(values()))
        }
    }
}

enum class Operation(val value: String) {
    CREATE_CN("createCN"),
    CREATE_PN("createPN"),
    CREATE_PIN("createPIN"),
    UPDATE_CN("updateCN"),
    UPDATE_PN("updatePN"),
    CREATE_CN_ON_PN("createCNonPN"),
    CREATE_CN_ON_PIN("createCNonPIN"),
    CREATE_PIN_ON_PN("createPINonPN");

    companion object {
        private val CONSTANTS = HashMap<String, Operation>()

        init {
            for (c in Operation.values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): Operation {
            return CONSTANTS[value] ?: throw IllegalArgumentException(value)
        }
    }
}
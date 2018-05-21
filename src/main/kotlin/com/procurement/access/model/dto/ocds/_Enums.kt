package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.notice.exception.EnumException
import java.util.*

enum class Currency constructor(private val value: String) {

    ADP("ADP"),
    AED("AED"),
    AFA("AFA"),
    AFN("AFN"),
    ALK("ALK"),
    ALL("ALL"),
    AMD("AMD"),
    ANG("ANG"),
    AOA("AOA"),
    AOK("AOK"),
    AON("AON"),
    AOR("AOR"),
    ARA("ARA"),
    ARP("ARP"),
    ARS("ARS"),
    ARY("ARY"),
    ATS("ATS"),
    AUD("AUD"),
    AWG("AWG"),
    AYM("AYM"),
    AZM("AZM"),
    AZN("AZN"),
    BAD("BAD"),
    BAM("BAM"),
    BBD("BBD"),
    BDT("BDT"),
    BEC("BEC"),
    BEF("BEF"),
    BEL("BEL"),
    BGJ("BGJ"),
    BGK("BGK"),
    BGL("BGL"),
    BGN("BGN"),
    BHD("BHD"),
    BIF("BIF"),
    BMD("BMD"),
    BND("BND"),
    BOB("BOB"),
    BOP("BOP"),
    BOV("BOV"),
    BRB("BRB"),
    BRC("BRC"),
    BRE("BRE"),
    BRL("BRL"),
    BRN("BRN"),
    BRR("BRR"),
    BSD("BSD"),
    BTN("BTN"),
    BUK("BUK"),
    BWP("BWP"),
    BYB("BYB"),
    BYN("BYN"),
    BYR("BYR"),
    BZD("BZD"),
    CAD("CAD"),
    CDF("CDF"),
    CHC("CHC"),
    CHE("CHE"),
    CHF("CHF"),
    CHW("CHW"),
    CLF("CLF"),
    CLP("CLP"),
    CNY("CNY"),
    COP("COP"),
    COU("COU"),
    CRC("CRC"),
    CSD("CSD"),
    CSJ("CSJ"),
    CSK("CSK"),
    CUC("CUC"),
    CUP("CUP"),
    CVE("CVE"),
    CYP("CYP"),
    CZK("CZK"),
    DDM("DDM"),
    DEM("DEM"),
    DJF("DJF"),
    DKK("DKK"),
    DOP("DOP"),
    DZD("DZD"),
    ECS("ECS"),
    ECV("ECV"),
    EEK("EEK"),
    EGP("EGP"),
    ERN("ERN"),
    ESA("ESA"),
    ESB("ESB"),
    ESP("ESP"),
    ETB("ETB"),
    EUR("EUR"),
    FIM("FIM"),
    FJD("FJD"),
    FKP("FKP"),
    FRF("FRF"),
    GBP("GBP"),
    GEK("GEK"),
    GEL("GEL"),
    GHC("GHC"),
    GHP("GHP"),
    GHS("GHS"),
    GIP("GIP"),
    GMD("GMD"),
    GNE("GNE"),
    GNF("GNF"),
    GNS("GNS"),
    GQE("GQE"),
    GRD("GRD"),
    GTQ("GTQ"),
    GWE("GWE"),
    GWP("GWP"),
    GYD("GYD"),
    HKD("HKD"),
    HNL("HNL"),
    HRD("HRD"),
    HRK("HRK"),
    HTG("HTG"),
    HUF("HUF"),
    IDR("IDR"),
    IEP("IEP"),
    ILP("ILP"),
    ILR("ILR"),
    ILS("ILS"),
    INR("INR"),
    IQD("IQD"),
    IRR("IRR"),
    ISJ("ISJ"),
    ISK("ISK"),
    ITL("ITL"),
    JMD("JMD"),
    JOD("JOD"),
    JPY("JPY"),
    KES("KES"),
    KGS("KGS"),
    KHR("KHR"),
    KMF("KMF"),
    KPW("KPW"),
    KRW("KRW"),
    KWD("KWD"),
    KYD("KYD"),
    KZT("KZT"),
    LAJ("LAJ"),
    LAK("LAK"),
    LBP("LBP"),
    LKR("LKR"),
    LRD("LRD"),
    LSL("LSL"),
    LSM("LSM"),
    LTL("LTL"),
    LTT("LTT"),
    LUC("LUC"),
    LUF("LUF"),
    LUL("LUL"),
    LVL("LVL"),
    LVR("LVR"),
    LYD("LYD"),
    MAD("MAD"),
    MDL("MDL"),
    MGA("MGA"),
    MGF("MGF"),
    MKD("MKD"),
    MLF("MLF"),
    MMK("MMK"),
    MNT("MNT"),
    MOP("MOP"),
    MRO("MRO"),
    MTL("MTL"),
    MTP("MTP"),
    MUR("MUR"),
    MVQ("MVQ"),
    MVR("MVR"),
    MWK("MWK"),
    MXN("MXN"),
    MXP("MXP"),
    MXV("MXV"),
    MYR("MYR"),
    MZE("MZE"),
    MZM("MZM"),
    MZN("MZN"),
    NAD("NAD"),
    NGN("NGN"),
    NIC("NIC"),
    NIO("NIO"),
    NLG("NLG"),
    NOK("NOK"),
    NPR("NPR"),
    NZD("NZD"),
    OMR("OMR"),
    PAB("PAB"),
    PEH("PEH"),
    PEI("PEI"),
    PEN("PEN"),
    PES("PES"),
    PGK("PGK"),
    PHP("PHP"),
    PKR("PKR"),
    PLN("PLN"),
    PLZ("PLZ"),
    PTE("PTE"),
    PYG("PYG"),
    QAR("QAR"),
    RHD("RHD"),
    ROK("ROK"),
    ROL("ROL"),
    RON("RON"),
    RSD("RSD"),
    RUB("RUB"),
    RUR("RUR"),
    RWF("RWF"),
    SAR("SAR"),
    SBD("SBD"),
    SCR("SCR"),
    SDD("SDD"),
    SDG("SDG"),
    SDP("SDP"),
    SEK("SEK"),
    SGD("SGD"),
    SHP("SHP"),
    SIT("SIT"),
    SKK("SKK"),
    SLL("SLL"),
    SOS("SOS"),
    SRD("SRD"),
    SRG("SRG"),
    SSP("SSP"),
    STD("STD"),
    SUR("SUR"),
    SVC("SVC"),
    SYP("SYP"),
    SZL("SZL"),
    THB("THB"),
    TJR("TJR"),
    TJS("TJS"),
    TMM("TMM"),
    TMT("TMT"),
    TND("TND"),
    TOP("TOP"),
    TPE("TPE"),
    TRL("TRL"),
    TRY("TRY"),
    TTD("TTD"),
    TWD("TWD"),
    TZS("TZS"),
    UAH("UAH"),
    UAK("UAK"),
    UGS("UGS"),
    UGW("UGW"),
    UGX("UGX"),
    USD("USD"),
    USN("USN"),
    USS("USS"),
    UYI("UYI"),
    UYN("UYN"),
    UYP("UYP"),
    UYU("UYU"),
    UZS("UZS"),
    VEB("VEB"),
    VEF("VEF"),
    VNC("VNC"),
    VND("VND"),
    VUV("VUV"),
    WST("WST"),
    XAF("XAF"),
    XAG("XAG"),
    XAU("XAU"),
    XBA("XBA"),
    XBB("XBB"),
    XBC("XBC"),
    XBD("XBD"),
    XCD("XCD"),
    XDR("XDR"),
    XEU("XEU"),
    XFO("XFO"),
    XFU("XFU"),
    XOF("XOF"),
    XPD("XPD"),
    XPF("XPF"),
    XPT("XPT"),
    XRE("XRE"),
    XSU("XSU"),
    XTS("XTS"),
    XUA("XUA"),
    XXX("XXX"),
    YDD("YDD"),
    YER("YER"),
    YUD("YUD"),
    YUM("YUM"),
    YUN("YUN"),
    ZAL("ZAL"),
    ZAR("ZAR"),
    ZMK("ZMK"),
    ZMW("ZMW"),
    ZRN("ZRN"),
    ZRZ("ZRZ"),
    ZWC("ZWC"),
    ZWD("ZWD"),
    ZWL("ZWL"),
    ZWN("ZWN"),
    ZWR("ZWR");

    override fun toString(): String {
        return this.value
    }

    @JsonValue
    fun value(): String {
        return this.value
    }

    companion object {
        private val CONSTANTS = HashMap<String, Currency>()

        init {
            for (c in values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): Currency {
            return Currency.CONSTANTS[value]
                    ?: throw EnumException(Currency::class.java.name, value, Arrays.toString(values()))
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
    OPEN("open"),
    SELECTIVE("selective"),
    LIMITED("limited"),
    DIRECT("direct");

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
    COMPLETE("complete"),
    WITHDRAWN("withdrawn");

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
    PRESELECTION("preselection"),
    PRESELECTED("preselected"),
    PREQUALIFICATION("prequalification"),
    PREQUALIFIED("prequalified"),
    EVALUATION("evaluation"),
    EVALUATED("evaluated"),
    EXECUTION("execution"),
    AWARDED("awarded"),
    //**//
    PLANNING("planning"),
    PLANNED("planned"),
    ACTIVE("active"),
    BLOCKED("blocked"),
    CANCELLED("cancelled"),
    UNSUCCESSFUL("unsuccessful"),
    COMPLETE("complete"),
    WITHDRAWN("withdrawn"),
    SUSPENDED("suspended"),
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

enum class DocumentType constructor(private val value: String) {
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
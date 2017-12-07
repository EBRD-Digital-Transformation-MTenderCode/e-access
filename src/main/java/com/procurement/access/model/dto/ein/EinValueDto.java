package com.procurement.access.model.dto.ein;

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
    "amount",
    "currency"
})
public class EinValueDto {
    @JsonProperty("amount")
    @JsonPropertyDescription("Amount as a number.")
    @NotNull
    private final Double amount;

    @JsonProperty("currency")
    @JsonPropertyDescription("The currency for each amount should always be specified using the uppercase 3-letter " +
        "currency code from ISO4217.")
    @NotNull
    private final Currency currency;

    @JsonCreator
    public EinValueDto(@JsonProperty("amount") final Double amount,
                       @JsonProperty("currency") final Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(amount)
                                    .append(currency)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof EinValueDto)) {
            return false;
        }
        final EinValueDto rhs = (EinValueDto) other;
        return new EqualsBuilder().append(amount, rhs.amount)
                                  .append(currency, rhs.currency)
                                  .isEquals();
    }

    public enum Currency {
        AED("AED"),
        AFN("AFN"),
        ALL("ALL"),
        AMD("AMD"),
        ANG("ANG"),
        AOA("AOA"),
        ARS("ARS"),
        AUD("AUD"),
        AWG("AWG"),
        AZN("AZN"),
        BAM("BAM"),
        BBD("BBD"),
        BDT("BDT"),
        BGN("BGN"),
        BHD("BHD"),
        BIF("BIF"),
        BMD("BMD"),
        BND("BND"),
        BOB("BOB"),
        BOV("BOV"),
        BRL("BRL"),
        BSD("BSD"),
        BTN("BTN"),
        BWP("BWP"),
        BYR("BYR"),
        BZD("BZD"),
        CAD("CAD"),
        CDF("CDF"),
        CHF("CHF"),
        CLF("CLF"),
        CLP("CLP"),
        CNY("CNY"),
        COP("COP"),
        COU("COU"),
        CRC("CRC"),
        CUC("CUC"),
        CUP("CUP"),
        CVE("CVE"),
        CZK("CZK"),
        DJF("DJF"),
        DKK("DKK"),
        DOP("DOP"),
        DZD("DZD"),
        EEK("EEK"),
        EGP("EGP"),
        ERN("ERN"),
        ETB("ETB"),
        EUR("EUR"),
        FJD("FJD"),
        FKP("FKP"),
        GBP("GBP"),
        GEL("GEL"),
        GHS("GHS"),
        GIP("GIP"),
        GMD("GMD"),
        GNF("GNF"),
        GTQ("GTQ"),
        GYD("GYD"),
        HKD("HKD"),
        HNL("HNL"),
        HRK("HRK"),
        HTG("HTG"),
        HUF("HUF"),
        IDR("IDR"),
        ILS("ILS"),
        INR("INR"),
        IQD("IQD"),
        IRR("IRR"),
        ISK("ISK"),
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
        LAK("LAK"),
        LBP("LBP"),
        LKR("LKR"),
        LRD("LRD"),
        LSL("LSL"),
        LTL("LTL"),
        LVL("LVL"),
        LYD("LYD"),
        MAD("MAD"),
        MDL("MDL"),
        MGA("MGA"),
        MKD("MKD"),
        MMK("MMK"),
        MNT("MNT"),
        MOP("MOP"),
        MRO("MRO"),
        MUR("MUR"),
        MVR("MVR"),
        MWK("MWK"),
        MXN("MXN"),
        MXV("MXV"),
        MYR("MYR"),
        MZN("MZN"),
        NAD("NAD"),
        NGN("NGN"),
        NIO("NIO"),
        NOK("NOK"),
        NPR("NPR"),
        NZD("NZD"),
        OMR("OMR"),
        PAB("PAB"),
        PEN("PEN"),
        PGK("PGK"),
        PHP("PHP"),
        PKR("PKR"),
        PLN("PLN"),
        PYG("PYG"),
        QAR("QAR"),
        RON("RON"),
        RSD("RSD"),
        RUB("RUB"),
        RWF("RWF"),
        SAR("SAR"),
        SBD("SBD"),
        SCR("SCR"),
        SDG("SDG"),
        SEK("SEK"),
        SGD("SGD"),
        SHP("SHP"),
        SLL("SLL"),
        SOS("SOS"),
        SSP("SSP"),
        SRD("SRD"),
        STD("STD"),
        SVC("SVC"),
        SYP("SYP"),
        SZL("SZL"),
        THB("THB"),
        TJS("TJS"),
        TMT("TMT"),
        TND("TND"),
        TOP("TOP"),
        TRY("TRY"),
        TTD("TTD"),
        TWD("TWD"),
        TZS("TZS"),
        UAH("UAH"),
        UGX("UGX"),
        USD("USD"),
        USN("USN"),
        USS("USS"),
        UYI("UYI"),
        UYU("UYU"),
        UZS("UZS"),
        VEF("VEF"),
        VND("VND"),
        VUV("VUV"),
        WST("WST"),
        XAF("XAF"),
        XBT("XBT"),
        XCD("XCD"),
        XDR("XDR"),
        XOF("XOF"),
        XPF("XPF"),
        YER("YER"),
        ZAR("ZAR"),
        ZMK("ZMK"),
        ZWL("ZWL");

        static final Map<String, Currency> CONSTANTS = new HashMap<>();

        static {
            for (final Currency c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        Currency(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static Currency fromValue(final String value) {
            final Currency constant = CONSTANTS.get(value);
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


package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "measure",
    "date",
    "value",
    "notes",
    "relatedLot"
})
public class BidsStatistic {
    @JsonProperty("id")
    @JsonPropertyDescription("An internal identifier for this statistical item.")
    @NotNull
    private final String id;

    @JsonProperty("measure")
    @JsonPropertyDescription("An item from the bidStatistics codelist for the statisic reported in value.")
    @NotNull
    private final Measure measure;

    @JsonProperty("date")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonPropertyDescription("The date when this statistic was last updated. This is often the closing date of the " +
        "tender process. This field can be left blank unless either (a) the same statistic is provided from multiple " +
        "points in time, or (b) there is a specific local requirement for the data when statistics were calculated to" +
        " be provided.")
    private final LocalDateTime date;

    @JsonProperty("value")
    @JsonPropertyDescription("The value for the measure in question. Total counts should be provided as an integer. " +
        "Percentages should be presented as a proportion of 1 (e.g. 10% = 0.1)")
    @NotNull
    private final Double value;

    @JsonProperty("notes")
    @JsonPropertyDescription("Any notes required to understand or interpret the given statistic.")
    private final String notes;

    @JsonProperty("relatedLot")
    @JsonPropertyDescription("Where lots are in use, if this statistic relates to bids on a particular lot, provide " +
        "the lot identifier here. If left blank, the statistic will be interpreted as applying to the whole tender.")
    private final String relatedLot;

    @JsonCreator
    public BidsStatistic(@JsonProperty("id") final String id,
                         @JsonProperty("measure") final Measure measure,
                         @JsonProperty("date") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime date,
                         @JsonProperty("value") final Double value,
                         @JsonProperty("notes") final String notes,
                         @JsonProperty("relatedLot") final String relatedLot) {
        this.id = id;
        this.measure = measure;
        this.date = date;
        this.value = value;
        this.notes = notes;
        this.relatedLot = relatedLot;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(measure)
                                    .append(date)
                                    .append(value)
                                    .append(notes)
                                    .append(relatedLot)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof BidsStatistic)) {
            return false;
        }
        final BidsStatistic rhs = (BidsStatistic) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(measure, rhs.measure)
                                  .append(date, rhs.date)
                                  .append(value, rhs.value)
                                  .append(notes, rhs.notes)
                                  .append(relatedLot, rhs.relatedLot)
                                  .isEquals();
    }

    public enum Measure {
        REQUESTS("requests"),
        BIDS("bids"),
        VALID_BIDS("validBids"),
        BIDDERS("bidders"),
        QUALIFIED_BIDDERS("qualifiedBidders"),
        DISQUALIFIED_BIDDERS("disqualifiedBidders"),
        ELECTRONIC_BIDS("electronicBids"),
        SME_BIDS("smeBids"),
        FOREIGN_BIDS("foreignBids"),
        FOREIGN_BIDS_FROM_EU("foreignBidsFromEU"),
        TENDERS_ABNORMALLY_LOW("tendersAbnormallyLow");

        private final String value;
        private final static Map<String, Measure> CONSTANTS = new HashMap<>();

        static {
            for (final Measure c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Measure(final String value) {
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
        public static Measure fromValue(final String value) {
            final Measure constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            }
            return constant;
        }
    }
}

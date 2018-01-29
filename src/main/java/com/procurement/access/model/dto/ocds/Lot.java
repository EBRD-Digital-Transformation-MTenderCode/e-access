package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import java.util.List;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "title",
        "description",
        "status",
        "statusDetails",
        "value",
        "options",
        "recurrentProcurement",
        "renewals",
        "variants",
        "contractPeriod",
        "placeOfPerformance"
})
public class Lot {
    @JsonProperty("id")
    @JsonPropertyDescription("A local identifier for this lot, such as a lot number. This is used in relatedLot " +
            "references at the item, document and award level.")
    private String id;

    @JsonProperty("title")
    @JsonPropertyDescription("A title for this lot.")
    private final String title;

    @JsonProperty("description")
    @JsonPropertyDescription("A description of this lot.")
    private final String description;

    @JsonProperty("status")
    @JsonPropertyDescription("The current status of the process related to this lot based on the [tenderStatus " +
            "codelist](http://ocds.open-contracting.org/standard/r/1__0__0/en/schema/codelists#tender-status)")
    private final TenderStatus status;

    @JsonProperty("statusDetails")
    @JsonPropertyDescription("Additional details of status.)")
    private final TenderStatusDetails statusDetails;

    @JsonProperty("value")
    @Valid
    private final Value value;

    @JsonProperty("options")
    @JsonPropertyDescription("Details about lot options: if they will be accepted and what they can consist of. " +
            "Required by the EU")
    @Valid
    private final List<Option> options;

    @JsonProperty("recurrentProcurement")
    @JsonPropertyDescription("Details of possible recurrent procurements and their subsequent calls for competition.")
    @Valid
    private final List<RecurrentProcurement> recurrentProcurement;

    @JsonProperty("renewals")
    @JsonPropertyDescription("Details of allowable contract renewals")
    @Valid
    private final List<Renewal> renewals;

    @JsonProperty("variants")
    @JsonPropertyDescription("Details about lot variants: if they will be accepted and what they can consist of. " +
            "Required by the EU")
    @Valid
    private final List<Variant> variants;

    @JsonProperty("contractPeriod")
    @Valid
    private final Period contractPeriod;

    @JsonProperty("placeOfPerformance")
    @Valid
    private final PlaceOfPerformance placeOfPerformance;

    @JsonCreator
    public Lot(@JsonProperty("id") final String id,
               @JsonProperty("title") final String title,
               @JsonProperty("description") final String description,
               @JsonProperty("status") final TenderStatus status,
               @JsonProperty("statusDetails") final TenderStatusDetails statusDetails,
               @JsonProperty("value") final Value value,
               @JsonProperty("options") final List<Option> options,
               @JsonProperty("recurrentProcurement") final List<RecurrentProcurement> recurrentProcurement,
               @JsonProperty("renewals") final List<Renewal> renewals,
               @JsonProperty("variants") final List<Variant> variants,
               @JsonProperty("contractPeriod") final Period contractPeriod,
               @JsonProperty("placeOfPerformance") final PlaceOfPerformance placeOfPerformance) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.statusDetails = statusDetails;
        this.value = value;
        this.options = options;
        this.recurrentProcurement = recurrentProcurement;
        this.renewals = renewals;
        this.variants = variants;
        this.contractPeriod = contractPeriod;
        this.placeOfPerformance = placeOfPerformance;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                .append(title)
                .append(description)
                .append(status)
                .append(statusDetails)
                .append(value)
                .append(options)
                .append(recurrentProcurement)
                .append(renewals)
                .append(variants)
                .append(contractPeriod)
                .append(placeOfPerformance)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Lot)) {
            return false;
        }
        final Lot rhs = (Lot) other;
        return new EqualsBuilder().append(id, rhs.id)
                .append(title, rhs.title)
                .append(description, rhs.description)
                .append(status, rhs.status)
                .append(statusDetails, rhs.statusDetails)
                .append(value, rhs.value)
                .append(options, rhs.options)
                .append(recurrentProcurement, rhs.recurrentProcurement)
                .append(renewals, rhs.renewals)
                .append(variants, rhs.variants)
                .append(contractPeriod, rhs.contractPeriod)
                .append(placeOfPerformance, rhs.placeOfPerformance)
                .isEquals();
    }
}

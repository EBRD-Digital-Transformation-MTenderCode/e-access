package com.procurement.access.model.dto.pn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.procurement.access.model.dto.ocds.*;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
    "variants",
    "renewals",
    "recurrentProcurement",
    "contractPeriod",
    "placeOfPerformance",
})
public class PnLot {
    @NotNull
    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private final String title;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("status")
    private TenderStatus status;

    @JsonProperty("statusDetails")
    private TenderStatusDetails statusDetails;

    @Valid
    @JsonProperty("value")
    private final Value value;

    @Valid
    @JsonProperty("options")
    private final List<Option> options;

    @Valid
    @JsonProperty("variants")
    private final List<Variant> variants;

    @Valid
    @JsonProperty("renewals")
    private final List<Renewal> renewals;

    @Valid
    @JsonProperty("recurrentProcurement")
    private final List<RecurrentProcurement> recurrentProcurement;

    @Valid
    @JsonProperty("contractPeriod")
    private final Period contractPeriod;

    @Valid
    @JsonProperty("placeOfPerformance")
    private final PlaceOfPerformance placeOfPerformance;

    @JsonCreator
    public PnLot(@JsonProperty("id") final String id,
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
        if (!(other instanceof PnLot)) {
            return false;
        }
        final PnLot rhs = (PnLot) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(title, rhs.title)
                                  .append(description, rhs.description)
                                  .append(status,rhs.status)
                                  .append(statusDetails,rhs.statusDetails)
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

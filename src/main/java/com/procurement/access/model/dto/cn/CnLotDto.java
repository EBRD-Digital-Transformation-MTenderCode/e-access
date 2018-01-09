package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({
        "id",
        "title",
        "description",
        "value",
        "options",
        "variants",
        "renewals",
        "recurrentProcurement",
        "contractPeriod",
        "placeOfPerformance"
})
public class CnLotDto {
    @JsonProperty("id")
    @JsonPropertyDescription("A local identifier for this lot, such as a lot number. This is used in relatedLot " +
            "references at the item, document and award level.")
    private String id;

    @JsonProperty("title")
    @JsonPropertyDescription("A title for this lot.")
    @NotNull
    private final String title;

    @JsonProperty("description")
    @JsonPropertyDescription("A description of this lot.")
    @NotNull
    private final String description;

    @JsonProperty("value")
    @Valid
    @NotNull
    private final CnValueDto value;

    @JsonProperty("options")
    @JsonPropertyDescription("FsDetailsDto about lot options: if they will be accepted and what they can consist of. " +
            "Required by the EU")
    @Valid
    @NotNull
    private final List<CnOptionDto> options;

    @JsonProperty("recurrentProcurement")
    @JsonPropertyDescription("FsDetailsDto of possible recurrent procurements and their subsequent calls for " +
            "competition.")
    @Valid
    @NotNull
    private final List<CnRecurrentProcurementDto> recurrentProcurement;

    @JsonProperty("renewals")
    @JsonPropertyDescription("FsDetailsDto of allowable contract renewals")
    @Valid
    @NotNull
    private final List<CnRenewalDto> renewals;

    @JsonProperty("variants")
    @JsonPropertyDescription("FsDetailsDto about lot variants: if they will be accepted and what they can consist of." +
            " " +
            "Required by the EU")
    @Valid
    @NotNull
    private final List<CnVariantDto> variants;

    @JsonProperty("contractPeriod")
    @Valid
    private final CnPeriodDto contractPeriod;

    @JsonProperty("placeOfPerformance")
    @Valid
    private final CnPlaceOfPerformanceDto placeOfPerformance;

    @JsonCreator
    public CnLotDto(@JsonProperty("id") final String id,
                    @JsonProperty("title") final String title,
                    @JsonProperty("description") final String description,
                    @JsonProperty("value") final CnValueDto value,
                    @JsonProperty("options") final List<CnOptionDto> options,
                    @JsonProperty("recurrentProcurement") final List<CnRecurrentProcurementDto> recurrentProcurement,
                    @JsonProperty("renewals") final List<CnRenewalDto> renewals,
                    @JsonProperty("variants") final List<CnVariantDto> variants,
                    @JsonProperty("contractPeriod") final CnPeriodDto contractPeriod,
                    @JsonProperty("placeOfPerformance") final CnPlaceOfPerformanceDto placeOfPerformance) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.value = value;
        this.options = options;
        this.recurrentProcurement = recurrentProcurement;
        this.renewals = renewals;
        this.variants = variants;
        this.contractPeriod = contractPeriod;
        this.placeOfPerformance = placeOfPerformance;
    }
}

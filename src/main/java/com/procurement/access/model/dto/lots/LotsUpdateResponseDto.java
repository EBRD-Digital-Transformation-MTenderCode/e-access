package com.procurement.access.model.dto.lots;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.procurement.access.model.dto.ocds.Item;
import com.procurement.access.model.dto.ocds.Lot;
import com.procurement.access.model.dto.ocds.TenderStatus;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LotsUpdateResponseDto {

    @JsonProperty(value = "tenderStatus")
    private TenderStatus tenderStatus;

    @JsonProperty(value = "lots")
    private List<Lot> lots;

    @JsonProperty(value = "items")
    private List<Item> items;

    @JsonCreator
    public LotsUpdateResponseDto(@JsonProperty("tenderStatus") final TenderStatus tenderStatus,
                                 @JsonProperty("lots") final List<Lot> lots,
                                 @JsonProperty("items") final List<Item> items) {
        this.tenderStatus = tenderStatus;
        this.lots = lots;
        this.items = items;
    }
}

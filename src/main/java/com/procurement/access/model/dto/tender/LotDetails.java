package com.procurement.access.model.dto.tender;

import lombok.Data;

@Data
public class LotDetails {
    public Integer maximumLotsBidPerSupplier;
    public Integer maximumLotsAwardedPerSupplier;
}

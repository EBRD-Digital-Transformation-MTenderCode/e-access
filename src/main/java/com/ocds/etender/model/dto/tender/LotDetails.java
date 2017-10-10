package com.ocds.etender.model.dto.tender;

import lombok.Data;

@Data
public class LotDetails {
    public Integer maximumLotsBidPerSupplier;
    public Integer maximumLotsAwardedPerSupplier;
}

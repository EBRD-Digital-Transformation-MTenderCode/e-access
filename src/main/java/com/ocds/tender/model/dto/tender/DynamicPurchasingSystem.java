package com.ocds.tender.model.dto.tender;

import lombok.Data;

@Data
public class DynamicPurchasingSystem {
    public Boolean hasDynamicPurchasingSystem;
    public Boolean hasOutsideBuyerAccess;
    public Boolean noFurtherContracts;
}

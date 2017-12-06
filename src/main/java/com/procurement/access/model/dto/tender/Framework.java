package com.procurement.access.model.dto.tender;

import lombok.Data;

import java.util.List;

@Data
public class Framework {
    public Boolean isAFramework;
    public String typeOfFramework;
    public Integer maxSuppliers;
    public String exceptionalDurationRationale;
    public List<String> additionalBuyerCategories;
    public String description;
}

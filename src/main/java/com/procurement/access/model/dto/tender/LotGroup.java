package com.procurement.access.model.dto.tender;

import lombok.Data;

import java.util.List;

@Data
public class LotGroup {
    public String id;
    public List<String> relatedLots;
    public Boolean optionToCombine;
    public Value maximumValue;
}

package com.ocds.etender.model.dto.tender;

import java.util.List;

public class LotGroup {
    public String id;
    public List<String> relatedLots;
    public Boolean optionToCombine;
    public Value maximumValue;
}

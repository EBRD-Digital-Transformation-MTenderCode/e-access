package com.ocds.access.model.dto.budget;

import com.ocds.access.model.dto.tender.Value;
import lombok.Data;

@Data
public class Budget {
    public String id;
    public String description;
    public Value amount;
    public String project;
    public String projectID;
    public String uri;
    public String source;
    public EuropeanUnionFunding europeanUnionFunding;
}


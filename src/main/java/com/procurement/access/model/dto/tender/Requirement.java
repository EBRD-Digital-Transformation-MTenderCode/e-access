package com.procurement.access.model.dto.tender;

import lombok.Data;

@Data
public class Requirement {
    public String id;
    public String title;
    public String description;
    public String dataType;
    public String pattern;
    public Object expectedValue;
    public Object minValue;
    public Object maxValue;
    public Period period;
}

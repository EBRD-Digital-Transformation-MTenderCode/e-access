package com.procurement.access.model.dto.tender;

import lombok.Data;

import java.util.List;

@Data
public class Criterion {
    public String id;
    public String title;
    public String description;
    public String source;
    public String relatesTo;
    public String relatedItem;
    public List<RequirementGroup> requirementGroups;
}

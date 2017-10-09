package com.ocds.etender.model.dto.tender;

import java.util.List;

public class Criterion {
    public String id;
    public String title;
    public String description;
    public String source;
    public String relatesTo;
    public String relatedItem;
    public List<RequirementGroup> requirementGroups;
}

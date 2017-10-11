package com.ocds.tender.model.dto.tender;

import lombok.Data;

import java.util.List;

@Data
public class RequirementGroup {
    public String id;
    public String description;
    public List<Requirement> requirements;
}

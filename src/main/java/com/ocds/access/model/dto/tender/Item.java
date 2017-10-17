package com.ocds.access.model.dto.tender;

import lombok.Data;

import java.util.List;

@Data
public class Item {
    public String id;
    public String description;
    public Classification classification;
    public List<Classification> additionalClassifications;
    public Integer quantity;
    public Unit unit;
}
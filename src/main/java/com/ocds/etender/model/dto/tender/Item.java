package com.ocds.etender.model.dto.tender;

import java.util.List;

/**
 * Author: user
 * Created by: ModelGenerator on 10/5/17
 */
public class Item {
    public String id;
    public String description;
    public Classification classification;
    public List<Classification> additionalClassifications;
    public Integer quantity;
    public Unit unit;
}
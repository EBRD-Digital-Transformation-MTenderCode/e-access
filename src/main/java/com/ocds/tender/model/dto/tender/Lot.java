package com.ocds.tender.model.dto.tender;

import lombok.Data;

import java.util.List;

@Data
public class Lot {
    public String id;
    public String title;
    public String description;
    public String status;
    public Value value;
    public List<Options> options;
    public RecurrentProcurement recurrentProcurement;
    public List<Renewals> renewals;
    public List<Variants> variants;
}

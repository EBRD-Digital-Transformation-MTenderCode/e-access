package com.procurement.access.model.dto.tender;

import lombok.Data;

import java.util.List;

@Data
public class ParticipationFee {
    public List<String> type;
    public Value value;
    public String description;
    public List<String> methodOfPayment;
}

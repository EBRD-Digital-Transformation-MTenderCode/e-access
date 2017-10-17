package com.ocds.access.model.dto.tender;

import lombok.Data;

import java.util.List;

@Data
public class RecurrentProcurement {
    public Boolean isRecurrent;
    public List<Period> dates;
    public String description;
}

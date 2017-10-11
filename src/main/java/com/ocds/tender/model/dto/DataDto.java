package com.ocds.tender.model.dto;

import com.ocds.tender.model.dto.budget.Budget;
import com.ocds.tender.model.dto.relatedNotice.RelatedNotice;
import com.ocds.tender.model.dto.tender.Tender;
import lombok.Data;

@Data
public class DataDto {
    String ocid;
    Tender tender;
    Budget budget;
    RelatedNotice relatedNotice;
}

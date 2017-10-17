package com.ocds.access.model.dto;

import com.ocds.access.model.dto.budget.Budget;
import com.ocds.access.model.dto.relatedNotice.RelatedNotice;
import com.ocds.access.model.dto.tender.Tender;
import lombok.Data;

@Data
public class DataDto {
    String ocid;
    Tender tender;
    Budget budget;
    RelatedNotice relatedNotice;
}

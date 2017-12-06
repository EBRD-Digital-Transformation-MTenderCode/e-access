package com.procurement.access.model.dto;

import com.procurement.access.model.dto.budget.Budget;
import com.procurement.access.model.dto.relatedNotice.RelatedNotice;
import com.procurement.access.model.dto.tender.Tender;
import lombok.Data;

@Data
public class DataDto {
    String ocid;
    Tender tender;
    Budget budget;
    RelatedNotice relatedNotice;
}

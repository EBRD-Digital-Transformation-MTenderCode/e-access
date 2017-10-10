package com.ocds.etender.model.dto;

import com.ocds.etender.model.dto.budget.Budget;
import com.ocds.etender.model.dto.relatedNotice.RelatedNotice;
import com.ocds.etender.model.dto.tender.Tender;
import lombok.Data;

import java.util.Date;

@Data
public class DataDto {
    String ocid;
    Tender tender;
    Budget budget;
    RelatedNotice relatedNotice;
}

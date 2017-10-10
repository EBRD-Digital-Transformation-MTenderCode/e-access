package com.ocds.etender.model.dto;

import com.ocds.etender.model.dto.budget.Budget;
import com.ocds.etender.model.dto.relatedNotice.RelatedNotice;
import com.ocds.etender.model.dto.tender.Tender;

import java.util.Date;

public class DataDto {

    String ocid;

    Date date;

    Tender tender;

    Budget budget;

    RelatedNotice relatedNotice;
}

package com.ocds.etender.model.dto;

import com.ocds.etender.model.dto.tender.Tender;
import lombok.Data;

import java.util.Date;

@Data
public class DataDto {

    String ocid;

    Date date;

    Tender tender;
//
//    Budget budget;
//
//    RelatedNotice relatedNotice;
}

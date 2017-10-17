package com.ocds.access.service;

import com.ocds.access.model.dto.tender.Tender;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public interface TenderService {

    void insertData(String ocId, Date addedDate, Tender data);

    void updateData(String ocId, Date addedDate, Tender data);

}

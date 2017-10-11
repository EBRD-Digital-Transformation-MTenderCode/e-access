package com.ocds.tender.service;

import com.ocds.tender.model.dto.relatedNotice.RelatedNotice;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public interface RelatedNoticeService {

    void updateData(String ocId, Date addedDate, RelatedNotice data);
}

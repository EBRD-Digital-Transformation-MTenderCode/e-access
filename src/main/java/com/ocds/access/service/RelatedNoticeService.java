package com.ocds.access.service;

import com.ocds.access.model.dto.relatedNotice.RelatedNotice;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public interface RelatedNoticeService {

    void insertData(String ocId, Date addedDate, RelatedNotice data);

    void updateData(String ocId, Date addedDate, RelatedNotice data);
}

package com.ocds.tender.service;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public interface EventLogService {

    void updateData(String ocId, Date addedDate, String eventType, UUID id);

}

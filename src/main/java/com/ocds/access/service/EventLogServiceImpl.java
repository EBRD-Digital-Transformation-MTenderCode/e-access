package com.ocds.access.service;

import com.ocds.access.model.entity.EventLogEntity;
import com.ocds.access.repository.EventLogRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Service
public class EventLogServiceImpl implements EventLogService {

    private EventLogRepository eventLogRepository;

    public EventLogServiceImpl(EventLogRepository eventLogRepository) {
        this.eventLogRepository = eventLogRepository;
    }

    @Override
    public void updateData(String ocId, Date addedDate, String eventType, UUID id) {
        Objects.requireNonNull(ocId);
        Objects.requireNonNull(addedDate);
        Objects.requireNonNull(eventType);
        Objects.requireNonNull(id);
        EventLogEntity eventEntity = new EventLogEntity();
        eventEntity.setOcId(ocId);
        eventEntity.setAddedDate(addedDate);
        eventEntity.setEventType(eventType);
        eventEntity.setId(id);
        eventLogRepository.save(eventEntity);
    }
}

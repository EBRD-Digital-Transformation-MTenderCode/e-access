package com.ocds.tender.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocds.tender.model.dto.tender.Tender;
import com.ocds.tender.model.entity.TenderEntity;
import com.ocds.tender.repository.TenderRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
public class TenderServiceImpl implements TenderService {

    private TenderRepository tenderRepository;

    private EventLogService eventLogService;

    private ObjectMapper objectMapper;

    public TenderServiceImpl(TenderRepository tenderRepository,
                             EventLogService eventLogService,
                             ObjectMapper objectMapper) {
        this.tenderRepository = tenderRepository;
        this.eventLogService = eventLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void updateData(String ocId, Date addedDate, Tender tenderDto) {
        if (Objects.nonNull(tenderDto)) {
            TenderEntity tenderEntity = new TenderEntity();
            tenderEntity.setOcId(ocId);
            tenderEntity.setAddedDate(addedDate);
            try {
                String tenderJson = objectMapper.writeValueAsString(tenderDto);
                tenderEntity.setJsonData(tenderJson);
                tenderRepository.save(tenderEntity);
                eventLogService.updateData(ocId, addedDate, "tender", tenderEntity.getId());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}

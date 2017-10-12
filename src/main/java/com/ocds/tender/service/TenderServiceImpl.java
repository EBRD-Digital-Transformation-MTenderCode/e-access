package com.ocds.tender.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocds.tender.model.dto.tender.Tender;
import com.ocds.tender.model.entity.EventType;
import com.ocds.tender.model.entity.TenderEntity;
import com.ocds.tender.repository.TenderRepository;
import com.ocds.tender.utils.JsonUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    public void insertData(String ocId, Date addedDate, Tender tenderDto) {
        if (Objects.nonNull(tenderDto)) {
            TenderEntity tenderEntity = convertDtoToEntity(ocId, addedDate, tenderDto);
            saveEntity(ocId, addedDate, tenderEntity);
        }
    }

    @Override
    public void updateData(String ocId, Date addedDate, Tender tenderDto) {
        if (Objects.nonNull(tenderDto)) {
            TenderEntity sourceTenderEntity = tenderRepository.getLastByOcId(ocId);
            if (Objects.nonNull(sourceTenderEntity)) {
                Tender newTenderDto = mergeData(sourceTenderEntity.getJsonData(), tenderDto);
                if (Objects.nonNull(newTenderDto)) {
                    TenderEntity newTenderEntity = convertDtoToEntity(ocId, addedDate, newTenderDto);
                    saveEntity(ocId, addedDate, newTenderEntity);
                }
            }else{
                TenderEntity tenderEntity = convertDtoToEntity(ocId, addedDate, tenderDto);
                saveEntity(ocId, addedDate, tenderEntity);
            }
        }
    }

    public Tender mergeData(String sourceJsonData, Tender tenderDto) {
        JsonNode updateJson = objectMapper.valueToTree(tenderDto);
        JsonNode sourceJson = null;
        try {
            sourceJson = objectMapper.readTree(sourceJsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonNode mergedJson = null;
        if (Objects.nonNull(sourceJson) && Objects.nonNull(updateJson)) {
            mergedJson = JsonUtils.merge(sourceJson, updateJson);
        }
        Tender tender = null;
        if (Objects.nonNull(mergedJson)) {
            try {
                tender = objectMapper.treeToValue(mergedJson, Tender.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return tender;
    }

    public TenderEntity convertDtoToEntity(String ocId, Date addedDate, Tender tenderDto) {
        TenderEntity tenderEntity = null;
        if (Objects.nonNull(tenderDto)) {
            tenderEntity = new TenderEntity();
            tenderEntity.setOcId(ocId);
            tenderEntity.setAddedDate(addedDate);
            try {
                String tenderJson = objectMapper.writeValueAsString(tenderDto);
                tenderEntity.setJsonData(tenderJson);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            tenderEntity.setEventType(EventType.TENDER.getText());
        }
        return tenderEntity;
    }

    public void saveEntity(String ocId, Date addedDate, TenderEntity tenderEntity) {
        if (Objects.nonNull(tenderEntity.getJsonData())) {
            tenderRepository.save(tenderEntity);
            eventLogService.updateData(ocId, addedDate, tenderEntity.getEventType(), tenderEntity.getId());
        }
    }
}

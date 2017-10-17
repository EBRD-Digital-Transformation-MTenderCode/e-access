package com.ocds.tender.service;

import com.ocds.tender.model.dto.tender.Tender;
import com.ocds.tender.model.entity.EventType;
import com.ocds.tender.model.entity.TenderEntity;
import com.ocds.tender.repository.TenderRepository;
import com.ocds.tender.utils.JsonUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
public class TenderServiceImpl implements TenderService {

    private TenderRepository tenderRepository;

    private EventLogService eventLogService;

    private JsonUtil jsonUtil;

    public TenderServiceImpl(TenderRepository tenderRepository,
                             EventLogService eventLogService,
                             JsonUtil jsonUtil) {
        this.tenderRepository = tenderRepository;
        this.eventLogService = eventLogService;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public void insertData(String ocId, Date addedDate, Tender tenderDto) {
        Objects.requireNonNull(ocId);
        Objects.requireNonNull(addedDate);
        Objects.requireNonNull(tenderDto);
        convertDtoToEntity(ocId, addedDate, tenderDto)
            .ifPresent(tender -> {
                tenderRepository.save(tender);
                eventLogService.updateData(ocId, addedDate, tender.getEventType(), tender.getId());
            });
    }

    @Override
    public void updateData(String ocId, Date addedDate, Tender tenderDto) {
        Objects.requireNonNull(ocId);
        Objects.requireNonNull(addedDate);
        Objects.requireNonNull(tenderDto);
        TenderEntity sourceTenderEntity = tenderRepository.getLastByOcId(ocId);
        Tender mergedTender = mergeJson(sourceTenderEntity, tenderDto);
        convertDtoToEntity(ocId, addedDate, mergedTender)
            .ifPresent(tender -> {
                tenderRepository.save(tender);
                eventLogService.updateData(ocId, addedDate, tender.getEventType(), tender.getId());
            });
    }

    public Tender mergeJson(TenderEntity tenderEntity, Tender tenderDto) {
        Objects.requireNonNull(tenderEntity);
        Objects.requireNonNull(tenderDto);
        String sourceJson = tenderEntity.getJsonData();
        String updateJson = jsonUtil.toJson(tenderDto);
        String mergedJson = jsonUtil.merge(sourceJson, updateJson);
        return jsonUtil.toObject(Tender.class, mergedJson);
    }

    public Optional<TenderEntity> convertDtoToEntity(String ocId, Date addedDate, Tender tenderDto) {
        String tenderJson = jsonUtil.toJson(tenderDto);
        if (!tenderJson.equals("{}")) {
            TenderEntity tenderEntity = new TenderEntity();
            tenderEntity.setOcId(ocId);
            tenderEntity.setAddedDate(addedDate);
            tenderEntity.setEventType(EventType.TENDER.getText());
            tenderEntity.setJsonData(tenderJson);
            return Optional.of(tenderEntity);
        } else {
            return Optional.empty();
        }
    }
}

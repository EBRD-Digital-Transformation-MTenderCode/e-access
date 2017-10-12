package com.ocds.tender.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocds.tender.model.dto.relatedNotice.RelatedNotice;
import com.ocds.tender.model.entity.EventType;
import com.ocds.tender.model.entity.RelatedNoticeEntity;
import com.ocds.tender.repository.RelatedNoticeRepository;
import com.ocds.tender.utils.JsonUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

@Service
public class RelatedNoticeServiceImpl implements RelatedNoticeService {

    private RelatedNoticeRepository relatedNoticeRepository;

    private ObjectMapper objectMapper;

    public RelatedNoticeServiceImpl(RelatedNoticeRepository relatedNoticeRepository,
                                    ObjectMapper objectMapper) {
        this.relatedNoticeRepository = relatedNoticeRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void insertData(String ocId, Date addedDate, RelatedNotice relatedNoticeDto) {
        if (Objects.nonNull(relatedNoticeDto)) {
            RelatedNoticeEntity relatedNoticeEntity = convertDtoToEntity(ocId, addedDate, relatedNoticeDto);
            saveEntity(relatedNoticeEntity);
        }
    }

    @Override
    public void updateData(String ocId, Date addedDate, RelatedNotice relatedNoticeDto) {
        if (Objects.nonNull(relatedNoticeDto)) {
            RelatedNoticeEntity sourceRelatedNoticeEntity = relatedNoticeRepository.getLastByOcId(ocId);
            if (Objects.nonNull(sourceRelatedNoticeEntity)) {
                RelatedNotice newRelatedNoticeDto = mergeData(sourceRelatedNoticeEntity.getJsonData(),
                    relatedNoticeDto);
                if (Objects.nonNull(newRelatedNoticeDto)) {
                    RelatedNoticeEntity newRelatedNoticeEntity = convertDtoToEntity(ocId, addedDate,
                        newRelatedNoticeDto);
                    saveEntity(newRelatedNoticeEntity);
                }
            } else {
                RelatedNoticeEntity relatedNoticeEntity = convertDtoToEntity(ocId, addedDate, relatedNoticeDto);
                saveEntity(relatedNoticeEntity);
            }
        }
    }

    public RelatedNotice mergeData(String sourceJsonData, RelatedNotice relatedNoticeDto) {
        JsonNode updateJson = objectMapper.valueToTree(relatedNoticeDto);
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
        RelatedNotice relatedNotice = null;
        if (Objects.nonNull(mergedJson)) {
            try {
                relatedNotice = objectMapper.treeToValue(mergedJson, RelatedNotice.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return relatedNotice;
    }

    public RelatedNoticeEntity convertDtoToEntity(String ocId, Date addedDate, RelatedNotice relatedNoticeDto) {
        RelatedNoticeEntity relatedNoticeEntity = null;
        if (Objects.nonNull(relatedNoticeDto)) {
            relatedNoticeEntity = new RelatedNoticeEntity();
            relatedNoticeEntity.setOcId(ocId);
            relatedNoticeEntity.setAddedDate(addedDate);
            try {
                String relatedNoticeJson = objectMapper.writeValueAsString(relatedNoticeDto);
                relatedNoticeEntity.setJsonData(relatedNoticeJson);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            relatedNoticeEntity.setEventType(EventType.RELATED_NOTICE.getText());
        }
        return relatedNoticeEntity;
    }

    public void saveEntity(RelatedNoticeEntity relatedNoticeEntity) {
        if (Objects.nonNull(relatedNoticeEntity.getJsonData())) {
            relatedNoticeRepository.save(relatedNoticeEntity);
        }
    }
}

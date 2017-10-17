package com.ocds.access.service;

import com.ocds.access.model.dto.relatedNotice.RelatedNotice;
import com.ocds.access.model.entity.EventType;
import com.ocds.access.model.entity.RelatedNoticeEntity;
import com.ocds.access.repository.RelatedNoticeRepository;
import com.ocds.access.utils.JsonUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
public class RelatedNoticeServiceImpl implements RelatedNoticeService {

    private RelatedNoticeRepository relatedNoticeRepository;

    private JsonUtil jsonUtil;

    public RelatedNoticeServiceImpl(RelatedNoticeRepository relatedNoticeRepository,
                                    JsonUtil jsonUtil) {
        this.relatedNoticeRepository = relatedNoticeRepository;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public void insertData(String ocId, Date addedDate, RelatedNotice relatedNoticeDto) {
        Objects.requireNonNull(ocId);
        Objects.requireNonNull(addedDate);
        Objects.requireNonNull(relatedNoticeDto);
        convertDtoToEntity(ocId, addedDate, relatedNoticeDto)
            .ifPresent(relatedNoticeRepository::save);
    }

    @Override
    public void updateData(String ocId, Date addedDate, RelatedNotice relatedNoticeDto) {
        Objects.requireNonNull(ocId);
        Objects.requireNonNull(addedDate);
        Objects.requireNonNull(relatedNoticeDto);
        RelatedNoticeEntity sourceRelatedNoticeEntity = relatedNoticeRepository.getLastByOcId(ocId);
        RelatedNotice mergedRelatedNotice = mergeJson(sourceRelatedNoticeEntity, relatedNoticeDto);
        convertDtoToEntity(ocId, addedDate, mergedRelatedNotice)
            .ifPresent(relatedNoticeRepository::save);
    }

    public RelatedNotice mergeJson(RelatedNoticeEntity relatedNoticeEntity, RelatedNotice relatedNoticeDto) {
        Objects.requireNonNull(relatedNoticeEntity);
        Objects.requireNonNull(relatedNoticeDto);
        String sourceJson = relatedNoticeEntity.getJsonData();
        String updateJson = jsonUtil.toJson(relatedNoticeDto);
        String mergedJson = jsonUtil.merge(sourceJson, updateJson);
        return jsonUtil.toObject(RelatedNotice.class, mergedJson);
    }

    public Optional<RelatedNoticeEntity> convertDtoToEntity(String ocId, Date addedDate,
                                                            RelatedNotice relatedNoticeDto) {
        String relatedNoticeJson = jsonUtil.toJson(relatedNoticeDto);
        if (!relatedNoticeJson.equals("{}")) {
            RelatedNoticeEntity relatedNoticeEntity = new RelatedNoticeEntity();
            relatedNoticeEntity.setOcId(ocId);
            relatedNoticeEntity.setAddedDate(addedDate);
            relatedNoticeEntity.setEventType(EventType.RELATED_NOTICE.getText());
            relatedNoticeEntity.setJsonData(relatedNoticeJson);
            return Optional.of(relatedNoticeEntity);
        } else {
            return Optional.empty();
        }
    }
}

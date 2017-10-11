package com.ocds.tender.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocds.tender.model.dto.relatedNotice.RelatedNotice;
import com.ocds.tender.model.entity.RelatedNoticeEntity;
import com.ocds.tender.repository.RelatedNoticeRepository;
import org.springframework.stereotype.Service;

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
    public void updateData(String ocId, Date addedDate, RelatedNotice relatedNoticeDto) {
        if (Objects.nonNull(relatedNoticeDto)) {
            RelatedNoticeEntity relatedNoticeEntity = new RelatedNoticeEntity();
            relatedNoticeEntity.setOcId(ocId);
            relatedNoticeEntity.setAddedDate(addedDate);
            String relatedJson;
            try {
                relatedJson = objectMapper.writeValueAsString(relatedNoticeDto);
                relatedNoticeEntity.setJsonData(relatedJson);
                relatedNoticeRepository.save(relatedNoticeEntity);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}

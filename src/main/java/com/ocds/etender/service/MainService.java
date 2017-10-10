package com.ocds.etender.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocds.etender.model.dto.DataDto;
import com.ocds.etender.model.dto.budget.Budget;
import com.ocds.etender.model.dto.relatedNotice.RelatedNotice;
import com.ocds.etender.model.dto.tender.Tender;
import com.ocds.etender.model.entity.BudgetEntity;
import com.ocds.etender.model.entity.EventEntity;
import com.ocds.etender.model.entity.RelatedNoticeEntity;
import com.ocds.etender.model.entity.TenderEntity;
import com.ocds.etender.repository.BudgetRepository;
import com.ocds.etender.repository.EventRepository;
import com.ocds.etender.repository.RelatedNoticeRepository;
import com.ocds.etender.repository.TenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
public class MainService {

    @Autowired
    private TenderRepository tenderRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private RelatedNoticeRepository relatedNoticeRepository;

    public void updateData(DataDto data) {
        ObjectMapper mapper = new ObjectMapper();
        Date addedDate = new Date();

        Tender tenderDto = data.getTender();
        if (Objects.nonNull(tenderDto)) {
            TenderEntity tenderEntity = new TenderEntity();
            tenderEntity.setOcId(data.getOcid());
            tenderEntity.setAddedDate(addedDate);
            try {
                String tenderJson = mapper.writeValueAsString(tenderDto);
                tenderEntity.setJsonData(tenderJson);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            tenderRepository.save(tenderEntity);

            EventEntity eventEntity = new EventEntity();
            eventEntity.setOcId(tenderEntity.getOcId());
            eventEntity.setAddedDate(tenderEntity.getAddedDate());
            eventEntity.setType("tender");
            eventEntity.setId(tenderEntity.getId());
            eventRepository.save(eventEntity);
        }

        Budget budgetDto = data.getBudget();
        if (Objects.nonNull(budgetDto)) {
            BudgetEntity budgetEntity = new BudgetEntity();
            budgetEntity.setOcId(data.getOcid());
            budgetEntity.setAddedDate(addedDate);
            try {
                String budgetJson = mapper.writeValueAsString(budgetDto);
                budgetEntity.setJsonData(budgetJson);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            budgetRepository.save(budgetEntity);
        }

        RelatedNotice relatedNoticeDto = data.getRelatedNotice();
        if (Objects.nonNull(relatedNoticeDto)) {
            RelatedNoticeEntity relatedNoticeEntity = new RelatedNoticeEntity();
            relatedNoticeEntity.setOcId(data.getOcid());
            relatedNoticeEntity.setAddedDate(addedDate);
            try {
                String relatedJson = mapper.writeValueAsString(relatedNoticeDto);
                relatedNoticeEntity.setJsonData(relatedJson);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            relatedNoticeRepository.save(relatedNoticeEntity);
        }
    }
}

package com.ocds.etender.service;

import com.ocds.etender.model.dto.DataDto;
import com.ocds.etender.model.entity.EventEntity;
import com.ocds.etender.model.entity.TenderEntity;
import com.ocds.etender.repository.EventRepository;
import com.ocds.etender.repository.TenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MainService {

    @Autowired
    private TenderRepository tenderRepository;

    @Autowired
    private EventRepository eventRepository;

    public void updateData(DataDto data) {
        TenderEntity tender = new TenderEntity();
        tender.setOcId("ocds-213czf-000-00001");
        tender.setAddedDate(new Date());
        tender.setJsonData("{id: ocds-213czf-000-00001-01-planning}");
        tenderRepository.save(tender);

        EventEntity eventEntity = new EventEntity();
        eventEntity.setOcId(tender.getOcId());
        eventEntity.setAddedDate(tender.getAddedDate());
        eventEntity.setType("tender");
        eventEntity.setId(tender.getId());
        eventRepository.save(eventEntity);
    }
}

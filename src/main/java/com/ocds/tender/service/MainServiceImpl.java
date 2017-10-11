package com.ocds.tender.service;

import com.ocds.tender.model.dto.DataDto;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
public class MainServiceImpl {

    private TenderService tenderService;

    private BudgetService budgetService;

    private RelatedNoticeService relatedNoticeService;

    public MainServiceImpl(TenderService tenderService,
                           BudgetService budgetService,
                           RelatedNoticeService relatedNoticeService) {
        this.tenderService = tenderService;
        this.budgetService = budgetService;
        this.relatedNoticeService = relatedNoticeService;
    }

    public void updateData(DataDto data) {
        Date addedDate = new Date();
        String osId = data.getOcid();
        if (Objects.nonNull(osId)) { //update
            tenderService.updateData(osId, addedDate, data.getTender());
            budgetService.updateData(osId, addedDate, data.getBudget());
            relatedNoticeService.updateData(osId, addedDate, data.getRelatedNotice());
        } else {
            osId = "ocds-213czf-000-00001";
            tenderService.insertData(osId, addedDate, data.getTender());
            budgetService.insertData(osId, addedDate, data.getBudget());
            relatedNoticeService.insertData(osId, addedDate, data.getRelatedNotice());
        }
    }
}


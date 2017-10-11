package com.ocds.tender.service;

import com.ocds.tender.model.dto.DataDto;
import org.springframework.stereotype.Service;

import java.util.Date;

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
        String osid = data.getOcid();
        tenderService.updateData(osid, addedDate, data.getTender());
        budgetService.updateData(osid, addedDate, data.getBudget());
        relatedNoticeService.updateData(osid, addedDate, data.getRelatedNotice());
    }
}


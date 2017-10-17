package com.ocds.access.service;

import com.ocds.access.config.properties.OCDSProperties;
import com.ocds.access.model.dto.DataDto;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@Service
public class MainServiceImpl implements MainService {

    private TenderService tenderService;

    private BudgetService budgetService;

    private RelatedNoticeService relatedNoticeService;

    private OCDSProperties ocdsProperties;

    public MainServiceImpl(TenderService tenderService,
                           BudgetService budgetService,
                           RelatedNoticeService relatedNoticeService,
                           OCDSProperties ocdsProperties) {
        this.tenderService = tenderService;
        this.budgetService = budgetService;
        this.relatedNoticeService = relatedNoticeService;
        this.ocdsProperties = ocdsProperties;
    }

    @Override
    public void insertData(DataDto data) {
        Date addedDate = new Date();
        String osId = data.getOcid();
        if (Objects.isNull(osId)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String sDate = sdf.format(addedDate);
            osId = ocdsProperties.getPrefix() + "-" + sDate + "-" + addedDate.getTime();
        }
        tenderService.insertData(osId, addedDate, data.getTender());
        budgetService.insertData(osId, addedDate, data.getBudget());
        relatedNoticeService.insertData(osId, addedDate, data.getRelatedNotice());
    }

    @Override
    public void updateData(DataDto data) {
        Date addedDate = new Date();
        String osId = data.getOcid();
        if (Objects.nonNull(osId)) { //update
            tenderService.updateData(osId, addedDate, data.getTender());
            budgetService.updateData(osId, addedDate, data.getBudget());
            relatedNoticeService.updateData(osId, addedDate, data.getRelatedNotice());
        }
    }
}

